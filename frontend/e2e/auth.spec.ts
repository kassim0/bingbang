import {test, expect} from '@playwright/test';
import {newTestAccount, openLoginForm, openRegisterForm, registerFromApi, registerFromUi, submitLoginForm} from './helpers';

test('shows the username in the navbar after creating an account', async ({page}) => {
  const account = newTestAccount();

  await registerFromUi(page, account);

  await expect(page.getByTestId('connected-username')).toContainText(account.username);
  await expect(page.getByTestId('register-submit')).toBeHidden();
});

test('shows the username in the navbar after logging in', async ({page, request}) => {
  const account = newTestAccount();
  await registerFromApi(request, account);

  await openLoginForm(page);
  await submitLoginForm(page, account.email, account.password);

  await expect(page.getByTestId('connected-username')).toContainText(account.username);
  await expect(page.getByTestId('login-submit')).toBeHidden();
});

test('keeps the username in the navbar after refreshing the page', async ({page}) => {
  const account = newTestAccount();
  await registerFromUi(page, account);
  await expect(page.getByTestId('connected-username')).toContainText(account.username);

  await page.reload();

  await expect(page.getByTestId('connected-username')).toContainText(account.username);
});

test('removes the username from the navbar after logging out', async ({page}) => {
  const account = newTestAccount();
  await registerFromUi(page, account);
  await expect(page.getByTestId('connected-username')).toContainText(account.username);

  await page.getByTestId('user-menu-trigger').click();
  await page.getByTestId('logout-button').click();

  await expect(page.getByTestId('connected-username')).toBeHidden();
  await expect(page.getByTestId('open-auth-dialog')).toBeVisible();

  // La déconnexion doit survivre au rechargement : le token ne doit plus être stocké.
  await page.reload();
  await expect(page.getByTestId('open-auth-dialog')).toBeVisible();
  await expect(page.getByTestId('connected-username')).toBeHidden();
});

test.describe('error messages in the auth dialog', () => {
  const INVALID_INPUT = 'Invalid email, or password not between 8 and 72 characters.';

  test('rejects an email already in use (409)', async ({page, request}) => {
    const account = newTestAccount();
    await registerFromApi(request, account);

    await registerFromUi(page, account);

    await expect(page.getByTestId('auth-error-message')).toHaveText('This email is already in use.');
    await expect(page.getByTestId('connected-username')).toBeHidden();
  });

  test('rejects a wrong password (401)', async ({page, request}) => {
    const account = newTestAccount();
    await registerFromApi(request, account);

    await openLoginForm(page);
    await submitLoginForm(page, account.email, 'wrong-password');

    await expect(page.getByTestId('auth-error-message')).toHaveText('Wrong email or password.');
    await expect(page.getByTestId('connected-username')).toBeHidden();
  });

  test('rejects an unknown email (401)', async ({page}) => {
    const account = newTestAccount();

    await openLoginForm(page);
    await submitLoginForm(page, account.email, account.password);

    await expect(page.getByTestId('auth-error-message')).toHaveText('Wrong email or password.');
  });

  test('rejects a password that is too short (400)', async ({page}) => {
    const account = {...newTestAccount(), password: 'short'};

    await registerFromUi(page, account);

    await expect(page.getByTestId('auth-error-message')).toHaveText(INVALID_INPUT);
  });

  test('rejects an invalid email (400)', async ({page}) => {
    const account = {...newTestAccount(), email: 'not-an-email'};

    await registerFromUi(page, account);

    await expect(page.getByTestId('auth-error-message')).toHaveText(INVALID_INPUT);
  });

  test('asks to fill in all fields of the register form', async ({page}) => {
    await openRegisterForm(page);
    await page.getByTestId('register-submit').click();

    await expect(page.getByTestId('auth-error-message')).toHaveText('Please fill in all fields.');
  });

  test('asks to fill in all fields of the login form', async ({page}) => {
    await openLoginForm(page);
    await page.getByTestId('login-submit').click();

    await expect(page.getByTestId('auth-error-message')).toHaveText('Please fill in all fields.');
  });

  test('allows a new attempt after an error', async ({page, request}) => {
    const account = newTestAccount();
    await registerFromApi(request, account);

    await openLoginForm(page);
    await submitLoginForm(page, account.email, 'wrong-password');
    await expect(page.getByTestId('auth-error-message')).toBeVisible();

    await submitLoginForm(page, account.email, account.password);

    await expect(page.getByTestId('connected-username')).toContainText(account.username);
  });

  test('clears the error when switching form', async ({page}) => {
    await openRegisterForm(page);
    await page.getByTestId('register-submit').click();
    await expect(page.getByTestId('auth-error-message')).toBeVisible();

    await page.getByTestId('switch-to-login').click();

    await expect(page.getByTestId('login-submit')).toBeVisible();
    await expect(page.getByTestId('auth-error-message')).toBeHidden();
  });
});
