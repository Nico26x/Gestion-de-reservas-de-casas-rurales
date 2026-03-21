import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { loginGuard } from './core/guards/login.guard';

import { HomeComponent } from './pages/home/home';
import { LoginComponent } from './pages/auth/login/login';
import { RegisterComponent } from './pages/auth/register/register';
import { CrearCasaComponent } from './pages/casas/crear-casa/crear-casa';
import { CrearPaqueteComponent } from './pages/paquetes/crear-paquete/crear-paquete';
import { CrearReservaComponent } from './pages/reservas/crear-reserva/crear-reserva';
import { NotFoundComponent } from './pages/not-found/not-found';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: LoginComponent, canActivate: [loginGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [loginGuard] },

  { path: 'home', component: HomeComponent, canActivate: [authGuard] },

  { path: 'casas/crear', component: CrearCasaComponent, canActivate: [authGuard] },
  { path: 'paquetes/crear', component: CrearPaqueteComponent, canActivate: [authGuard] },
  { path: 'reservas/crear', component: CrearReservaComponent, canActivate: [authGuard] },

  { path: '**', component: NotFoundComponent }
];