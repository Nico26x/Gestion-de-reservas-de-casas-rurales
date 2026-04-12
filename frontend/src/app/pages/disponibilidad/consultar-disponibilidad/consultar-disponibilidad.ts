import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DisponibilidadService } from '../../../core/services/disponibilidad/disponibilidad.service';
import { DisponibilidadDiaResponse } from '../../../core/models/disponibilidad/disponibilidad-dia-response.model';
import {
  fireErrorAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-consultar-disponibilidad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './consultar-disponibilidad.html',
  styleUrl: './consultar-disponibilidad.css'
})
export class ConsultarDisponibilidadComponent {
  private fb = inject(FormBuilder);
  private disponibilidadService = inject(DisponibilidadService);

  loadingConsultar = false;
  disponibilidadPorDia: DisponibilidadDiaResponse[] = [];

  consultaForm = this.fb.group({
    casaId: [null as number | null, [Validators.required, Validators.min(1)]],
    fechaEntrada: ['', Validators.required],
    numeroNoches: [1, [Validators.required, Validators.min(1)]]
  });

  consultarDisponibilidad(): void {
    if (this.consultaForm.invalid) {
      this.consultaForm.markAllAsTouched();
      fireWarningAlert(
        'Formulario incompleto',
        'Debes diligenciar correctamente los datos de consulta.'
      );
      return;
    }

    this.loadingConsultar = true;
    this.disponibilidadPorDia = [];

    this.disponibilidadService
      .consultarDisponibilidad(
        Number(this.consultaForm.value.casaId),
        this.consultaForm.value.fechaEntrada!,
        Number(this.consultaForm.value.numeroNoches)
      )
      .subscribe({
        next: (response) => {
          this.loadingConsultar = false;
          this.disponibilidadPorDia = response;
        },
        error: (error) => {
          this.loadingConsultar = false;
          fireErrorAlert(
            'Error al consultar disponibilidad',
            error?.error?.mensaje ||
              error?.error?.message ||
              'No se pudo consultar la disponibilidad.'
          );
        }
      });
  }
}