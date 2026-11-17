import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {GameSearchResult, NewGameList} from '../models/game-search.models';
import {Commentary, GamesList, UpdateGamesList} from "../models/games.model";

@Injectable({providedIn: 'root'})
export class GameApiService {

  private base = '/api';

  constructor(private http: HttpClient) {}

  searchGames(name: string) {
    return this.http.get<GameSearchResult[]>(`${this.base}/games/search`, {params: {name}});
  }

  saveGamesList(newGameList:NewGameList) {
    return this.http.put<GamesList>(`${this.base}/game/newGamesList`, newGameList);
  }

  getGamesList() {
    return this.http.get<GamesList[]>(`${this.base}/game/getGamesLists`);
  }

  updateGamesList(updateGamesList : UpdateGamesList){
    return this.http.post(`${this.base}/game/updateGamesList`,updateGamesList, {responseType: 'text'});
  }

  deleteGamesList(id: number) {
    return this.http.delete(`${this.base}/game/deleteGamesList/${id}`, {responseType: 'text'});
  }

  // ---------- commentaires ----------

  private commentaryUrl(gamesListId: number, gameId: number) {
    return `${this.base}/game/gamesList/${gamesListId}/games/${gameId}/commentary`;
  }

  /** Crée le commentaire, ou remplace son texte s'il existe déjà. */
  saveCommentary(gamesListId: number, gameId: number, text: string) {
    return this.http.put<Commentary>(this.commentaryUrl(gamesListId, gameId), {text});
  }

  deleteCommentary(gamesListId: number, gameId: number) {
    return this.http.delete<void>(this.commentaryUrl(gamesListId, gameId));
  }

  /**
   * Copie le commentaire vers une autre liste. Sans `replace`, le serveur répond 409 si la liste cible
   * a déjà un commentaire sur ce jeu.
   */
  copyCommentary(gamesListId: number, gameId: number, targetListId: number, replace = false) {
    return this.http.post<Commentary>(`${this.commentaryUrl(gamesListId, gameId)}/transfer`, {targetListId, replace});
  }
}
