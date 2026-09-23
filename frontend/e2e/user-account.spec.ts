import {test, expect, Page} from '@playwright/test';
import {createGamesList, gamesListCard} from './helpers';

const TOKEN_KEY = 'bingbang_token';
const USER_KEY = 'bingbang_user';
const PASSWORD = 'e2e-password-123';

interface Credentials {
  username: string;
  email: string;
  password: string;
}

/** Identifiants uniques par appel : les tests tournent en parallèle sur une même base. */
function uniqueCredentials(prefix: string): Credentials {
  const suffix = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  return {
    username: `${prefix}-${suffix}`,
    email: `${prefix}-${suffix}@e2e.test`,
    password: PASSWORD,
  };
}

/** Crée un compte directement via l'API (sans passer par l'UI) pour préparer un test. */
async function registerViaApi(page: Page, credentials: Credentials) {
  const response = await page.request.post('/api/auth/register', {data: credentials});
  expect(response.ok()).toBeTruthy();
}

/** Ouvre la home et attend que la session (invitée ou non) soit prête : la dashboard est alors affichée. */
async function openHomeWithSession(page: Page) {
  await page.goto('/');
  await expect(page.getByTestId('open-search-popup')).toBeVisible();
}

async function readStoredUser(page: Page) {
  return page.evaluate(key => JSON.parse(localStorage.getItem(key) ?? 'null'), USER_KEY);
}

async function openAuthDialog(page: Page, mode: 'login' | 'register') {
  await page.getByTestId('open-auth-dialog').click();
  if (mode === 'register') {
    await page.getByTestId('auth-toggle-mode').click();
    await expect(page.getByTestId('auth-username-input')).toBeVisible();
  }
}

async function registerViaUi(page: Page, credentials: Credentials) {
  await openAuthDialog(page, 'register');
  await page.getByTestId('auth-username-input').fill(credentials.username);
  await page.getByTestId('auth-email-input').fill(credentials.email);
  await page.getByTestId('auth-password-input').fill(credentials.password);
  await page.getByTestId('auth-submit').click();
}

async function loginViaUi(page: Page, email: string, password: string) {
  await openAuthDialog(page, 'login');
  await page.getByTestId('auth-email-input').fill(email);
  await page.getByTestId('auth-password-input').fill(password);
  await page.getByTestId('auth-submit').click();
}

async function logoutViaUi(page: Page) {
  await page.getByTestId('user-menu').click();
  await page.getByTestId('logout-button').click();
}

async function expectLoggedInAs(page: Page, credentials: Credentials) {
  await expect(page.getByTestId('user-menu')).toBeVisible();
  await expect(page.getByTestId('open-auth-dialog')).toHaveCount(0);
  const user = await readStoredUser(page);
  expect(user).toMatchObject({username: credentials.username, email: credentials.email, guest: false});
}

test.describe('compte invité', () => {

  test('crée automatiquement un compte invité pour un nouveau visiteur', async ({page}) => {
    const guestResponse = page.waitForResponse(resp =>
      resp.url().includes('/api/auth/guest') && resp.request().method() === 'POST');

    await page.goto('/');

    expect((await guestResponse).ok()).toBeTruthy();
    await expect(page.getByTestId('open-search-popup')).toBeVisible();
    // Un invité doit toujours pouvoir se connecter / créer un vrai compte.
    await expect(page.getByTestId('open-auth-dialog')).toBeVisible();

    const token = await page.evaluate(key => localStorage.getItem(key), TOKEN_KEY);
    expect(token).toBeTruthy();
    const user = await readStoredUser(page);
    expect(user.guest).toBe(true);
    expect(user.username).toMatch(/^Invité-/);
  });

  test('réutilise le même compte invité et ses listes après rechargement', async ({page}) => {
    const listName = `E2E Guest ${Date.now()}`;
    await openHomeWithSession(page);
    const guestBefore = await readStoredUser(page);

    await createGamesList(page, listName, ['zelda']);
    await expect(gamesListCard(page, listName)).toBeVisible();

    let guestRecreated = false;
    page.on('request', req => {
      if (req.url().includes('/api/auth/guest')) {
        guestRecreated = true;
      }
    });

    await page.reload();

    await expect(gamesListCard(page, listName)).toBeVisible();
    expect(await readStoredUser(page)).toEqual(guestBefore);
    expect(guestRecreated).toBe(false);
  });

  test('repart sur un nouveau compte invité si le token stocké est invalide', async ({page}) => {
    await page.addInitScript(key => {
      // Uniquement au premier chargement : sinon le token invalide serait réinjecté à chaque navigation.
      if (!sessionStorage.getItem('e2e-invalid-token-injected')) {
        sessionStorage.setItem('e2e-invalid-token-injected', '1');
        localStorage.setItem(key, 'token-invalide');
      }
    }, TOKEN_KEY);

    const meResponse = page.waitForResponse(resp => resp.url().includes('/api/auth/me'));
    const guestResponse = page.waitForResponse(resp =>
      resp.url().includes('/api/auth/guest') && resp.request().method() === 'POST');

    await page.goto('/');

    expect((await meResponse).ok()).toBeFalsy();
    expect((await guestResponse).ok()).toBeTruthy();
    await expect(page.getByTestId('open-search-popup')).toBeVisible();

    const token = await page.evaluate(key => localStorage.getItem(key), TOKEN_KEY);
    expect(token).not.toBe('token-invalide');
    expect((await readStoredUser(page)).guest).toBe(true);
  });
});

