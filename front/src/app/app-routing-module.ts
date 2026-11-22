import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { GuestGuard } from './guards/guest.guard';
import { Logincomponent } from './components/logincomponent/logincomponent';
import { Registrocomponent } from './components/registrocomponent/registrocomponent';
import { Verificaremailcomponent } from './components/verificaremailcomponent/verificaremailcomponent';
import { Olvidopasswordcomponent } from './components/olvidopasswordcomponent/olvidopasswordcomponent';
import { Restablecerpasswordcomponent } from './components/restablecerpasswordcomponent/restablecerpasswordcomponent';
import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';
import { Buscadorcomponent } from './components/buscadorcomponent/buscadorcomponent';
import { Salidascomponent } from './components/salidascomponent/salidascomponent';
import { Salidaformcomponent } from './components/salidaformcomponent/salidaformcomponent';
import { Salidadetallecomponent } from './components/salidadetallecomponent/salidadetallecomponent';
import { Invitacioncomponent } from './components/invitacioncomponent/invitacioncomponent';
import { Rutacomponent } from './components/rutacomponent/rutacomponent';

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
  { path: 'planes', component: Buscadorcomponent, canActivate: [AuthGuard] },
  { path: 'salidas', component: Salidascomponent, canActivate: [AuthGuard] },
  { path: 'salidas/nueva', component: Salidaformcomponent, canActivate: [AuthGuard] },
  { path: 'salidas/editar/:id', component: Salidaformcomponent, canActivate: [AuthGuard] },
  { path: 'salidas/:id', component: Salidadetallecomponent, canActivate: [AuthGuard] },
  { path: 'invitacion', component: Invitacioncomponent },
  { path: 'ruta/:salidaId/:lugarId', component: Rutacomponent, canActivate: [AuthGuard] },
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
