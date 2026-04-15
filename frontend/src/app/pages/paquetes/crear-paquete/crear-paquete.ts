import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PaquetesService } from '../../../core/services/paquetes/paquetes.service';
import { CrearPaqueteRequest } from '../../../core/models/paquetes/crear-paquete-request.model';
import {
  fireErrorAlert,
  fireSuccessAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-crear-paquete',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './crear-paquete.html',
  styleUrl: './crear-paquete.css'
})
export class CrearPaqueteComponent {
  private fb = inject(FormBuilder);
  private paquetesService = inject(PaquetesService);

  loading = false;

  paqueteForm = this.fb.group({
    casaId: [null as number | null, [Validators.required, Validators.min(1)]],
    fechaInicio: ['', Validators.required],
    fechaFin: ['', Validators.required],
    precio: [null as number | null, [Validators.required, Validators.min(1)]],
    modalidad: ['', [Validators.required]]
  });

  onSubmit(): void {
    if (this.paqueteForm.invalid) {
      this.paqueteForm.markAllAsTouched();

      fireWarningAlert(
        'Formulario incompleto',
        'Debes diligenciar correctamente los datos del paquete.'
      );
      return;
    }

    const fechaInicio = this.paqueteForm.value.fechaInicio!;
    const fechaFin = this.paqueteForm.value.fechaFin!;

    if (fechaInicio > fechaFin) {
      fireWarningAlert(
        'Fechas inválidas',
        'La fecha de inicio no puede ser mayor que la fecha final.'
      );
      return;
    }

    this.loading = true;

    const data: CrearPaqueteRequest = {
      casaId: Number(this.paqueteForm.value.casaId),
      fechaInicio,
      fechaFin,
      precio: Number(this.paqueteForm.value.precio),
      modalidad: this.paqueteForm.value.modalidad!
    };

    this.paquetesService.crearPaquete(data).subscribe({
      next: () => {
        this.loading = false;

        fireSuccessAlert(
          'Paquete creado',
          'El paquete fue registrado correctamente.'
        );

        this.paqueteForm.reset({
          casaId: null,
          fechaInicio: '',
          fechaFin: '',
          precio: null,
          modalidad: ''
        });
      },
      error: (error) => {
        this.loading = false;

        fireErrorAlert(
          'Error al crear paquete',
          error?.error?.mensaje ||
            error?.error?.message ||
            'No se pudo crear el paquete.'
        );
      }
    });
  }
}