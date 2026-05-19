import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CasasService } from '../../../core/services/casas/casas.service';
import { DisponibilidadService } from '../../../core/services/disponibilidad/disponibilidad.service';
import { Casa } from '../../../core/models/casas/casa.model';
import { DisponibilidadDiaResponse } from '../../../core/models/disponibilidad/disponibilidad-dia-response.model';
import { HabitacionDisponibilidadResponse } from '../../../core/models/disponibilidad/habitacion-disponibilidad-response.model';
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
  habitacionesDisponiblesSeleccionables: HabitacionDisponibilidadResponse[] = [];
  reservaCreada: ReservaResponseView | null = null;
  habitacionesSeleccionadas: number[] = [];

  minDate = (() => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  })();

  reservaForm = this.fb.group({
    casaId: [null as number | null, [Validators.required, Validators.min(1)]],
    fechaEntrada: ['', Validators.required],
    numeroNoches: [1, [Validators.required, Validators.min(1)]],
    telefonoCliente: [
      '',
      [Validators.required, Validators.minLength(7), Validators.maxLength(20)]
    ],
    tipoReserva: ['CASA_COMPLETA', Validators.required],
    habitacionIds: [[] as number[]]
  });

  ngOnInit(): void {
    this.cargarCasas();

    //  Listener para resetear habitacionIds cuando cambia tipoReserva
    this.reservaForm.get('tipoReserva')?.valueChanges.subscribe(() => {
      this.habitacionesSeleccionadas = [];
      this.reservaForm.get('habitacionIds')?.setValue([]);
      this.reservaCreada = null;
    });

    //  Listener para limpiar selección cuando cambia fechaEntrada o numeroNoches
    this.reservaForm.get('fechaEntrada')?.valueChanges.subscribe(() => {
      this.habitacionesSeleccionadas = [];
      this.reservaForm.get('habitacionIds')?.setValue([]);
      this.habitacionesDisponiblesSeleccionables = [];
      this.disponibilidad = [];
      this.reservaCreada = null;
    });

    this.reservaForm.get('numeroNoches')?.valueChanges.subscribe(() => {
      this.habitacionesSeleccionadas = [];
      this.reservaForm.get('habitacionIds')?.setValue([]);
      this.habitacionesDisponiblesSeleccionables = [];
      this.disponibilidad = [];
      this.reservaCreada = null;
    });
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
    const casaBasica =
      this.casas.find((casa) => Number(casa.id) === casaId) ?? null;

    if (casaBasica) {
      // Obtener detalles completos incluyendo fotos
      this.casasService.obtenerCasaPorId(casaBasica.id!).subscribe({
        next: (casaDetalle) => {
          this.casaSeleccionada = casaDetalle;
        },
        error: (error) => {
          // Si falla la carga de detalles, usar la información básica
          this.casaSeleccionada = casaBasica;
        }
      });
    } else {
      this.casaSeleccionada = null;
    }

    this.habitacionesSeleccionadas = [];
    this.reservaForm.get('habitacionIds')?.setValue([]);
    this.habitacionesDisponiblesSeleccionables = [];
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
    this.habitacionesDisponiblesSeleccionables = [];
    if (showFeedback) {
      this.reservaCreada = null;
    }

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

          // Construir lista de habitaciones disponibles desde la consulta
          this.construirHabitacionesDisponibles();

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

    //  VALIDAR HABITACIONES SI TIPO DE RESERVA ES HABITACIONES
    const tipoReserva = this.reservaForm.value.tipoReserva;
    if (tipoReserva === 'HABITACIONES' && this.habitacionesSeleccionadas.length === 0) {
      fireWarningAlert(
        'Habitaciones requeridas',
        'Debes seleccionar al menos una habitación para una reserva por habitaciones.'
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
      habitacionIds: tipoReserva === 'HABITACIONES' ? this.habitacionesSeleccionadas : []
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

  toggleHabitacion(habitacionId: number): void {
    const index = this.habitacionesSeleccionadas.indexOf(habitacionId);
    if (index > -1) {
      this.habitacionesSeleccionadas.splice(index, 1);
    } else {
      this.habitacionesSeleccionadas.push(habitacionId);
    }
    this.reservaForm.get('habitacionIds')?.setValue(this.habitacionesSeleccionadas);
  }

  isHabitacionSeleccionada(habitacionId: number): boolean {
    return this.habitacionesSeleccionadas.includes(habitacionId);
  }

  construirHabitacionesDisponibles(): void {
    // Primero, validar que TODOS los días del rango permitan selección por habitaciones
    const algunDiaEsCasaEntera = this.disponibilidad.some(
      (dia) => dia.modalidad?.toUpperCase() === 'CASA_ENTERA'
    );

    if (algunDiaEsCasaEntera || this.disponibilidad.length === 0) {
      // Si algún día es CASA_ENTERA o no hay disponibilidad, no hay habitaciones seleccionables
      this.habitacionesDisponiblesSeleccionables = [];
      this.limpiarHabitacionesNoValidas([]);
      return;
    }

    // Calcular intersección de habitaciones libres en TODOS los días
    const habitacionesDisponibles: Map<number, HabitacionDisponibilidadResponse> = new Map();
    let esPrimerDia = true;

    for (const dia of this.disponibilidad) {
      const habitacionesLibresEnEsteDia = new Set<number>();

      // Recopilar IDs de habitaciones libres en este día
      for (const habitacion of dia.habitaciones ?? []) {
        if (habitacion.estado === 'LIBRE') {
          habitacionesLibresEnEsteDia.add(habitacion.id);
        }
      }

      if (esPrimerDia) {
        // En el primer día, agregar todas las habitaciones libres
        for (const habitacion of dia.habitaciones ?? []) {
          if (habitacion.estado === 'LIBRE') {
            habitacionesDisponibles.set(habitacion.id, habitacion);
          }
        }
        esPrimerDia = false;
      } else {
        // En los siguientes días, mantener solo las que también están libres
        for (const [habitacionId] of habitacionesDisponibles) {
          if (!habitacionesLibresEnEsteDia.has(habitacionId)) {
            // Si no está libre en este día, eliminar de la lista
            habitacionesDisponibles.delete(habitacionId);
          }
        }
      }
    }

    // Convertir a array ordenado por número de habitación
    this.habitacionesDisponiblesSeleccionables = Array.from(habitacionesDisponibles.values()).sort(
      (a, b) => {
        const numA = this.obtenerNumeroHabitacion(a.codigoHabitacion);
        const numB = this.obtenerNumeroHabitacion(b.codigoHabitacion);
        if (numA !== numB) {
          return numA - numB;
        }
        return a.id - b.id;
      }
    );

    // Limpiar habitaciones seleccionadas que ya no sean válidas
    this.limpiarHabitacionesNoValidas(
      this.habitacionesDisponiblesSeleccionables.map((h) => h.id)
    );
  }

  private limpiarHabitacionesNoValidas(habitacionesValidas: number[]): void {
    const habitacionesValidasSet = new Set(habitacionesValidas);

    // Filtrar habitaciones seleccionadas que ya no son válidas
    this.habitacionesSeleccionadas = this.habitacionesSeleccionadas.filter(
      (id) => habitacionesValidasSet.has(id)
    );

    // Sincronizar el form control
    this.reservaForm.get('habitacionIds')?.setValue(this.habitacionesSeleccionadas);
  }

  limpiarFormulario(): void {
    this.reservaForm.reset({
      casaId: null,
      fechaEntrada: '',
      numeroNoches: 1,
      telefonoCliente: '',
      tipoReserva: 'CASA_COMPLETA',
      habitacionIds: []
    });

    this.habitacionesSeleccionadas = [];
    this.habitacionesDisponiblesSeleccionables = [];
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

    const year = fecha.getFullYear();
    const month = String(fecha.getMonth() + 1).padStart(2, '0');
    const day = String(fecha.getDate()).padStart(2, '0');
    return this.formatDate(`${year}-${month}-${day}`);
  }

  getHabitacionesLibresCount(dia: DisponibilidadDiaResponse): number {
    return (dia.habitaciones ?? []).filter(
      (habitacion) => habitacion.estado === 'LIBRE'
    ).length;
  }

  // Devuelve el estado a mostrar según modalidad del día y tipo de reserva seleccionado
  getEstadoParaMostrar(dia: DisponibilidadDiaResponse): string {
    const tipoReserva = this.reservaForm.value.tipoReserva?.toUpperCase();
    const modalidadDia = dia.modalidad?.toUpperCase();

    // Si el día es CASA_ENTERA, mostrar estado de casa
    if (modalidadDia === 'CASA_ENTERA') {
      return dia.estadoCasa;
    }

    // Si el día es HABITACIONES, mostrar disponibilidad de habitaciones
    if (modalidadDia === 'HABITACIONES') {
      if (!dia.habitaciones || dia.habitaciones.length === 0) {
        return 'NO_DISPONIBLE';
      }
      const habitacionesLibres = dia.habitaciones.filter(
        (h) => h.estado === 'LIBRE'
      ).length;
      if (habitacionesLibres > 0) {
        return 'LIBRE';
      }
      const habitacionesReservadas = dia.habitaciones.filter(
        (h) => h.estado === 'RESERVADA'
      ).length;
      return habitacionesReservadas > 0 ? 'RESERVADA' : 'NO_DISPONIBLE';
    }

    // Si el día es AMBAS, considerar el tipo de reserva seleccionado
    if (modalidadDia === 'AMBAS') {
      if (tipoReserva === 'CASA_COMPLETA') {
        return dia.estadoCasa;
      } else if (tipoReserva === 'HABITACIONES') {
        if (!dia.habitaciones || dia.habitaciones.length === 0) {
          return 'NO_DISPONIBLE';
        }
        const habitacionesLibres = dia.habitaciones.filter(
          (h) => h.estado === 'LIBRE'
        ).length;
        if (habitacionesLibres > 0) {
          return 'LIBRE';
        }
        const habitacionesReservadas = dia.habitaciones.filter(
          (h) => h.estado === 'RESERVADA'
        ).length;
        return habitacionesReservadas > 0 ? 'RESERVADA' : 'NO_DISPONIBLE';
      }
    }

    return dia.estadoCasa;
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

  private obtenerNumeroHabitacion(codigo: string | undefined): number {
    if (!codigo) {
      return Number.MAX_SAFE_INTEGER;
    }

    try {
      const numero = codigo.replace(/\D+/g, '');
      return numero ? parseInt(numero, 10) : Number.MAX_SAFE_INTEGER;
    } catch (e) {
      return Number.MAX_SAFE_INTEGER;
    }
  }
}