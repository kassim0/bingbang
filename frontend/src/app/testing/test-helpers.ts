import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {Game, GamesList} from '../models/games.model';
import {GameSearchResult} from '../models/game-search.models';

/**
 * Providers communs aux tests de composants :
 * - un faux HttpClient (aucune vraie requête ne part, on répond avec HttpTestingController) ;
 * - des animations désactivées (Angular Material en a besoin pour s'afficher).
 */
export const commonTestProviders = [
  provideHttpClient(),
  provideHttpClientTesting(),
  provideNoopAnimations(),
];

export function aGame(id: number, name: string): Game {
  return {
    id,
    name,
    slug: name.toLowerCase().replace(/\s+/g, '-'),
    releaseDate: '2020-01-01',
    backgroundImage: `https://images.igdb.com/igdb/image/upload/t_cover_big/${id}.jpg`,
    rawgId: null,
    igdbId: id,
  };
}

/** Une liste dont les jeux portent les noms donnés, dans l'ordre. */
export function aGamesList(id: number, name: string, gameNames: string[] = []): GamesList {
  return {
    id,
    name,
    position: id,
    games: gameNames.map((gameName, index) => ({id: id * 100 + index, position: index, game: aGame(id * 100 + index, gameName)})),
  };
}

export function aSearchResult(externalId: number, name: string): GameSearchResult {
  return {source: 'IGDB', externalId, name, slug: name.toLowerCase(), releaseDate: '2020-01-01', imageUrl: 'https://images.igdb.com/x.jpg'};
}
