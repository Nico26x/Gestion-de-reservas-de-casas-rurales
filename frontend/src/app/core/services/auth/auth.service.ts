import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest } from '../../models/auth/login-request.model';
import { AuthResponse } from '../../models/auth/auth-response.model';
import { RegisterRequest } from '../../models/auth/register-request.model';
import { RegisterResponse } from '../../models/auth/register-response.model';
import { StorageService } from './storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private storageService = inject(StorageService);

  private apiUrl = `${environment.apiUrl}/auth`;

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, data).pipe(
      tap((response) => {
        this.storageService.setToken(response.token);
        this.storageService.setNombreCuenta(response.nombreCuenta);
        this.storageService.setRol(response.rol);
      })
    );
  }

  register(data: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/registro`, data);
  }

  logout(): void {
    this.storageService.clearAuth();
  }

  isAuthenticated(): boolean {
    return this.storageService.isLoggedIn();
  }

  getToken(): string | null {
    return this.storageService.getToken();
  }

  getNombreCuenta(): string | null {
    return this.storageService.getNombreCuenta();
  }

  getRol(): string | null {
    return this.storageService.getRol();
  }
}