import {test, expect, APIRequestContext} from '@playwright/test';
import {registerAndGetToken} from './helpers';

/**
 * Validation de la phase 2 : avec deux comptes A et B, B ne peut ni voir, ni modifier,
 * ni supprimer une liste créée par A. Les appels passent directement par l'API, chacun avec son token.
 */

const auth = (token: string) => ({Authorization: `Bearer ${token}`});

async function createList(request: APIRequestContext, token: string, name: string): Promise<number> {
  const response = await request.put('/api/game/newGamesList', {headers: auth(token), data: {name, games: []}});
  expect(response.ok()).toBeTruthy();
  return (await response.json()).id;
}

async function listNames(request: APIRequestContext, token: string): Promise<string[]> {
  const response = await request.get('/api/game/getGamesLists', {headers: auth(token)});
  expect(response.ok()).toBeTruthy();
  return (await response.json()).map((list: {name: string}) => list.name);
}

test.describe('games lists belong to their account', () => {
  let tokenA: string;
  let tokenB: string;
  let listName: string;
  let listId: number;

  test.beforeEach(async ({request}) => {
    tokenA = await registerAndGetToken(request);
    tokenB = await registerAndGetToken(request);
    listName = `E2E Ownership ${Date.now()}`;
    listId = await createList(request, tokenA, listName);
  });

  test('B does not see the list of A', async ({request}) => {
    expect(await listNames(request, tokenA)).toContain(listName);
    expect(await listNames(request, tokenB)).not.toContain(listName);
  });

  test('B cannot modify the list of A (404)', async ({request}) => {
    const response = await request.post('/api/game/updateGamesList', {
      headers: auth(tokenB),
      data: {gamesListId: listId, newName: 'hacked'},
    });

    expect(response.status()).toBe(404);
    expect(await listNames(request, tokenA)).toContain(listName);
  });

  test('B cannot delete the list of A (404)', async ({request}) => {
    const response = await request.delete(`/api/game/deleteGamesList/${listId}`, {headers: auth(tokenB)});

    expect(response.status()).toBe(404);
    expect(await listNames(request, tokenA)).toContain(listName);
  });

  test('A can delete its own list', async ({request}) => {
    const response = await request.delete(`/api/game/deleteGamesList/${listId}`, {headers: auth(tokenA)});

    expect(response.ok()).toBeTruthy();
    expect(await listNames(request, tokenA)).not.toContain(listName);
  });

  test('a request without token is rejected', async ({request}) => {
    const response = await request.get('/api/game/getGamesLists');

    expect([401, 403]).toContain(response.status());
  });
});
