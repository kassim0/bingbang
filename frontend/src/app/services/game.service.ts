import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {RawgResponseDto, RawgResultsDto} from '../models/game.model';

@Injectable({
  providedIn: 'root'
})
export class GameService {

  private apiUrl = 'http://localhost:8080/api/games';

  constructor(private http: HttpClient) {}

  searchGamesByName(gameName: string): Observable<RawgResponseDto> {
    return this.http.get<RawgResponseDto>(`${this.apiUrl}/search/${gameName}`);
  }

  saveGame(game: RawgResultsDto): Observable<any> {
    return this.http.post(this.apiUrl, game);
  }
}
