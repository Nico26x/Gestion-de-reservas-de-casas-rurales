import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import Swal from 'sweetalert2';
import { ReservasService } from '../../../core/services/reservas/reservas.service';
import { ReservaNotificacion } from '../../../core/models/reservas/reserva-notificacion.model';
import {
  fireErrorAlert,
  fireSuccessAlert
} from '../../../shared/utils/sweet-alert.util';
import { Router } from '@angular/router';

@Component({
  selector: 'app-reservas-recibidas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reservas-recibidas.html',
  styleUrl: './reservas-recibidas.css'
})
export class ReservasRecibidasComponent implements OnInit {
  private reservasService = inject(ReservasService);
  private router = inject(Router);

  reservas: ReservaNotificacion[] = [];
  loading = false;
  cancelandoId: number | null = null;

  ngOnInit(): void {
    this.cargarReservas();
  }

  cargarReservas(): void {
    this.loading = true;
    this.reservasService.obtenerNotificacionesReservas().subscribe({
      next: (data) => {
        this.reservas = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar reservas:', error);
        fireErrorAlert('Error', 'No se pudieron cargar las reservas');
        this.loading = false;
      }
    });
  }

  copiarNumeroReserva(numero: number): void {
    navigator.clipboard.writeText(numero.toString()).then(() => {
      fireSuccessAlert('Copiado', 'Número de reserva copiado al portapapeles');
    });
  }

  irARegistrarPago(numeroReserva: number): void {
    this.router.navigate(['/pagos/registrar'], { queryParams: { numeroReserva } });
  }

  async cancelarReserva(reserva: ReservaNotificacion): Promise<void> {
    const resultado = await Swal.fire({
      title: '¿Cancelar reserva?',
      text: `¿Estás seguro de que deseas cancelar la reserva ${reserva.numeroReserva}?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Aceptar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      background: '#ffffff',
      color: '#10243a'
    });

    if (!resultado.isConfirmed) {
      return;
    }

    this.cancelandoId = reserva.reservaId;

    this.reservasService.cancelarReserva(reserva.reservaId).subscribe({
      next: (response) => {
        fireSuccessAlert('Éxito', 'Reserva cancelada correctamente');
        reserva.estadoReserva = 'CANCELADA';
        this.cancelandoId = null;
      },
      error: (error) => {
        const mensajeError = error.error || 'No se pudo cancelar la reserva';
        fireErrorAlert('Error', mensajeError);
        this.cancelandoId = null;
      }
    });
  }

  obtenerColorEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE':
        return 'orange';
      case 'CONFIRMADA':
        return 'green';
      case 'EXPIRADA':
        return 'red';
      case 'CANCELADA':
        return 'gray';
      default:
        return 'black';
    }
  }

  obtenerClaseEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE':
        return 'pendiente';
      case 'CONFIRMADA':
        return 'confirmada';
      case 'EXPIRADA':
        return 'expirada';
      case 'CANCELADA':
        return 'cancelada';
      default:
        return 'default';
    }
  }
}
