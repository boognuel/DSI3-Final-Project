import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Authservice } from '../services/authservice';

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(Authservice);
  const router = inject(Router);
  return auth.logueado()
    ? true
    : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(Authservice);
  const router = inject(Router);
  return auth.esAdmin() ? true : router.createUrlTree(['/']);
};

export const guestGuard: CanActivateFn = () => {
  const auth = inject(Authservice);
  const router = inject(Router);
  return auth.logueado() ? router.createUrlTree(['/']) : true;
};
