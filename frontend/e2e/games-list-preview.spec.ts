import {test, expect} from '@playwright/test';
import {createGamesList, gamesListCard} from './helpers';

test('shows the created games list preview on the dashboard', async ({page}) => {
  const listName = `E2E Preview ${Date.now()}`;

  const [firstGame, secondGame] = await createGamesList(page, listName, ['mario', 'sonic']);

  const card = gamesListCard(page, listName);
  await expect(card).toBeVisible();
  await expect(card.getByText(firstGame, {exact: true})).toBeVisible();
  await expect(card.getByText(secondGame, {exact: true})).toBeVisible();
  await expect(card.locator('img')).toHaveCount(2);
});
