import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req,next)=> {
  const token = localStorage.getItem('token');
  req.headers.set('Authorization', 'Bearer ' + token);
  return next(req)
}
