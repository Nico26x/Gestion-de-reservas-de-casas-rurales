import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReservasService, ReservaResponseDTO } from '../../../core/services/reservas/reservas.service';
import Swal from 'sweetalert2';
import {
  fireErrorAlert,
  fireSuccessAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-reservas-vencidas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reservas-vencidas.html',
  styleUrl: './reservas-vencidas.css'
})
export class ReservasVencidasComponent implements OnInit {
  private reservasService = inject(ReservasService);

  reservas: ReservaResponseDTO[] = [];
  cargando = false;
  procesandoId: number | null = null;
  error = '';

  ngOnInit(): void {
    this.cargarReservasVencidas();
  }

  cargarReservasVencidas(): void {
    this.error = '';
    this.cargando = true;
    this.reservasService.listarReservasVencidas().subscribe({
      next: (data) => {
        this.reservas = data;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar reservas vencidas:', error);
        const mensajeError = this.obtenerMensajeError(error);
        fireErrorAlert('Error', mensajeError);
        this.error = mensajeError;
        this.cargando = false;
      }
    });
  }

  gestionarReserva(reserva: ReservaResponseDTO, accion: 'ANULAR' | 'MANTENER'): void {
    if (!reserva.reservaId) {
      fireErrorAlert('Error', 'No se puede gestionar la reserva porque no se recibió el id interno.');
      return;
    }

    const reservaId = reserva.reservaId;
    const titulo = accion === 'ANULAR' ? '¿Anular esta reserva?' : '¿Mantener esta reserva?';
    const mensaje = accion === 'ANULAR'
      ? 'Estás a punto de anular esta reserva vencida. Esta acción liberará la disponibilidad.'
      : 'Mantenerás esta reserva como pendiente de gestionar.';
    const confirmText = accion === 'ANULAR' ? 'Sí, anular' : 'Sí, mantener';
    const confirmButtonColor = accion === 'ANULAR' ? '#c33a3a' : '#3274dc';

    Swal.fire({
      title: titulo,
      text: mensaje,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: confirmButtonColor,
      cancelButtonColor: '#999999',
      confirmButtonText: confirmText,
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (!result.isConfirmed) {
        return;
      }

      this.procesandoId = reservaId;

      this.reservasService.gestionarReservaVencida(reservaId, accion).subscribe({
        next: (response) => {
          const mensaje = accion === 'ANULAR'
            ? 'Reserva vencida anulada correctamente'
            : 'Reserva vencida mantenida correctamente';
          fireSuccessAlert('Éxito', mensaje);
          this.reservas = this.reservas.filter(r => r.reservaId !== reservaId);
          this.procesandoId = null;
        },
        error: (error) => {
          const mensajeError = this.obtenerMensajeError(error);
          fireErrorAlert('Error', mensajeError);
          this.procesandoId = null;
        }
      });
    });
  }

  obtenerEstado(reserva: ReservaResponseDTO): string {
    return reserva.estado || reserva.estadoReserva || 'Desconocido';
  }

  private obtenerMensajeError(error: any): string {
    if (typeof error?.error === 'string') {
      return error.error;
    }

    if (error?.error?.message) {
      return error.error.message;
    }

    if (error?.message) {
      return error.message;
    }

    return 'No se pudo gestionar la reserva vencida.';
  }
}
