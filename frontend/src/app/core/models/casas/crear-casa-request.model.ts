export interface CrearCasaRequest {
  nombre: string;
  direccion: string;
  poblacion: string;
  numeroHabitaciones: number;
  numeroBanos: number;
  numeroCocinas: number;
  numeroCamas: number;
  tieneBano: boolean;
  tipoCama: string;
  foto: File;
}