import { HabitacionDisponibilidadResponse } from './habitacion-disponibilidad-response.model';

export interface DisponibilidadDiaResponse {
  fecha: string;
  modalidad: string;
  estadoCasa: string;
  habitaciones: HabitacionDisponibilidadResponse[];
}