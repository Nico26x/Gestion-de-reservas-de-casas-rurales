import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CrearPaqueteRequest } from '../../models/paquetes/crear-paquete-request.model';
import { Paquete } from '../../models/paquetes/paquete.model';

@Injectable({
  providedIn: 'root'
})
export class PaquetesService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/paquetes`;

  crearPaquete(data: CrearPaqueteRequest): Observable<Paquete> {
    return this.http.post<Paquete>(this.apiUrl, data);
  }

  obtenerPaquete(id: number): Observable<Paquete> {
    return this.http.get<Paquete>(`${this.apiUrl}/${id}`);
  }

  obtenerPaquetesPorCasa(casaId: number): Observable<Paquete[]> {
    return this.http.get<Paquete[]>(this.apiUrl, { params: { casaId: casaId.toString() } });
  }

  obtenerPaquetesDelPropietario(): Observable<Paquete[]> {
    return this.http.get<Paquete[]>(`${this.apiUrl}/propietario`);
  }

  modificarPaquete(id: number, data: CrearPaqueteRequest): Observable<Paquete> {
    return this.http.put<Paquete>(`${this.apiUrl}/${id}`, data);
  }
}