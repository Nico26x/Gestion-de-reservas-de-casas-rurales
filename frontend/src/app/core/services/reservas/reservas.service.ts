import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReservaNotificacion } from '../../models/reservas/reserva-notificacion.model';

export interface ReservaResponseDTO {
  reservaId?: number;
  numeroReserva?: number;
  fechaEntrada?: string;
  numeroNoches?: number;
  importe?: number;
  anticipo?: number;
  telefonoCliente?: string;
  nombreCasa?: string;
  numeroCuentaBancaria?: string;
  estado?: string;
  estadoReserva?: string;
  mensaje?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReservasService {
  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/reservas`;

  obtenerNotificacionesReservas(): Observable<ReservaNotificacion[]> {
    return this.http.get<ReservaNotificacion[]>(`${this.apiUrl}/propietario`);
  }

  cancelarReserva(reservaId: number): Observable<string> {
    return this.http.put(
      `${this.apiUrl}/${reservaId}/cancelar`,
      {},
      { responseType: 'text' }
    );
  }

  listarReservasVencidas(): Observable<ReservaResponseDTO[]> {
    return this.http.get<ReservaResponseDTO[]>(`${this.apiUrl}/vencidas`);
  }

  gestionarReservaVencida(reservaId: number, accion: 'ANULAR' | 'MANTENER'): Observable<ReservaResponseDTO> {
    return this.http.put<ReservaResponseDTO>(
      `${this.apiUrl}/${reservaId}/vencida?accion=${accion}`,
      {}
    );
  }
}
