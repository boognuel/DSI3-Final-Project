import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/authinterceptor';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Footercomponent } from './components/footercomponent/footercomponent';
import { Avisoubicacioncomponent } from './components/avisoubicacioncomponent/avisoubicacioncomponent';
import { ReactiveFormsModule } from '@angular/forms';
import { Logincomponent } from './components/logincomponent/logincomponent';
import { Registrocomponent } from './components/registrocomponent/registrocomponent';
import { Verificaremailcomponent } from './components/verificaremailcomponent/verificaremailcomponent';
import { Olvidopasswordcomponent } from './components/olvidopasswordcomponent/olvidopasswordcomponent';
import { Restablecerpasswordcomponent } from './components/restablecerpasswordcomponent/restablecerpasswordcomponent';
import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';

@NgModule({
  declarations: [
    App,
    Navbarcomponent,
    Footercomponent,
    Avisoubicacioncomponent,
    Logincomponent,
    Registrocomponent,
    Verificaremailcomponent,
    Olvidopasswordcomponent,
    Restablecerpasswordcomponent,
    Iniciocomponent,
  ],
  imports: [BrowserModule, AppRoutingModule, ReactiveFormsModule],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
  bootstrap: [App],
})
export class AppModule {}
