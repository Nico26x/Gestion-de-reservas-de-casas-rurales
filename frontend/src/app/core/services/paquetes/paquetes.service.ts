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
}