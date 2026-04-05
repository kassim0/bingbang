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
  results?: Array<RawgResultsDto>;
}
