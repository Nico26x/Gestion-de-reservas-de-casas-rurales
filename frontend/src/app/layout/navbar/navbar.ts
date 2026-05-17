import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  get nombreCuenta(): string | null {
    return this.authService.getNombreCuenta();
  }

  get rol(): string | null {
    return this.authService.getRol();
  }

  get esPropietario(): boolean {
    return this.rol === 'PROPIETARIO' || this.rol === 'ROLE_PROPIETARIO';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}