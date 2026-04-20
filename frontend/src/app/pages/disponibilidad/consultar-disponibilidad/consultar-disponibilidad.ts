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

  // Devuelve el estado correcto según la modalidad
  getEstadoSegunModalidad(dia: DisponibilidadDiaResponse): string {
    switch (dia.modalidad?.toUpperCase()) {
      case 'CASA_ENTERA':
        return dia.estadoCasa;

      case 'HABITACIONES':
        // Para habitaciones, determinar el estado según la disponibilidad real
        if (!dia.habitaciones || dia.habitaciones.length === 0) {
          return 'NO_DISPONIBLE';
        }
        
        // Revisar si hay al menos una habitación LIBRE
        const habitacionesLibres = dia.habitaciones.filter(
          (h) => h.estado === 'LIBRE'
        ).length;
        if (habitacionesLibres > 0) {
          return 'LIBRE';
        }
        
        // Si no hay libres, revisar si hay al menos una RESERVADA
        const tieneReservadas = dia.habitaciones.some(
          (h) => h.estado === 'RESERVADA'
        );
        if (tieneReservadas) {
          return 'RESERVADA';
        }
        
        // Si no hay libres ni reservadas, todo está bloqueado
        return 'NO_DISPONIBLE';

      case 'AMBAS':
        // Para modalidad AMBAS, combinar estado de casa + estado de habitaciones
        // Si hay alguna vía de reserva disponible, reflejarlo
        const casaLibre = dia.estadoCasa === 'LIBRE';
        const habitacionesLibresAmbas = 
          dia.habitaciones && dia.habitaciones.length > 0 &&
          dia.habitaciones.some((h) => h.estado === 'LIBRE');
        
        // Si casa o alguna habitación están libres, mostrar LIBRE
        if (casaLibre || habitacionesLibresAmbas) {
          return 'LIBRE';
        }
        
        // Si no hay vía libre, revisar si hay reservadas
        const casaReservada = dia.estadoCasa === 'RESERVADA';
        const habitacionesReservadas = 
          dia.habitaciones && dia.habitaciones.length > 0 &&
          dia.habitaciones.some((h) => h.estado === 'RESERVADA');
        
        if (casaReservada || habitacionesReservadas) {
          return 'RESERVADA';
        }
        
        // Si todo está bloqueado
        return 'NO_DISPONIBLE';

      default:
        return dia.estadoCasa;
    }
  }
}