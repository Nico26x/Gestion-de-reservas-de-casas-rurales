import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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
    formData.append('poblacion', data.poblacion);
    if (data.descripcion) {
      formData.append('descripcion', data.descripcion);
    }
    formData.append('numeroHabitaciones', String(data.numeroHabitaciones));
    formData.append('numeroBanos', String(data.numeroBanos));
    formData.append('numeroCocinas', String(data.numeroCocinas));
    formData.append('numeroComedores', String(data.numeroComedores));
    formData.append('numeroGarajes', String(data.numeroGarajes));
    // Enviar múltiples archivos con la clave 'fotos'
    for (const foto of data.fotos) {
      formData.append('fotos', foto);
    }
    formData.append('numeroCamas', String(data.numeroCamas));
    formData.append('tieneBano', String(data.tieneBano));
    formData.append('tipoCama', data.tipoCama);

    return this.http.post<Casa>(this.apiUrl, formData);
  }

  buscarPorPoblacion(poblacion: string): Observable<Casa[]> {
    const params = new HttpParams().set('poblacion', poblacion);
    return this.http.get<Casa[]>(this.apiUrl, { params });
  }

  obtenerTodas(): Observable<Casa[]> {
    return this.http.get<Casa[]>(this.apiUrl);
  }

  obtenerCasaDetalle(id: number): Observable<Casa> {
    return this.http.get<Casa>(`${this.apiUrl}/${id}`);
  }

  listarMisCasas(): Observable<Casa[]> {
    return this.http.get<Casa[]>(`${this.apiUrl}/propietario`);
  }

  eliminarCasa(casaId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${casaId}`, { responseType: 'text' });
  }
}