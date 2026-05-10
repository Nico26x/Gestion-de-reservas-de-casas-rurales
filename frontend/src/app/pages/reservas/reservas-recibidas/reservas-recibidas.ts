import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
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
}
