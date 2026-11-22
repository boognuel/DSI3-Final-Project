import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GuestGuard } from './guards/guest.guard';
import { Logincomponent } from './components/logincomponent/logincomponent';
import { Registrocomponent } from './components/registrocomponent/registrocomponent';
import { Verificaremailcomponent } from './components/verificaremailcomponent/verificaremailcomponent';
import { Olvidopasswordcomponent } from './components/olvidopasswordcomponent/olvidopasswordcomponent';
import { Restablecerpasswordcomponent } from './components/restablecerpasswordcomponent/restablecerpasswordcomponent';
import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';

/**
 * Rutas de la aplicación con su componente y su guarda.
 */
const routes: Routes = [
  { path: 'login', component: Logincomponent, canActivate: [GuestGuard] },
  { path: 'register', component: Registrocomponent, canActivate: [GuestGuard] },
  { path: 'verify-email', component: Verificaremailcomponent },
  { path: 'forgot-password', component: Olvidopasswordcomponent, canActivate: [GuestGuard] },
  { path: 'reset-password', component: Restablecerpasswordcomponent },
  { path: '', component: Iniciocomponent },
  { path: '**', redirectTo: '' },
];

/**
 * Módulo de enrutamiento: asocia cada ruta con su componente y su guarda.
 */
@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      anchorScrolling: 'enabled',
      scrollPositionRestoration: 'enabled',
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
