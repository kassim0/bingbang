import {test, expect} from '@playwright/test';
import {createGamesList, gamesListCard, signInWithNewAccount} from './helpers';

test('creates a new games list by searching and adding a game', async ({page}) => {
  const listName = `E2E List ${Date.now()}`;

  await signInWithNewAccount(page);

  await createGamesList(page, listName, ['zelda']);

  await expect(page.getByRole('heading', {name: listName})).toBeVisible();
});

test('creates a games list with only a name, without any game', async ({page}) => {
  const listName = `E2E Name only ${Date.now()}`;

  await signInWithNewAccount(page);

  await createGamesList(page, listName, []);

  const card = gamesListCard(page, listName);
  await expect(card).toBeVisible();
  await expect(card.getByText('empty')).toBeVisible();
  // la liste existe bien côté serveur, pas seulement à l'écran
  await page.reload();
  await expect(gamesListCard(page, listName)).toBeVisible();
});
