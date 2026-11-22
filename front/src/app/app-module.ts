import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Footercomponent } from './components/footercomponent/footercomponent';
import { Logincomponent } from './components/logincomponent/logincomponent';
import { Registrocomponent } from './components/registrocomponent/registrocomponent';
import { Verificaremailcomponent } from './components/verificaremailcomponent/verificaremailcomponent';
import { Olvidopasswordcomponent } from './components/olvidopasswordcomponent/olvidopasswordcomponent';
import { Restablecerpasswordcomponent } from './components/restablecerpasswordcomponent/restablecerpasswordcomponent';
import { Avisoubicacioncomponent } from './components/avisoubicacioncomponent/avisoubicacioncomponent';
import { Iniciocomponent } from './components/iniciocomponent/iniciocomponent';
import { Buscadorcomponent } from './components/buscadorcomponent/buscadorcomponent';
import { Mapacomponent } from './components/mapacomponent/mapacomponent';
import { Salidascomponent } from './components/salidascomponent/salidascomponent';
import { Salidaformcomponent } from './components/salidaformcomponent/salidaformcomponent';
import { Salidadetallecomponent } from './components/salidadetallecomponent/salidadetallecomponent';
import { Invitacioncomponent } from './components/invitacioncomponent/invitacioncomponent';
import { Rutacomponent } from './components/rutacomponent/rutacomponent';

/**
 * Módulo raíz: declara los componentes, importa el enrutamiento y los formularios, y registra el cliente HTTP con el interceptor de autenticación.
 */
@NgModule({
  declarations: [
    App,
    Navbarcomponent,
    Footercomponent,
    Logincomponent,
    Registrocomponent,
    Verificaremailcomponent,
    Olvidopasswordcomponent,
    Restablecerpasswordcomponent,
    Avisoubicacioncomponent,
    Iniciocomponent,
    Buscadorcomponent,
    Mapacomponent,
    Salidascomponent,
    Salidaformcomponent,
    Salidadetallecomponent,
    Invitacioncomponent,
    Rutacomponent,
  ],
  imports: [BrowserModule, AppRoutingModule, FormsModule],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [App],
})
export class AppModule {}
