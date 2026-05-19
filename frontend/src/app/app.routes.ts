import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { loginGuard } from './core/guards/login.guard';

import { HomeComponent } from './pages/home/home';
import { LoginComponent } from './pages/auth/login/login';
import { RegisterComponent } from './pages/auth/register/register';
import { CrearCasaComponent } from './pages/casas/crear-casa/crear-casa';
import { DetalleCasaComponent } from './pages/casas/detalle-casa/detalle-casa';
import { MisCasasComponent } from './pages/casas/mis-casas/mis-casas';
import { EditarCasaComponent } from './pages/casas/editar-casa/editar-casa';
import { CrearPaqueteComponent } from './pages/paquetes/crear-paquete/crear-paquete';
import { EditarPaqueteComponent } from './pages/paquetes/editar-paquete/editar-paquete';
import { MisPaquetesComponent } from './pages/paquetes/mis-paquetes/mis-paquetes';
import { CrearReservaComponent } from './pages/reservas/crear-reserva/crear-reserva';
import { ReservasRecibidasComponent } from './pages/reservas/reservas-recibidas/reservas-recibidas';
import { ReservasVencidasComponent } from './pages/reservas/reservas-vencidas/reservas-vencidas';
import { NotFoundComponent } from './pages/not-found/not-found';
import { DefinirDisponibilidadComponent } from './pages/disponibilidad/definir-disponibilidad/definir-disponibilidad';
import { ConsultarDisponibilidadComponent } from './pages/disponibilidad/consultar-disponibilidad/consultar-disponibilidad';
import { ownerGuard } from './core/guards/owner.guard';
import { BuscarCasasComponent } from './pages/casas/buscar-casas/buscar-casas';
import { RegistrarPagoComponent } from './pages/pagos/registrar-pago/registrar-pago';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: LoginComponent, canActivate: [loginGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [loginGuard] },

  { path: 'home', component: HomeComponent, canActivate: [authGuard] },

  { path: 'casas/crear', component: CrearCasaComponent, canActivate: [ownerGuard] },
  { path: 'casas/mis-casas', component: MisCasasComponent, canActivate: [ownerGuard] },
  { path: 'casas/editar/:id', component: EditarCasaComponent, canActivate: [ownerGuard] },
  { path: 'casas/buscar', component: BuscarCasasComponent },
  { path: 'casas/:id', component: DetalleCasaComponent },
  { path: 'paquetes/crear', component: CrearPaqueteComponent, canActivate: [ownerGuard] },
  { path: 'paquetes/:id/editar', component: EditarPaqueteComponent, canActivate: [ownerGuard] },
  { path: 'paquetes', component: MisPaquetesComponent, canActivate: [ownerGuard] },
  { path: 'reservas/crear', component: CrearReservaComponent, canActivate: [authGuard] },
  { path: 'reservas/recibidas', component: ReservasRecibidasComponent, canActivate: [ownerGuard] },
  { path: 'reservas/vencidas', component: ReservasVencidasComponent, canActivate: [ownerGuard] },
  { path: 'disponibilidad/definir', component: DefinirDisponibilidadComponent, canActivate: [ownerGuard] },
  { path: 'disponibilidad/consultar', component: ConsultarDisponibilidadComponent, canActivate: [authGuard] },
  { path: 'pagos/registrar', component: RegistrarPagoComponent, canActivate: [ownerGuard] },

  { path: '**', component: NotFoundComponent }
];