export interface RawgResultsDto {
  id?: number;
  slug?: string;
  name?: string;
  released?: string;
  tba?: boolean;
  background_image?: string;
}

export interface RawgResponseDto {
  count?: number;
  next?: string;
  previous?: string;
  results?: RawgResultsDto[];
}

export interface Game {
  id: number;
  name: string;
  slug: string;
  released: string;
  backgroundImage: string;
  rawgId: number;
}

export interface GameList {
  id: number;
  name: string;
  games: Game[];
}
