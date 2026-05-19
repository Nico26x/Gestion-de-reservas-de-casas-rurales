export interface CrearCasaRequest {
  nombre: string;
  direccion: string;
  poblacion: string;
  descripcion?: string;
  numeroHabitaciones: number;
  numeroBanos: number;
  numeroCocinas: number;
  numeroComedores: number;
  numeroGarajes: number;
  numeroCamas: number;
  tieneBano: boolean;
  tipoCama: string;
  fotos: File[];
  habitacionesJson?: string;
  cocinasJson?: string;
}