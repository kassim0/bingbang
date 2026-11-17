import {test, expect, Page} from '@playwright/test';
import {createGamesList, gamesListCard, signInWithNewAccount} from './helpers';

/** ROADMAP phase 4 : partage en lecture seule. Le « visiteur » est un autre navigateur, sans compte. */

/** Crée un lien depuis la fenêtre de partage de la home et retourne son adresse. */
async function createShareLink(page: Page, options: {listName?: string} = {}): Promise<string> {
  await page.getByTestId('open-share-dialog').click();
  if (options.listName) {
    await page.getByTestId('share-scope-lists').check();
    await page.getByLabel(options.listName, {exact: true}).check();
  }
  await page.getByTestId('share-create').click();
  const url = await page.getByTestId('share-link-url').first().inputValue();
  expect(url).toContain('/share/');
  return url;
}

test('a visitor opens a share link and can only read; a revoked link stops working', async ({page, browser}) => {
  const listName = `E2E Shared ${Date.now()}`;
  await signInWithNewAccount(page);
  const [gameName] = await createGamesList(page, listName, ['celeste']);
  await expect(gamesListCard(page, listName)).toBeVisible();

  const url = await createShareLink(page);

  // le visiteur : un navigateur neuf, sans token
  const visitorContext = await browser.newContext();
  const visitor = await visitorContext.newPage();
  await visitor.goto(url);

  await expect(visitor.getByTestId('shared-banner')).toContainText('Read only');
  await expect(visitor.getByRole('heading', {name: listName})).toBeVisible();
  await expect(visitor.getByText(gameName, {exact: true})).toBeVisible();
  // lecture seule : aucun moyen de créer ou de modifier une liste sur cette page
  await expect(visitor.getByTestId('open-search-popup')).toHaveCount(0);
  await expect(visitor.locator('[data-testid^="games-list-card-"]')).toHaveCount(0);

  // le propriétaire révoque le lien
  await page.getByTestId('share-link-revoke').click();
  await expect(page.getByTestId('share-no-link')).toBeVisible();

  await visitor.reload();
  await expect(visitor.getByTestId('shared-not-found')).toBeVisible();
  await visitorContext.close();
});

test('sharing a single list does not show the other lists', async ({page, browser}) => {
  const sharedName = `E2E Shared one ${Date.now()}`;
  const privateName = `E2E Private ${Date.now()}`;
  await signInWithNewAccount(page);
  await createGamesList(page, sharedName, ['hades']);
  // attendre que la première liste soit enregistrée avant de recharger la page pour créer la seconde
  await expect(gamesListCard(page, sharedName)).toBeVisible();
  await createGamesList(page, privateName, ['portal']);
  await expect(gamesListCard(page, privateName)).toBeVisible();

  const url = await createShareLink(page, {listName: sharedName});

  const visitorContext = await browser.newContext();
  const visitor = await visitorContext.newPage();
  await visitor.goto(url);

  await expect(visitor.getByRole('heading', {name: sharedName})).toBeVisible();
  await expect(visitor.getByRole('heading', {name: privateName})).toHaveCount(0);
  await visitorContext.close();
});
