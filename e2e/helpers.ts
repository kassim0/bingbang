import {Page} from '@playwright/test';

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
