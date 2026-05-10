import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PagoRequest } from '../../models/pagos/pago-request.model';
import { PagoResponse } from '../../models/pagos/pago-response.model';
import { Pago } from '../../models/pagos/pago.model';

/**
 * Servicio para consumir endpoints de pagos del backend
 * Maneja:
 * - Registro de pagos (clientes)
 * - Listado de pagos pendientes (propietarios)
 * - Verificación de pagos (propietarios)
 */
@Injectable({
  providedIn: 'root'
})
export class PagosService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/pagos`;

  /**
   * Registra un nuevo pago para una reserva
   * Endpoint: POST /api/pagos
   * 
   * @param pago - Datos del pago (numeroReserva, monto, metodoPago, fechaPago opcional)
   * @returns Observable con datos del pago creado y detalles de verificación
   */
  registrarPago(pago: PagoRequest): Observable<PagoResponse> {
    return this.http.post<PagoResponse>(this.apiUrl, pago);
  }

  /**
   * Obtiene los pagos pendientes de verificación
   * Solo para propietarios (validado en backend por JWT)
   * Endpoint: GET /api/pagos/pendientes
   * 
   * @returns Observable con lista de pagos pendientes
   */
  obtenerPagosPendientes(): Observable<Pago[]> {
    return this.http.get<Pago[]>(`${this.apiUrl}/pendientes`);
  }

  /**
   * Verifica un pago y confirma la reserva si aplica
   * Solo para propietarios (validado en backend por JWT)
   * Endpoint: PUT /api/pagos/{id}/verificar
   * 
   * @param pagoId - ID del pago a verificar
   * @returns Observable con datos actualizados del pago
   */
  verificarPago(pagoId: number): Observable<PagoResponse> {
    return this.http.put<PagoResponse>(`${this.apiUrl}/${pagoId}/verificar`, {});
  }
}
