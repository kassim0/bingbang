import {RawgResultsDto} from "./rawg.models";

export interface Game{
  id: number;
  slug?: string;
  name: string;
  releaseDate: string;
  backgroundImage: string;
  rawgId: string;
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
  newRawgGames : RawgResultsDto[] | null;
  removeGameId : number [] | null;
  newName : string | null;
}

/** Résultat renvoyé par GamesSearchPopupComponent à sa fermeture. */
export interface GamesSearchPopupResult {
  addedGames : RawgResultsDto[];
  listName : string;
}
