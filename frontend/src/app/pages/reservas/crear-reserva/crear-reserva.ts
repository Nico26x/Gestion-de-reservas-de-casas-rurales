import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CasasService } from '../../../core/services/casas/casas.service';
import { DisponibilidadService } from '../../../core/services/disponibilidad/disponibilidad.service';
import { Casa } from '../../../core/models/casas/casa.model';
import { DisponibilidadDiaResponse } from '../../../core/models/disponibilidad/disponibilidad-dia-response.model';
import { environment } from '../../../environments/environment';
import {
  fireErrorAlert,
  fireSuccessAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';

interface ReservaResponseView {
  numeroReserva?: number;
  fechaEntrada?: string;
  numeroNoches?: number;
  importe?: number;
  anticipo?: number;
  numeroCuentaBancaria?: string;
  nombreCasa?: string;
  telefonoCliente?: string;
  estado?: string;
  mensaje?: string;
}

@Component({
  selector: 'app-crear-reserva',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './crear-reserva.html',
  styleUrl: './crear-reserva.css'
})
export class CrearReservaComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private casasService = inject(CasasService);
  private disponibilidadService = inject(DisponibilidadService);

  private apiUrl = `${environment.apiUrl}/reservas`;

  loading = false;
  consultandoDisponibilidad = false;

  casas: Casa[] = [];
  casaSeleccionada: Casa | null = null;
  disponibilidad: DisponibilidadDiaResponse[] = [];
  reservaCreada: ReservaResponseView | null = null;

  minDate = new Date().toISOString().split('T')[0];

  reservaForm = this.fb.group({
    casaId: [null as number | null, [Validators.required, Validators.min(1)]],
    fechaEntrada: ['', Validators.required],
    numeroNoches: [1, [Validators.required, Validators.min(1)]],
    telefonoCliente: [
      '',
      [Validators.required, Validators.minLength(7), Validators.maxLength(20)]
    ],
    tipoReserva: ['CASA_COMPLETA', Validators.required]
  });

  ngOnInit(): void {
    this.cargarCasas();
  }

  get casaId() {
    return this.reservaForm.get('casaId');
  }

  get fechaEntrada() {
    return this.reservaForm.get('fechaEntrada');
  }

  get numeroNoches() {
    return this.reservaForm.get('numeroNoches');
  }

  get telefonoCliente() {
    return this.reservaForm.get('telefonoCliente');
  }

  get tipoReserva() {
    return this.reservaForm.get('tipoReserva');
  }

  cargarCasas(): void {
    this.casasService.obtenerTodas().subscribe({
      next: (response) => {
        this.casas = response ?? [];
      },
      error: (error) => {
        fireErrorAlert(
          'Error al cargar casas',
          this.extractErrorMessage(error, 'No se pudieron cargar las casas disponibles.')
        );
      }
    });
  }

  onCasaChange(): void {
    const casaId = Number(this.reservaForm.value.casaId);
    this.casaSeleccionada =
      this.casas.find((casa) => Number(casa.id) === casaId) ?? null;

    this.disponibilidad = [];
    this.reservaCreada = null;
  }

  consultarDisponibilidad(showFeedback = true): void {
    if (!this.hasFieldsForAvailability()) {
      this.reservaForm.markAllAsTouched();

      if (showFeedback) {
        fireWarningAlert(
          'Faltan datos',
          'Selecciona una casa, una fecha de entrada y el número de noches.'
        );
      }
      return;
    }

    if (!this.isFechaValida()) {
      if (showFeedback) {
        fireWarningAlert(
          'Fecha inválida',
          'La fecha de entrada no puede ser anterior al día actual.'
        );
      }
      return;
    }

    this.consultandoDisponibilidad = true;
    this.disponibilidad = [];
    this.reservaCreada = null;

    this.disponibilidadService
      .consultarDisponibilidad(
        Number(this.reservaForm.value.casaId),
        this.reservaForm.value.fechaEntrada!,
        Number(this.reservaForm.value.numeroNoches)
      )
      .subscribe({
        next: (response) => {
          this.consultandoDisponibilidad = false;
          this.disponibilidad = response ?? [];

          if (showFeedback && this.disponibilidad.length === 0) {
            fireWarningAlert(
              'Sin disponibilidad',
              'No se encontró información de disponibilidad para el rango seleccionado.'
            );
          }
        },
        error: (error) => {
          this.consultandoDisponibilidad = false;

          if (showFeedback) {
            fireErrorAlert(
              'Error al consultar disponibilidad',
              this.extractErrorMessage(
                error,
                'No fue posible consultar la disponibilidad.'
              )
            );
          }
        }
      });
  }

  onSubmit(): void {
    if (this.reservaForm.invalid) {
      this.reservaForm.markAllAsTouched();

      fireWarningAlert(
        'Formulario incompleto',
        'Debes diligenciar correctamente la casa, la fecha, las noches y el teléfono.'
      );
      return;
    }

    if (!this.isFechaValida()) {
      fireWarningAlert(
        'Fecha inválida',
        'La fecha de entrada no puede ser anterior al día actual.'
      );
      return;
    }

    this.loading = true;
    this.reservaCreada = null;

    const payload = {
      casaId: Number(this.reservaForm.value.casaId),
      fechaEntrada: this.reservaForm.value.fechaEntrada!,
      numeroNoches: Number(this.reservaForm.value.numeroNoches),
      telefonoCliente: this.reservaForm.value.telefonoCliente!.trim(),
      tipoReserva: this.reservaForm.value.tipoReserva!,
      habitacionIds: [] as number[]
    };

    this.http.post<ReservaResponseView>(this.apiUrl, payload).subscribe({
      next: (response) => {
        this.loading = false;
        this.reservaCreada = response;

        fireSuccessAlert(
          'Reserva creada',
          'La reserva fue registrada correctamente.'
        );

        this.consultarDisponibilidad(false);
      },
      error: (error) => {
        this.loading = false;

        fireErrorAlert(
          'Error al crear reserva',
          this.extractErrorMessage(error, 'No se pudo registrar la reserva.')
        );
      }
    });
  }

  limpiarFormulario(): void {
    this.reservaForm.reset({
      casaId: null,
      fechaEntrada: '',
      numeroNoches: 1,
      telefonoCliente: '',
      tipoReserva: 'CASA_COMPLETA'
    });

    this.casaSeleccionada = null;
    this.disponibilidad = [];
    this.reservaCreada = null;
  }

  getFechaSalidaEstimada(): string {
    const fechaEntrada = this.reservaForm.value.fechaEntrada;
    const numeroNoches = Number(this.reservaForm.value.numeroNoches ?? 0);

    if (!fechaEntrada || !numeroNoches || numeroNoches < 1) {
      return 'Pendiente';
    }

    const fecha = new Date(`${fechaEntrada}T00:00:00`);
    fecha.setDate(fecha.getDate() + numeroNoches);

    return this.formatDate(fecha.toISOString().split('T')[0]);
  }

  getHabitacionesLibresCount(dia: DisponibilidadDiaResponse): number {
    return (dia.habitaciones ?? []).filter(
      (habitacion) => habitacion.estado === 'LIBRE'
    ).length;
  }

  getEstadoBadgeClass(estado: string | undefined): string {
    switch ((estado ?? '').toUpperCase()) {
      case 'LIBRE':
        return 'badge--success';
      case 'RESERVADA':
        return 'badge--danger';
      default:
        return 'badge--neutral';
    }
  }

  getModalidadLabel(modalidad: string | undefined): string {
    if (!modalidad) return 'No definida';

    switch (modalidad.toUpperCase()) {
      case 'CASA_ENTERA':
        return 'Casa entera';
      case 'HABITACIONES':
        return 'Por habitaciones';
      default:
        return modalidad;
    }
  }

  getTipoReservaLabel(tipo: string | undefined): string {
    if (!tipo) return 'No definido';

    switch (tipo.toUpperCase()) {
      case 'CASA_COMPLETA':
        return 'Casa completa';
      case 'HABITACIONES':
        return 'Habitaciones';
      case 'AMBAS':
        return 'Ambas';
      default:
        return tipo;
    }
  }

  formatMoney(value: number | undefined): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      maximumFractionDigits: 0
    }).format(value ?? 0);
  }

  formatDate(date: string | undefined): string {
    if (!date) return 'No disponible';

    const [year, month, day] = date.split('-').map(Number);
    const parsed = new Date(year, month - 1, day);

    return new Intl.DateTimeFormat('es-CO', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    }).format(parsed);
  }

  private hasFieldsForAvailability(): boolean {
    return !!(
      this.reservaForm.value.casaId &&
      this.reservaForm.value.fechaEntrada &&
      this.reservaForm.value.numeroNoches
    );
  }

  private isFechaValida(): boolean {
    const fechaEntrada = this.reservaForm.value.fechaEntrada;
    if (!fechaEntrada) return false;

    return fechaEntrada >= this.minDate;
  }

  private extractErrorMessage(error: any, fallback: string): string {
    if (typeof error?.error === 'string' && error.error.trim()) {
      return error.error;
    }

    return (
      error?.error?.mensaje ||
      error?.error?.message ||
      error?.message ||
      fallback
    );
  }
}