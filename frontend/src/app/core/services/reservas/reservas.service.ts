import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReservaNotificacion } from '../../models/reservas/reserva-notificacion.model';

@Injectable({
  providedIn: 'root'
})
export class ReservasService {
  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/reservas`;

  obtenerNotificacionesReservas(): Observable<ReservaNotificacion[]> {
    return this.http.get<ReservaNotificacion[]>(`${this.apiUrl}/propietario`);
  }
}
