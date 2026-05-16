import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import Swal from 'sweetalert2';
import { PagosService } from '../../../core/services/pagos/pagos.service';
import { PagoRequest } from '../../../core/models/pagos/pago-request.model';
import { PagoResponse } from '../../../core/models/pagos/pago-response.model';
import { Pago } from '../../../core/models/pagos/pago.model';
import {
  fireErrorAlert,
  fireSuccessAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';

/**
 * Componente para registrar pagos de reservas
 * Permite a clientes registrar pagos y a propietarios verificarlos
 */
@Component({
  selector: 'app-registrar-pago',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './registrar-pago.html',
  styleUrl: './registrar-pago.css'
})
export class RegistrarPagoComponent implements OnInit {
  private fb = inject(FormBuilder);
  private pagosService = inject(PagosService);

  // Variables de control de estado
  cargandoPagos = false;
  registrandoPago = false;
  verificandoId: number | null = null;

  // Mensajes de usuario
  mensajeExito: string | null = null;
  mensajeError: string | null = null;

  // Datos
  pagosPendientes: Pago[] = [];
  pagoResponseData: PagoResponse | null = null;

  // Formulario reactivo
  pagoForm: FormGroup;

  // Métodos de pago disponibles
  metodosPago = ['EFECTIVO', 'TRANSFERENCIA', 'TARJETA'] as const;

  constructor() {
    this.pagoForm = this.fb.group({
      numeroReserva: [
        null as number | null,
        [Validators.required, Validators.min(1)]
      ],
      monto: [
        null as number | null,
        [Validators.required, Validators.min(0.01)]
      ],
      metodoPago: ['', Validators.required],
      fechaPago: ['']
    });
  }

  ngOnInit(): void {
    this.cargarPagosPendientes();
  }

  /**
   * Carga los pagos pendientes de verificación
   * Solo propietarios verán pagos reales
   */
  cargarPagosPendientes(): void {
    this.cargandoPagos = true;
    this.limpiarMensajes();

    this.pagosService.obtenerPagosPendientes().subscribe({
      next: (pagos: Pago[]) => {
        this.pagosPendientes = pagos;
        this.cargandoPagos = false;
      },
      error: (err: any) => {
        console.error('Error cargando pagos pendientes:', err);
        // No mostrar error si es 403 (no propietario) o similar
        // Los clientes no tienen pagos pendientes para verificar
        this.cargandoPagos = false;
      }
    });
  }

  /**
   * Registra un nuevo pago
   * Solo para clientes
   */
  registrarPago(): void {
    if (this.pagoForm.invalid) {
      this.pagoForm.markAllAsTouched();
      fireWarningAlert(
        'Formulario incompleto',
        'Debes diligenciar correctamente los datos del pago.'
      );
      return;
    }

    const pagoData = this.construirPagoRequest();

    this.registrandoPago = true;
    this.limpiarMensajes();

    this.pagosService.registrarPago(pagoData).subscribe({
      next: (response: PagoResponse) => {
        this.registrandoPago = false;
        this.pagoResponseData = response;
        this.mensajeExito = response.mensaje;

        fireSuccessAlert(
          'Pago registrado',
          `Pago de $${response.monto} registrado correctamente. Número de reserva: ${response.numeroReserva}`
        );

        this.resetFormulario();
      },
      error: (err: any) => {
        this.registrandoPago = false;
        const errorMsg = err?.error?.mensaje || err?.error?.message || 'Error al registrar el pago';
        this.mensajeError = errorMsg;

        fireErrorAlert(
          'Error al registrar pago',
          errorMsg
        );
      }
    });
  }

  /**
   * Verifica un pago (solo propietarios)
   */
  async verificarPago(pagoId: number): Promise<void> {
    const resultado = await Swal.fire({
      title: '¿Verificar pago?',
      text: '¿Confirmas que verificaste este pago?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Verificar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#3274dc',
      cancelButtonColor: '#999',
      background: '#ffffff',
      color: '#10243a'
    });

    if (!resultado.isConfirmed) {
      return;
    }

    this.verificandoId = pagoId;
    this.limpiarMensajes();

    this.pagosService.verificarPago(pagoId).subscribe({
      next: (response: PagoResponse) => {
        this.verificandoId = null;
        this.pagoResponseData = response;
        this.mensajeExito = response.mensaje;

        fireSuccessAlert(
          'Pago verificado',
          `Pago de $${response.monto} verificado. Reserva ${response.numeroReserva} ahora ${response.estadoReserva}`
        );

        // Recargar lista de pagos pendientes
        this.cargarPagosPendientes();
      },
      error: (err: any) => {
        this.verificandoId = null;
        const errorMsg = err?.error?.mensaje || err?.error?.message || 'Error al verificar pago';
        this.mensajeError = errorMsg;

        fireErrorAlert(
          'Error al verificar pago',
          errorMsg
        );
      }
    });
  }

  /**
   * Limpia los mensajes de éxito y error
   */
  limpiarMensajes(): void {
    this.mensajeExito = null;
    this.mensajeError = null;
  }

  /**
   * Reinicia el formulario a su estado inicial
   */
  resetFormulario(): void {
    this.pagoForm.reset();
    this.pagoForm.markAsUntouched();
    this.pagoForm.markAsPristine();
  }

  /**
   * Construye el objeto PagoRequest a partir del formulario
   */
  private construirPagoRequest(): PagoRequest {
    const formValue = this.pagoForm.value;

    const request: PagoRequest = {
      numeroReserva: Number(formValue.numeroReserva),
      monto: Number(formValue.monto),
      metodoPago: formValue.metodoPago
    };

    // Agregar fecha si fue proporcionada
    if (formValue.fechaPago) {
      request.fechaPago = new Date(formValue.fechaPago);
    }

    return request;
  }

  /**
   * Retorna true si un control del formulario tiene error y fue tocado
   */
  hasError(fieldName: string, errorType: string): boolean {
    const field = this.pagoForm.get(fieldName);
    return !!(field && field.hasError(errorType) && (field.dirty || field.touched));
  }

  /**
   * Retorna true si un control del formulario es inválido
   */
  isFieldInvalid(fieldName: string): boolean {
    const field = this.pagoForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
}
