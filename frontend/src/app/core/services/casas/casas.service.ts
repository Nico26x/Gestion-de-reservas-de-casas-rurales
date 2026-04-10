import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CrearCasaRequest } from '../../models/casas/crear-casa-request.model';
import { Casa } from '../../models/casas/casa.model';

@Injectable({
  providedIn: 'root'
})
export class CasasService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/casas`;

  crearCasa(data: CrearCasaRequest): Observable<Casa> {
    const formData = new FormData();
    formData.append('nombre', data.nombre);
    formData.append('direccion', data.direccion);
    formData.append('numeroHabitaciones', String(data.numeroHabitaciones));
    formData.append('numeroBanos', String(data.numeroBanos));
    formData.append('numeroCocinas', String(data.numeroCocinas));
    formData.append('foto', data.foto);

    return this.http.post<Casa>(this.apiUrl, formData);
  }
}