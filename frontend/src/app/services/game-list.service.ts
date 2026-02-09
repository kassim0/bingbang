import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {GameList, RawgResultsDto} from '../models/game.model';

@Injectable({
  providedIn: 'root'
})
export class GameListService {

  private apiUrl = 'http://localhost:8080/api/lists';

  constructor(private http: HttpClient) {}

  createList(name: string): Observable<GameList> {
    return this.http.post<GameList>(this.apiUrl, {name});
  }

  getAllLists(): Observable<GameList[]> {
    return this.http.get<GameList[]>(this.apiUrl);
  }

  addGameToList(listId: number, game: RawgResultsDto): Observable<GameList> {
    return this.http.post<GameList>(`${this.apiUrl}/${listId}/games`, game);
  }
}
