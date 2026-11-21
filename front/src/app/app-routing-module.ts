import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Logincomponent } from './components/logincomponent/logincomponent';
import { Registrocomponent } from './components/registrocomponent/registrocomponent';

const routes: Routes = [
  // Temporal: la página de inicio llega con front-recommendations
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: Logincomponent },
  { path: 'register', component: Registrocomponent },
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
