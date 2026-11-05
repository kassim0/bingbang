export type GameSource = 'RAWG' | 'IGDB';

export interface GameSearchResult {
  source: GameSource;
  externalId: number;
  slug?: string;
  name: string;
  releaseDate?: string;
  imageUrl?: string;
}

export interface NewGameList {
  name: string;
  games: GameSearchResult[];
}
