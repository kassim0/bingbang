import {test, expect} from '@playwright/test';
import {addFirstSearchResult, createGamesList, gamesListCard} from './helpers';

test('modifies a games list name and its games', async ({page}) => {
  const initialName = `E2E Modify ${Date.now()}`;
  const updatedName = `${initialName} - updated`;

  const [originalGameName] = await createGamesList(page, initialName, ['mario']);

  await gamesListCard(page, initialName).click();

  await page.getByTestId('edit-list-name-input').fill(updatedName);

  await page.getByTestId('my-list-add-game').click();
  const addedGameName = await addFirstSearchResult(page, 'zelda');
  await page.getByTestId('popup-close').click();

  const originalGameRow = page.locator('ul').filter({hasText: originalGameName});
  await originalGameRow.getByRole('button').click();

  const [updateResponse] = await Promise.all([
    page.waitForResponse(resp => resp.url().includes('/api/updateGamesList') && resp.request().method() === 'POST'),
    page.getByTestId('my-list-save').click(),
  ]);
  expect(updateResponse.ok()).toBeTruthy();

  await page.getByTestId('my-list-close').click();

  const updatedCard = gamesListCard(page, updatedName);
  await expect(updatedCard).toBeVisible();
  await expect(updatedCard.getByText(originalGameName, {exact: true})).toHaveCount(0);
  await expect(updatedCard.getByText(addedGameName, {exact: true})).toBeVisible();
});
