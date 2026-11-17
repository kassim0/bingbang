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
  position : number;
  /** Commentaire de l'utilisateur sur ce jeu dans cette liste ; absent ou null s'il n'y en a pas. */
  commentary? : string | null;
}

export interface Commentary {
  gamesListId : number;
  gameId : number;
  text : string;
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
