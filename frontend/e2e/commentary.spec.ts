import {test, expect} from '@playwright/test';
import {addFirstSearchResult, createGamesList, gamesListCard, signInWithNewAccount} from './helpers';

/** ROADMAP phase 2 : commentaires sur les jeux d'une liste. */

test('a comment survives the removal of its game and comes back when the game is added again', async ({page}) => {
  const listName = `E2E Commentary ${Date.now()}`;
  await signInWithNewAccount(page);
  await createGamesList(page, listName, ['outer wilds']);

  // écrire un commentaire
  await gamesListCard(page, listName).click();
  await page.locator('[data-testid^="commentary-add-"]').click();
  await page.locator('[data-testid^="commentary-input-"]').fill('Play it blind!');
  await page.locator('[data-testid^="commentary-save-"]').click();
  await expect(page.locator('[data-testid^="commentary-text-"]')).toHaveText('Play it blind!');

  // retirer le jeu de la liste
  await page.locator('[data-testid^="delete-game-"]').click();
  await Promise.all([
    page.waitForResponse(resp => resp.url().includes('/api/game/updateGamesList')),
    page.getByTestId('my-list-save').click(),
  ]);
  await page.getByTestId('my-list-close').click();
  await expect(gamesListCard(page, listName).getByText('empty')).toBeVisible();

  // rajouter le même jeu : le commentaire est toujours là
  await gamesListCard(page, listName).click();
  await page.getByTestId('my-list-add-game').click();
  await addFirstSearchResult(page, 'outer wilds');
  await page.getByTestId('popup-close').click();

  await expect(page.locator('[data-testid^="commentary-text-"]')).toHaveText('Play it blind!');
});

test('a comment can be edited then deleted, and stays after a reload', async ({page}) => {
  const listName = `E2E Commentary edit ${Date.now()}`;
  await signInWithNewAccount(page);
  await createGamesList(page, listName, ['hades']);

  await gamesListCard(page, listName).click();
  await page.locator('[data-testid^="commentary-add-"]').click();
  await page.locator('[data-testid^="commentary-input-"]').fill('Good');
  await page.locator('[data-testid^="commentary-save-"]').click();
  await page.locator('[data-testid^="commentary-edit-"]').click();
  await page.locator('[data-testid^="commentary-input-"]').fill('One more run...');
  await page.locator('[data-testid^="commentary-save-"]').click();
  await expect(page.locator('[data-testid^="commentary-text-"]')).toHaveText('One more run...');

  await page.reload();
  await gamesListCard(page, listName).click();
  await expect(page.locator('[data-testid^="commentary-text-"]')).toHaveText('One more run...');

  await page.locator('[data-testid^="commentary-delete-"]').click();
  await expect(page.locator('[data-testid^="commentary-add-"]')).toBeVisible();
});
