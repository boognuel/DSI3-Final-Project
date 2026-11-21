import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

/**
 * Rutas de la aplicación con su componente y su guarda.
 */
const routes: Routes = [];

/**
 * Módulo de enrutamiento: asocia cada ruta con su componente y su guarda.
 */
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
