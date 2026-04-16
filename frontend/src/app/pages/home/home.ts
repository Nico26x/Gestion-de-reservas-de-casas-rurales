import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent {
  private authService = inject(AuthService);

  get nombreCuenta(): string | null {
    return this.authService.getNombreCuenta();
  }

  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  get rol(): string | null {
    return this.authService.getRol();
  }

  get esCliente(): boolean {
    return this.rol === 'CLIENTE' || this.rol === 'ROLE_CLIENTE';
  }
}