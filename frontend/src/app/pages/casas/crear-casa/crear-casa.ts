import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CasasService } from '../../../core/services/casas/casas.service';
import { CrearCasaRequest } from '../../../core/models/casas/crear-casa-request.model';
import {
  fireErrorAlert,
  fireSuccessAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-crear-casa',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './crear-casa.html',
  styleUrl: './crear-casa.css'
})
export class CrearCasaComponent {
  private fb = inject(FormBuilder);
  private casasService = inject(CasasService);

  loading = false;
  selectedFile: File | null = null;

  crearCasaForm = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    direccion: ['', [Validators.required, Validators.minLength(5)]],
    numeroHabitaciones: [3, [Validators.required, Validators.min(3)]],
    numeroBanos: [2, [Validators.required, Validators.min(2)]],
    numeroCocinas: [1, [Validators.required, Validators.min(1)]]
  });

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] || null;
    this.selectedFile = file;
  }

  onSubmit(): void {
    if (this.crearCasaForm.invalid || !this.selectedFile) {
      this.crearCasaForm.markAllAsTouched();

      fireWarningAlert(
        'Formulario incompleto',
        'Debes completar todos los campos y seleccionar una foto.'
      );

      return;
    }

    this.loading = true;

    const casaData: CrearCasaRequest = {
      nombre: this.crearCasaForm.value.nombre!,
      direccion: this.crearCasaForm.value.direccion!,
      numeroHabitaciones: Number(this.crearCasaForm.value.numeroHabitaciones),
      numeroBanos: Number(this.crearCasaForm.value.numeroBanos),
      numeroCocinas: Number(this.crearCasaForm.value.numeroCocinas),
      foto: this.selectedFile
    };

    this.casasService.crearCasa(casaData).subscribe({
      next: () => {
        this.loading = false;

        fireSuccessAlert(
          'Casa registrada',
          'La casa rural fue creada correctamente.'
        );

        this.crearCasaForm.reset({
          nombre: '',
          direccion: '',
          numeroHabitaciones: 3,
          numeroBanos: 2,
          numeroCocinas: 1
        });
        this.selectedFile = null;
      },
      error: (error) => {
        this.loading = false;

        fireErrorAlert(
          'Error al registrar la casa',
          error?.error?.mensaje ||
            error?.error?.message ||
            'No se pudo crear la casa rural.'
        );
      }
    });
  }
}