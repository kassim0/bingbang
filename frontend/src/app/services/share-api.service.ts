import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CreateShareLink, SharedWallet, ShareLink} from '../models/share.model';

@Injectable({providedIn: 'root'})
export class ShareApiService {

  private base = '/api/share';

  constructor(private http: HttpClient) {}

  create(request: CreateShareLink) {
    return this.http.post<ShareLink>(this.base, request);
  }

  findMine() {
    return this.http.get<ShareLink[]>(this.base);
  }

  /** Révoque le lien : son adresse ne mène plus à rien. */
  revoke(linkId: number) {
    return this.http.delete<void>(`${this.base}/${linkId}`);
  }

  /** Contenu d'un lien de partage, pour la page /share/:token. */
  view(token: string) {
    return this.http.get<SharedWallet>(`${this.base}/view/${encodeURIComponent(token)}`);
  }
}
