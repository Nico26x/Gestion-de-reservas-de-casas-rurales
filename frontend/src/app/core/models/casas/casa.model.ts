export interface Habitacion {
  id?: number;
  codigoHabitacion?: string;
  numeroCamas?: number;
  tipoCama?: string;
  tieneBano?: boolean;
}

export interface Casa {
  id?: number;
  nombre?: string;
  direccion?: string;
  poblacion?: string;
  numeroHabitaciones?: number;
  numeroBanos?: number;
  numeroCocinas?: number;
  descripcion?: string;
  foto?: string;
  habitaciones?: Habitacion[];
}