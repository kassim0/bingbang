import {randomUUID} from 'node:crypto';
import {APIRequestContext, expect, Page} from '@playwright/test';

export interface TestAccount {
  username: string;
  email: string;
  password: string;
}

/** Génère un compte unique, pour que les tests lancés en parallèle ne se gênent pas (email déjà utilisé -> 409). */
export function newTestAccount(): TestAccount {
  const id = randomUUID();
  return {
    username: `e2e-${id.slice(0, 8)}`,
    email: `e2e-${id}@test.com`,
    password: 'password123',
  };
}

/**
 * Ouvre la popup d'authentification, qui s'ouvre sur le formulaire d'inscription.
 * MatDialog donne le focus au premier champ à la fin de son animation : on l'attend, sinon
 * ce focus peut arriver pendant la saisie d'un autre champ et le texte part dans le premier.
 */
export async function openRegisterForm(page: Page): Promise<void> {
  await page.goto('/');
  await page.getByTestId('open-auth-dialog').click();
  await expect(page.getByTestId('username-register-input')).toBeFocused();
}

/** Crée un compte depuis la popup d'authentification. */
export async function registerFromUi(page: Page, account: TestAccount): Promise<void> {
  await openRegisterForm(page);
  await page.getByTestId('username-register-input').fill(account.username);
  await page.getByTestId('email-register-input').fill(account.email);
  await page.getByTestId('password-register-input').fill(account.password);
  await page.getByTestId('register-submit').click();
}

/** Crée un compte directement via l'API, pour les tests dont la création de compte n'est pas le sujet. */
export async function registerFromApi(request: APIRequestContext, account: TestAccount): Promise<void> {
  const response = await request.post('/api/auth/register', {data: account});
  expect(response.ok()).toBeTruthy();
}

/**
 * Crée un compte via l'API et retourne son token, pour appeler directement les routes protégées
 * (`Authorization: Bearer <token>`) sans passer par l'interface.
 */
export async function registerAndGetToken(request: APIRequestContext, account: TestAccount = newTestAccount()): Promise<string> {
  const response = await request.post('/api/auth/register', {data: account});
  expect(response.ok()).toBeTruthy();
  return (await response.json()).token;
}

/**
 * Connecte un nouveau compte depuis l'interface : la home n'affiche les listes qu'à un utilisateur connecté.
 * Attend que le pseudo apparaisse dans le navbar, preuve que la connexion est terminée.
 */
export async function signInWithNewAccount(page: Page): Promise<TestAccount> {
  const account = newTestAccount();
  await registerFromUi(page, account);
  await expect(page.getByTestId('connected-username')).toContainText(account.username);
  return account;
}

/** Ouvre la popup d'authentification sur le formulaire de connexion. */
export async function openLoginForm(page: Page): Promise<void> {
  await openRegisterForm(page);
  await page.getByTestId('switch-to-login').click();
}

/** Remplit et envoie le formulaire de connexion, déjà ouvert. */
export async function submitLoginForm(page: Page, email: string, password: string): Promise<void> {
  await page.getByTestId('email-login-input').fill(email);
  await page.getByTestId('password-login-input').fill(password);
  await page.getByTestId('login-submit').click();
}

/**
 * Recherche `searchTerm` dans la popup ouverte, ajoute le premier résultat
 * et retourne son nom (utile pour vérifier ensuite qu'il apparaît bien où attendu).
 */
export async function addFirstSearchResult(page: Page, searchTerm: string): Promise<string> {
  await page.getByTestId('search-bar-input').fill(searchTerm);
  await page.getByTestId('search-bar-input').press('Enter');

  const firstItem = page.locator('.game-item-container').first();
  await firstItem.waitFor({state: 'visible'});
  const name = (await firstItem.locator('.game-name').textContent())?.trim() ?? '';
  await firstItem.getByTestId('game-item-add').click();

  return name;
}

/**
 * Crée une nouvelle games list depuis la home : ouvre la popup, saisit le nom,
 * ajoute le premier résultat pour chaque terme de recherche fourni, puis ferme la popup.
 * Retourne les noms des jeux ajoutés, dans l'ordre.
 * Un utilisateur doit déjà être connecté (voir signInWithNewAccount).
 */
export async function createGamesList(page: Page, listName: string, searchTerms: string[]): Promise<string[]> {
  await page.goto('/');
  await page.getByTestId('open-search-popup').click();
  await page.getByTestId('list-name-input').fill(listName);

  const addedNames: string[] = [];
  for (const term of searchTerms) {
    addedNames.push(await addFirstSearchResult(page, term));
  }

  await page.getByTestId('popup-close').click();

  return addedNames;
}

/** Locator de la carte de la dashboard portant le nom de liste donné. */
export function gamesListCard(page: Page, listName: string) {
  return page.locator('[data-testid^="games-list-card-"]')
    .filter({has: page.getByRole('heading', {name: listName})});
}
