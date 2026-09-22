import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NewGameList, RawgResponseDto, RawgResultsDto} from '../models/rawg.models';
import {GamesList, UpdateGamesList} from "../models/games.model";

@Injectable({providedIn: 'root'})
export class GameApiService {

  private base = '/api';

  constructor(private http: HttpClient) {}

  searchGames(name: string) {
    return this.http.get<RawgResponseDto>(`${this.base}/rawg/${name}`);
  }

  saveGamesList(newGameList:NewGameList) {
    return this.http.put<RawgResultsDto>(`${this.base}/newGamesList`, newGameList);
  }

  getGamesList() {
    return this.http.get<GamesList[]>(`${this.base}/getGamesLists`);
  }

  updateGamesList(updateGamesList : UpdateGamesList){
    return this.http.post(`${this.base}/updateGamesList`,updateGamesList, {responseType: 'text'});
  }

  deleteGamesList(id: number) {
    return this.http.delete(`${this.base}/deleteGamesList/${id}`, {responseType: 'text'});
  }
}
