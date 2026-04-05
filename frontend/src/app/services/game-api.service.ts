import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {RawgResponseDto, RawgResultsDto} from '../models/rawg.models';

@Injectable({providedIn: 'root'})
export class GameApiService {

  private base = '/api';

  constructor(private http: HttpClient) {}

  searchGames(name: string) {
    return this.http.get<RawgResponseDto>(`${this.base}/games/${name}`);
  }

  saveGame(game: RawgResultsDto) {
    return this.http.post<RawgResultsDto>(`${this.base}/games`, game);
  }
}
