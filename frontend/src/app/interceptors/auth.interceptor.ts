import {HttpInterceptorFn} from '@angular/common/http';
import {inject, PLATFORM_ID} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import {TOKEN_KEY} from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Lit le storage directement plutôt que d'injecter AuthService : sinon, un appel HTTP lancé depuis
  // le constructeur d'AuthService (bootstrap invité, revalidation du token) crée une dépendance
  // circulaire (NG0200) puisqu'AuthService n'a pas fini de se construire.
  const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  const token = isBrowser ? localStorage.getItem(TOKEN_KEY) : null;

  if (!token) {
    return next(req);
  }

  return next(req.clone({
    setHeaders: {Authorization: `Bearer ${token}`}
  }));
};
