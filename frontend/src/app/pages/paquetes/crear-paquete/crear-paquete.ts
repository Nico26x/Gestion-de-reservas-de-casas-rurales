import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
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
export class CrearPaqueteComponent implements OnInit {
  private fb = inject(FormBuilder);
  private paquetesService = inject(PaquetesService);

  loading = false;

  paqueteForm = this.fb.group({
    casaId: [null as number | null, [Validators.required, Validators.min(1)]],
    fechaInicio: ['', Validators.required],
    fechaFin: ['', Validators.required],
    precio: [null as number | null],
    precioHabitacion: [null as number | null],
    modalidad: ['', [Validators.required]]
  });

  ngOnInit(): void {
    this.paqueteForm.get('modalidad')?.valueChanges.subscribe((modalidad) => {
      const precioControl = this.paqueteForm.get('precio');
      const precioHabitacionControl = this.paqueteForm.get('precioHabitacion');

      // Lógica para precio (CASA_ENTERA o AMBAS)
      if (modalidad === 'CASA_ENTERA' || modalidad === 'AMBAS') {
        precioControl?.setValidators([Validators.required, Validators.min(1)]);
      } else {
        precioControl?.clearValidators();
        precioControl?.reset();
      }
      precioControl?.updateValueAndValidity();

      // Lógica para precioHabitacion (HABITACIONES o AMBAS)
      if (modalidad === 'HABITACIONES' || modalidad === 'AMBAS') {
        precioHabitacionControl?.setValidators([Validators.required, Validators.min(1)]);
      } else {
        precioHabitacionControl?.clearValidators();
        precioHabitacionControl?.reset();
      }
      precioHabitacionControl?.updateValueAndValidity();
    });
  }

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

    const modalidad = this.paqueteForm.value.modalidad!;
    const data: CrearPaqueteRequest = {
      casaId: Number(this.paqueteForm.value.casaId),
      fechaInicio,
      fechaFin,
      precio: (modalidad === 'CASA_ENTERA' || modalidad === 'AMBAS') ? Number(this.paqueteForm.value.precio) : undefined,
      precioHabitacion: (modalidad === 'HABITACIONES' || modalidad === 'AMBAS') ? Number(this.paqueteForm.value.precioHabitacion) : undefined,
      modalidad
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
          precioHabitacion: null,
          modalidad: ''
        });
      },
      error: (error) => {
        this.loading = false;

        fireErrorAlert(
          'Error al crear paquete',
          error?.error?.mensaje ||
            error?.error?.message ||
            error?.error ||
            'No se pudo crear el paquete.'
        );
      }
    });
  }
}