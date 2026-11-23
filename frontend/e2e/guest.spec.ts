import {test, expect} from '@playwright/test';
import {createGamesList, gamesListCard, newTestAccount, openLoginForm, registerFromApi, registerFromUi, storedToken,
  submitLoginForm, waitForGuest} from './helpers';

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

  test('a guest keeps its games lists after creating a real account', async ({page}) => {
    const listName = `E2E Guest to account ${Date.now()}`;
    const account = newTestAccount();
    await page.goto('/');
    await waitForGuest(page);
    await createGamesList(page, listName, ['zelda']);
    await expect(gamesListCard(page, listName)).toBeVisible();

    await registerFromUi(page, account);
    await expect(page.getByTestId('connected-username')).toContainText(account.username);

    await expect(gamesListCard(page, listName)).toBeVisible();
    // la liste est bien rattachée au compte côté serveur, pas seulement encore affichée
    await page.reload();
    await expect(page.getByTestId('connected-username')).toContainText(account.username);
    await expect(gamesListCard(page, listName)).toBeVisible();
  });

  test('a guest keeps its games lists after logging in to an existing account', async ({page, request}) => {
    const listName = `E2E Guest to existing account ${Date.now()}`;
    const account = newTestAccount();
    await registerFromApi(request, account);
    await page.goto('/');
    await waitForGuest(page);
    await createGamesList(page, listName, ['zelda']);
    await expect(gamesListCard(page, listName)).toBeVisible();

    await openLoginForm(page);
    await submitLoginForm(page, account.email, account.password);
    await expect(page.getByTestId('connected-username')).toContainText(account.username);

    await expect(gamesListCard(page, listName)).toBeVisible();
    // la liste est bien rattachée au compte côté serveur, pas seulement encore affichée
    await page.reload();
    await expect(page.getByTestId('connected-username')).toContainText(account.username);
    await expect(gamesListCard(page, listName)).toBeVisible();
  });

  /** Phase 0d : un /guest lent ne doit pas remplacer le compte auquel l'utilisateur s'est connecté entre-temps. */
  test('a late /guest response does not replace an account logged in meanwhile', async ({page, request}) => {
    const account = newTestAccount();
    await registerFromApi(request, account);

    // On retient la requête /guest jusqu'à ce que la connexion soit terminée
    let releaseGuest!: () => void;
    const guestReleased = new Promise<void>(resolve => releaseGuest = resolve);
    await page.route('**/api/auth/guest', async route => {
      await guestReleased;
      await route.continue();
    });

    await page.goto('/');
    // Pas de openLoginForm ici : il attend l'invité, qui est justement bloqué
    await page.getByTestId('open-auth-dialog').click();
    await expect(page.getByTestId('username-register-input')).toBeFocused();
    await page.getByTestId('switch-to-login').click();
    await submitLoginForm(page, account.email, account.password);
    await expect(page.getByTestId('connected-username')).toContainText(account.username);
    const accountToken = await storedToken(page);

    // La réponse de /guest arrive maintenant, après la connexion
    const guestResponse = page.waitForResponse('**/api/auth/guest');
    releaseGuest();
    await (await guestResponse).finished();
    // Laisse à l'app le temps de traiter la réponse : sans ce délai, les vérifications
    // pourraient passer avant que le bug (s'il revient) ait eu lieu
    await page.waitForTimeout(300);

    await expect(page.getByTestId('connected-username')).toContainText(account.username);
    await expect(page.getByTestId('guest-label')).toBeHidden();
    expect(await storedToken(page)).toBe(accountToken);

    // Le compte est toujours là après rechargement : c'est bien son token qui est resté stocké
    await page.unroute('**/api/auth/guest');
    await page.reload();
    await expect(page.getByTestId('connected-username')).toContainText(account.username);
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
