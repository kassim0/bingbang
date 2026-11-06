import {test, expect} from '@playwright/test';
import {createGamesList, signInWithNewAccount} from './helpers';

test('creates a new games list by searching and adding a game', async ({page}) => {
  const listName = `E2E List ${Date.now()}`;

  await signInWithNewAccount(page);

  await createGamesList(page, listName, ['zelda']);

  await expect(page.getByRole('heading', {name: listName})).toBeVisible();
});
