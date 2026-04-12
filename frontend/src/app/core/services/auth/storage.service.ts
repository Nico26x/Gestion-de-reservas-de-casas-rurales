import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly TOKEN_KEY = 'token';
  private readonly NOMBRE_CUENTA_KEY = 'nombreCuenta';
  private readonly ROL_KEY = 'rol';

  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  setNombreCuenta(nombreCuenta: string): void {
    localStorage.setItem(this.NOMBRE_CUENTA_KEY, nombreCuenta);
  }

  getNombreCuenta(): string | null {
    return localStorage.getItem(this.NOMBRE_CUENTA_KEY);
  }

  removeNombreCuenta(): void {
    localStorage.removeItem(this.NOMBRE_CUENTA_KEY);
  }

  setRol(rol: string): void {
    localStorage.setItem(this.ROL_KEY, rol);
  }

  getRol(): string | null {
    return localStorage.getItem(this.ROL_KEY);
  }

  removeRol(): void {
    localStorage.removeItem(this.ROL_KEY);
  }

  clearAuth(): void {
    this.removeToken();
    this.removeNombreCuenta();
    this.removeRol();
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}