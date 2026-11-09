import {test, expect} from '@playwright/test';
import {createGamesList, gamesListCard, registerFromUi, newTestAccount, storedToken, waitForGuest} from './helpers';

/** Phase 3 : un visiteur sans compte devient automatiquement un invité, qui peut déjà créer des listes. */

test.describe('guest account in the interface', () => {
  test('a visitor without account becomes a guest automatically', async ({page}) => {
    await page.goto('/');

    await waitForGuest(page);
    expect(await storedToken(page)).toBeTruthy();
    // Un invité doit pouvoir créer un vrai compte à tout moment, mais n'a pas de déconnexion
    await expect(page.getByTestId('open-auth-dialog')).toBeVisible();
    await expect(page.getByTestId('user-menu-trigger')).toBeHidden();
    await expect(page.getByTestId('connected-username')).toBeHidden();
    // La home est utilisable sans inscription
    await expect(page.getByTestId('open-search-popup')).toBeVisible();
  });

  test('a guest keeps the same account after reloading the page', async ({page}) => {
    await page.goto('/');
    await waitForGuest(page);
    const token = await storedToken(page);

    await page.reload();

    await waitForGuest(page);
    expect(await storedToken(page)).toBe(token);
  });

  test('a guest finds its games list again after reloading the page', async ({page}) => {
    const listName = `E2E Guest ${Date.now()}`;
    await page.goto('/');
    await waitForGuest(page);

    await createGamesList(page, listName, ['zelda']);
    await expect(gamesListCard(page, listName)).toBeVisible();

    await page.reload();

    await waitForGuest(page);
    await expect(gamesListCard(page, listName)).toBeVisible();
  });

  test('a guest can create a real account', async ({page}) => {
    const account = newTestAccount();

    await registerFromUi(page, account);

    await expect(page.getByTestId('connected-username')).toContainText(account.username);
    await expect(page.getByTestId('guest-label')).toBeHidden();
    await expect(page.getByTestId('user-menu-trigger')).toBeVisible();
  });
});

test.describe('guest API', () => {
  test('creates a guest account with a token usable on /me', async ({request}) => {
    const response = await request.post('/api/auth/guest');
    expect(response.ok()).toBeTruthy();
    const {token, user} = await response.json();
    expect(user.guest).toBe(true);
    expect(user.email).toMatch(/@bingbang\.invalid$/);

    const me = await request.get('/api/auth/me', {headers: {Authorization: `Bearer ${token}`}});

    expect(me.ok()).toBeTruthy();
    expect((await me.json()).guest).toBe(true);
  });

  test('creates a different account on each call', async ({request}) => {
    const first = await (await request.post('/api/auth/guest')).json();
    const second = await (await request.post('/api/auth/guest')).json();

    expect(first.user.email).not.toBe(second.user.email);
    expect(first.token).not.toBe(second.token);
  });
});
