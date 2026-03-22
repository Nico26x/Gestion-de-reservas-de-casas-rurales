import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth/auth.service';
import { RegisterRequest } from '../../../core/models/auth/register-request.model';
import {
  fireErrorAlert,
  fireSuccessAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loading = false;

  registerForm = this.fb.group({
    nombreCuenta: ['', [Validators.required, Validators.minLength(3)]],
    contrasena: ['', [Validators.required, Validators.minLength(5)]]
  });

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();

      fireWarningAlert(
        'Formulario incompleto',
        'Debes diligenciar correctamente los campos del registro.'
      );

      return;
    }

    this.loading = true;

    const registerData: RegisterRequest = {
      nombreCuenta: this.registerForm.value.nombreCuenta!,
      contrasena: this.registerForm.value.contrasena!
    };

    this.authService.register(registerData).subscribe({
      next: (response) => {
        this.loading = false;

        fireSuccessAlert(
          'Registro exitoso',
          response.mensaje || 'La cuenta fue creada correctamente.'
        ).then(() => {
          this.router.navigate(['/login']);
        });
      },
      error: (error) => {
        this.loading = false;

        fireErrorAlert(
          'Error en el registro',
          error?.error?.mensaje ||
            error?.error?.message ||
            'No se pudo completar el registro.'
        );
      }
    });
  }
}