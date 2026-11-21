import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { guestGuard } from './guards/authguard';
import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';
import { Logincomponent } from './components/logincomponent/logincomponent';
import { Registrocomponent } from './components/registrocomponent/registrocomponent';
import { Verificaremailcomponent } from './components/verificaremailcomponent/verificaremailcomponent';
import { Olvidopasswordcomponent } from './components/olvidopasswordcomponent/olvidopasswordcomponent';
import { Restablecerpasswordcomponent } from './components/restablecerpasswordcomponent/restablecerpasswordcomponent';

const routes: Routes = [
  { path: '', component: Iniciocomponent },
  { path: 'login', component: Logincomponent, canActivate: [guestGuard] },
  { path: 'register', component: Registrocomponent, canActivate: [guestGuard] },
  { path: 'verify-email', component: Verificaremailcomponent },
  { path: 'forgot-password', component: Olvidopasswordcomponent, canActivate: [guestGuard] },
  { path: 'reset-password', component: Restablecerpasswordcomponent },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      anchorScrolling: 'enabled',
      scrollPositionRestoration: 'enabled',
    }),
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
