import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DisponibilidadRequest } from '../../models/disponibilidad/disponibilidad-request.model';
import { DisponibilidadResponse } from '../../models/disponibilidad/disponibilidad-response.model';
import { DisponibilidadDiaResponse } from '../../models/disponibilidad/disponibilidad-dia-response.model';

@Injectable({
  providedIn: 'root'
})
export class DisponibilidadService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/disponibilidad`;

  registrarDisponibilidad(data: DisponibilidadRequest): Observable<DisponibilidadResponse> {
    return this.http.post<DisponibilidadResponse>(this.apiUrl, data);
  }

  consultarDisponibilidad(
    casaId: number,
    fechaEntrada: string,
    numeroNoches: number
  ): Observable<DisponibilidadDiaResponse[]> {
    const params = new HttpParams()
      .set('casaId', casaId)
      .set('fechaEntrada', fechaEntrada)
      .set('numeroNoches', numeroNoches);

    return this.http.get<DisponibilidadDiaResponse[]>(this.apiUrl, { params });
  }
}