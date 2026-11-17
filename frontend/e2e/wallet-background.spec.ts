import {test, expect} from '@playwright/test';
import {signInWithNewAccount, waitForGuest} from './helpers';

/** ROADMAP phase 3 : fond personnel du gamesWallet. */

const body = 'body';

test('the chosen background is applied and is still there after a reload', async ({page}) => {
  await signInWithNewAccount(page);
  await expect(page.locator(body)).toHaveAttribute('data-bb-background', 'COSMIC');

  await page.getByTestId('background-picker-trigger').click();
  await page.getByTestId('background-option-AURORA').click();
  await expect(page.locator(body)).toHaveAttribute('data-bb-background', 'AURORA');

  await page.reload();

  await expect(page.locator(body)).toHaveAttribute('data-bb-background', 'AURORA');
});

test('a guest can choose a background too', async ({page}) => {
  await page.goto('/');
  await waitForGuest(page);

  await page.getByTestId('background-picker-trigger').click();
  await page.getByTestId('background-option-SUNSET').click();
  await page.reload();
  await waitForGuest(page);

  await expect(page.locator(body)).toHaveAttribute('data-bb-background', 'SUNSET');
});
