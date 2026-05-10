import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PaquetesService } from '../../../core/services/paquetes/paquetes.service';
import { Paquete } from '../../../core/models/paquetes/paquete.model';
import { CrearPaqueteRequest } from '../../../core/models/paquetes/crear-paquete-request.model';
import {
  fireErrorAlert,
  fireSuccessAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-editar-paquete',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editar-paquete.html',
  styleUrl: './editar-paquete.css'
})
export class EditarPaqueteComponent implements OnInit {
  private fb = inject(FormBuilder);
  private paquetesService = inject(PaquetesService);
  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);

  paqueteId: number | null = null;
  paqueteActual: Paquete | null = null;
  editando = false;
  cargando = false;
  mensajeExito: string | null = null;
  mensajeError: string | null = null;

  paqueteForm = this.fb.group({
    casaId: [null as number | null, [Validators.required, Validators.min(1)]],
    fechaInicio: ['', Validators.required],
    fechaFin: ['', Validators.required],
    precio: [null as number | null],
    precioHabitacion: [null as number | null],
    modalidad: ['', [Validators.required]]
  });

  ngOnInit(): void {
    // Obtener el id del paquete desde la URL
    this.activatedRoute.params.subscribe(params => {
      const id = params['id'];
      if (id && !isNaN(id)) {
        this.paqueteId = Number(id);
        this.cargarPaquete();
      } else {
        this.mensajeError = 'ID de paquete inválido';
      }
    });

    // Suscribirse a cambios de modalidad para actualizar validaciones
    this.paqueteForm.get('modalidad')?.valueChanges.subscribe((modalidad) => {
      this.actualizarValidacionesPorModalidad(modalidad);
    });
  }

  cargarPaquete(): void {
    if (!this.paqueteId) {
      this.mensajeError = 'No se puede cargar el paquete sin un ID válido';
      return;
    }

    this.cargando = true;
    this.mensajeError = null;

    this.paquetesService.obtenerPaquete(this.paqueteId).subscribe({
      next: (paquete) => {
        this.cargando = false;
        this.paqueteActual = paquete;

        // Precargar el formulario con los datos del paquete
        this.paqueteForm.patchValue({
          casaId: paquete.casaId,
          fechaInicio: paquete.fechaInicio,
          fechaFin: paquete.fechaFin,
          precio: paquete.precio,
          precioHabitacion: paquete.precioHabitacion,
          modalidad: paquete.modalidad
        });

        // Actualizar validaciones según la modalidad cargada
        this.actualizarValidacionesPorModalidad(paquete.modalidad);
        this.editando = true;
      },
      error: (error) => {
        this.cargando = false;
        this.mensajeError = this.extraerMensajeError(error);
      }
    });
  }

  actualizarValidacionesPorModalidad(modalidad: string | null | undefined): void {
    const precioControl = this.paqueteForm.get('precio');
    const precioHabitacionControl = this.paqueteForm.get('precioHabitacion');

    if (!precioControl || !precioHabitacionControl) {
      return;
    }

    // Lógica para precio (CASA_ENTERA o AMBAS)
    if (modalidad === 'CASA_ENTERA' || modalidad === 'AMBAS') {
      precioControl.setValidators([Validators.required, Validators.min(0.01)]);
    } else {
      precioControl.clearValidators();
      precioControl.reset();
    }
    precioControl.updateValueAndValidity();

    // Lógica para precioHabitacion (HABITACIONES o AMBAS)
    if (modalidad === 'HABITACIONES' || modalidad === 'AMBAS') {
      precioHabitacionControl.setValidators([Validators.required, Validators.min(0.01)]);
    } else {
      precioHabitacionControl.clearValidators();
      precioHabitacionControl.reset();
    }
    precioHabitacionControl.updateValueAndValidity();
  }

  modificarPaquete(): void {
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

    if (!this.paqueteId) {
      fireErrorAlert(
        'Error',
        'No se puede modificar sin un ID válido.'
      );
      return;
    }

    this.cargando = true;
    this.limpiarMensajes();

    const modalidad = this.paqueteForm.value.modalidad!;
    const data: CrearPaqueteRequest = {
      casaId: Number(this.paqueteForm.value.casaId),
      fechaInicio,
      fechaFin,
      precio: (modalidad === 'CASA_ENTERA' || modalidad === 'AMBAS') ? Number(this.paqueteForm.value.precio) : undefined,
      precioHabitacion: (modalidad === 'HABITACIONES' || modalidad === 'AMBAS') ? Number(this.paqueteForm.value.precioHabitacion) : undefined,
      modalidad
    };

    this.paquetesService.modificarPaquete(this.paqueteId, data).subscribe({
      next: () => {
        this.cargando = false;
        fireSuccessAlert(
          'Paquete actualizado',
          'El paquete fue modificado correctamente.'
        );
        this.mensajeExito = 'Paquete modificado correctamente';
        setTimeout(() => {
          this.volver();
        }, 2000);
      },
      error: (error) => {
        this.cargando = false;
        this.mensajeError = this.extraerMensajeError(error);
        fireErrorAlert(
          'Error al modificar paquete',
          this.mensajeError
        );
      }
    });
  }

  limpiarMensajes(): void {
    this.mensajeExito = null;
    this.mensajeError = null;
  }

  volver(): void {
    this.router.navigate(['/home']);
  }

  private extraerMensajeError(error: any): string {
    if (error?.error?.message) {
      return error.error.message;
    }
    if (error?.error && typeof error.error === 'string') {
      return error.error;
    }
    if (error?.error?.mensaje) {
      return error.error.mensaje;
    }
    return 'Ocurrió un error al modificar el paquete. Por favor, intenta de nuevo.';
  }
}
