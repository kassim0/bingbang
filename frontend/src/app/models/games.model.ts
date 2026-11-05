import {GameSearchResult} from "./game-search.models";

export interface Game{
  id: number;
  slug?: string;
  name: string;
  releaseDate: string;
  backgroundImage: string;
  rawgId: number | null;
  igdbId: number | null;
}

export interface GamesList {
  id: number;
  name: string;
  position : number;
  games : GamesListEntry[];
}

export interface GamesListEntry {
  id: number;
  game : Game;
  gamesList : GamesList;
  position : number;
}

export interface UpdateGamesList{
  gamesListId : number;
  newGames : GameSearchResult[] | null;
  removeGameId : number [] | null;
  newName : string | null;
}

/** Résultat renvoyé par GamesSearchPopupComponent à sa fermeture. */
export interface GamesSearchPopupResult {
  addedGames : GameSearchResult[];
  listName : string;
}
