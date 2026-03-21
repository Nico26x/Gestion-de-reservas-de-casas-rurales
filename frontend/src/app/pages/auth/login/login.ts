import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import Swal from 'sweetalert2';

import { AuthService } from '../../../core/services/auth/auth.service';
import { LoginRequest } from '../../../core/models/auth/login-request.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loading = false;

  loginForm = this.fb.group({
    nombreCuenta: ['', Validators.required],
    contrasena: ['', [Validators.required, Validators.minLength(5)]]
  });

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();

      Swal.fire({
        icon: 'warning',
        title: 'Formulario incompleto',
        text: 'Debes diligenciar correctamente los campos del login.'
      });

      return;
    }

    this.loading = true;

    const loginData: LoginRequest = {
      nombreCuenta: this.loginForm.value.nombreCuenta!,
      contrasena: this.loginForm.value.contrasena!
    };

    this.authService.login(loginData).subscribe({
      next: (response) => {
        this.loading = false;

        Swal.fire({
          icon: 'success',
          title: 'Inicio de sesión exitoso',
          text: response.mensaje || 'Bienvenido al sistema',
          confirmButtonText: 'Continuar'
        }).then(() => {
          this.router.navigate(['/home']);
        });
      },
      error: (error) => {
        this.loading = false;

        Swal.fire({
          icon: 'error',
          title: 'Error al iniciar sesión',
          text:
            error?.error?.mensaje ||
            error?.error?.message ||
            'Credenciales inválidas. Verifica tus datos e inténtalo de nuevo.'
        });
      }
    });
  }
}