test.describe('inscription', () => {

  test('un invité peut créer un vrai compte', async ({page}) => {
    const credentials = uniqueCredentials('e2e-register');
    await openHomeWithSession(page);

    await registerViaUi(page, credentials);

    await expect(page.getByTestId('auth-submit')).toHaveCount(0);
    await expectLoggedInAs(page, credentials);
    await expect(page.getByTestId('open-search-popup')).toBeVisible();
  });

  test('refuse un email déjà utilisé', async ({page}) => {
    const existing = uniqueCredentials('e2e-existing');
    await openHomeWithSession(page);
    await registerViaApi(page, existing);

    await registerViaUi(page, {...uniqueCredentials('e2e-dup'), email: existing.email});

    await expect(page.getByText('Email already in use')).toBeVisible();
    await expect(page.getByTestId('auth-submit')).toBeVisible();
    expect((await readStoredUser(page)).guest).toBe(true);
  });

  test('refuse un nom d\'utilisateur déjà utilisé', async ({page}) => {
    const existing = uniqueCredentials('e2e-existing');
    await openHomeWithSession(page);
    await registerViaApi(page, existing);

    await registerViaUi(page, {...uniqueCredentials('e2e-dup'), username: existing.username});

    await expect(page.getByText('Username already in use')).toBeVisible();
    expect((await readStoredUser(page)).guest).toBe(true);
  });
});

test.describe('connexion / déconnexion', () => {

  test('se connecte avec un compte existant', async ({page}) => {
    const credentials = uniqueCredentials('e2e-login');
    await openHomeWithSession(page);
    await registerViaApi(page, credentials);

    await loginViaUi(page, credentials.email, credentials.password);

    await expect(page.getByTestId('auth-submit')).toHaveCount(0);
    await expectLoggedInAs(page, credentials);
  });

  test('affiche une erreur avec un mauvais mot de passe', async ({page}) => {
    const credentials = uniqueCredentials('e2e-badpwd');
    await openHomeWithSession(page);
    await registerViaApi(page, credentials);

    await loginViaUi(page, credentials.email, 'mauvais-mot-de-passe');

    await expect(page.getByText('Email ou mot de passe incorrect.')).toBeVisible();
    await expect(page.getByTestId('user-menu')).toHaveCount(0);
    expect((await readStoredUser(page)).guest).toBe(true);
  });

  test('la connexion survit à un rechargement de page', async ({page}) => {
    const credentials = uniqueCredentials('e2e-persist');
    await openHomeWithSession(page);
    await registerViaApi(page, credentials);
    await loginViaUi(page, credentials.email, credentials.password);
    await expectLoggedInAs(page, credentials);

    await page.reload();

    await expectLoggedInAs(page, credentials);
  });

  test('la déconnexion vide la session et masque la dashboard', async ({page}) => {
    const credentials = uniqueCredentials('e2e-logout');
    await openHomeWithSession(page);
    await registerViaApi(page, credentials);
    await loginViaUi(page, credentials.email, credentials.password);
    await expectLoggedInAs(page, credentials);

    await logoutViaUi(page);

    await expect(page.getByTestId('login-required-message')).toBeVisible();
    await expect(page.getByTestId('open-search-popup')).toHaveCount(0);
    await expect(page.getByTestId('open-auth-dialog')).toBeVisible();
    const storage = await page.evaluate(([tokenKey, userKey]) => ({
      token: localStorage.getItem(tokenKey),
      user: localStorage.getItem(userKey),
    }), [TOKEN_KEY, USER_KEY]);
    expect(storage).toEqual({token: null, user: null});
  });
});

test.describe('listes rattachées au compte', () => {

  test('chaque utilisateur ne voit que ses propres listes', async ({page}) => {
    const alice = uniqueCredentials('e2e-alice');
    const bob = uniqueCredentials('e2e-bob');
    const aliceList = `E2E Alice ${Date.now()}`;

    await openHomeWithSession(page);
    await registerViaApi(page, alice);
    await registerViaApi(page, bob);

    await loginViaUi(page, alice.email, alice.password);
    await expectLoggedInAs(page, alice);
    await createGamesList(page, aliceList, ['zelda']);
    await expect(gamesListCard(page, aliceList)).toBeVisible();

    await logoutViaUi(page);
    await loginViaUi(page, bob.email, bob.password);
    await expectLoggedInAs(page, bob);
    await expect(page.getByTestId('open-search-popup')).toBeVisible();
    await expect(gamesListCard(page, aliceList)).toHaveCount(0);

    await logoutViaUi(page);
    await loginViaUi(page, alice.email, alice.password);
    await expectLoggedInAs(page, alice);
    await expect(gamesListCard(page, aliceList)).toBeVisible();
  });

  test('les listes d\'un invité ne sont pas visibles par un autre visiteur', async ({page, browser}) => {
    const guestList = `E2E Guest Private ${Date.now()}`;
    await openHomeWithSession(page);
    await createGamesList(page, guestList, ['mario']);
    await expect(gamesListCard(page, guestList)).toBeVisible();

    const otherContext = await browser.newContext();
    try {
      const otherPage = await otherContext.newPage();
      await openHomeWithSession(otherPage);
      await expect(gamesListCard(otherPage, guestList)).toHaveCount(0);
    } finally {
      await otherContext.close();
    }
  });
});
