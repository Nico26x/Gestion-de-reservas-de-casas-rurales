import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DisponibilidadService } from '../../../core/services/disponibilidad/disponibilidad.service';
import { DisponibilidadRequest } from '../../../core/models/disponibilidad/disponibilidad-request.model';
import {
  fireErrorAlert,
  fireSuccessAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-definir-disponibilidad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './definir-disponibilidad.html',
  styleUrl: './definir-disponibilidad.css'
})
export class DefinirDisponibilidadComponent {
  private fb = inject(FormBuilder);
  private disponibilidadService = inject(DisponibilidadService);

  loadingRegistrar = false;

  registrarForm = this.fb.group({
    casaId: [null as number | null, [Validators.required, Validators.min(1)]],
    fechaInicio: ['', Validators.required],
    fechaFin: ['', Validators.required],
    modalidad: ['AMBAS', Validators.required],
    estadoCasa: ['LIBRE', Validators.required],
    estadoHabitaciones: ['LIBRE', Validators.required]
  });

  // Construye el payload filtrando campos según la modalidad
  private construirPayloadSegunModalidad(): DisponibilidadRequest {
    const modalidad = this.registrarForm.value.modalidad!;

    return {
      casaId: Number(this.registrarForm.value.casaId),
      fechaInicio: this.registrarForm.value.fechaInicio!,
      fechaFin: this.registrarForm.value.fechaFin!,
      modalidad,
      estadoCasa: modalidad === 'HABITACIONES' ? 'LIBRE' : this.registrarForm.value.estadoCasa!,
      estadoHabitaciones: modalidad === 'CASA_ENTERA' ? 'LIBRE' : this.registrarForm.value.estadoHabitaciones!
    };
  }

  registrarDisponibilidad(): void {
    if (this.registrarForm.invalid) {
      this.registrarForm.markAllAsTouched();
      fireWarningAlert(
        'Formulario incompleto',
        'Debes diligenciar correctamente los datos de disponibilidad.'
      );
      return;
    }

    const fechaInicio = this.registrarForm.value.fechaInicio!;
    const fechaFin = this.registrarForm.value.fechaFin!;

    if (fechaInicio > fechaFin) {
      fireWarningAlert(
        'Fechas inválidas',
        'La fecha de inicio no puede ser mayor que la fecha final.'
      );
      return;
    }

    this.loadingRegistrar = true;

    const data: DisponibilidadRequest = this.construirPayloadSegunModalidad();

    this.disponibilidadService.registrarDisponibilidad(data).subscribe({
      next: (response) => {
        this.loadingRegistrar = false;

        fireSuccessAlert(
          'Disponibilidad registrada',
          response.mensaje || 'La disponibilidad fue registrada correctamente.'
        );

        this.registrarForm.reset({
          casaId: null,
          fechaInicio: '',
          fechaFin: '',
          modalidad: 'AMBAS',
          estadoCasa: 'LIBRE',
          estadoHabitaciones: 'LIBRE'
        });
      },
      error: (error) => {
        this.loadingRegistrar = false;

        fireErrorAlert(
          'Error al registrar disponibilidad',
          error?.error?.mensaje ||
            error?.error?.message ||
            'No se pudo registrar la disponibilidad.'
        );
      }
    });
  }
}