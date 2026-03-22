import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth/auth.service';
import { LoginRequest } from '../../../core/models/auth/login-request.model';
import {
  fireErrorAlert,
  fireSuccessAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';
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

      fireWarningAlert(
      'Formulario incompleto',
      'Debes diligenciar correctamente los campos del login.'
    );

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

        fireSuccessAlert(
        'Inicio de sesión exitoso',
        response.mensaje || 'Bienvenido al sistema'
      ).then(() => {
        this.router.navigate(['/home']);
      });
    },
      error: (error) => {
        this.loading = false;

        fireErrorAlert(
        'Error al iniciar sesión',
        error?.error?.mensaje ||
          error?.error?.message ||
          'Credenciales inválidas. Verifica tus datos e inténtalo de nuevo.'
      );

      }
    });
  }
}
