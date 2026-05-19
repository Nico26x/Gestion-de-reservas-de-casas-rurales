export interface Habitacion {
  id?: number;
  codigoHabitacion?: string;
  numeroCamas?: number;
  tipoCama?: string;
  tieneBano?: boolean;
}

export interface Cocina {
  id?: number;
  lavavajillas?: boolean;
  lavadora?: boolean;
}

export interface CasaFoto {
  id?: number;
  url?: string;
}

export interface Casa {
  id?: number;
  nombre?: string;
  direccion?: string;
  poblacion?: string;
  numeroHabitaciones?: number;
  numeroBanos?: number;
  numeroCocinas?: number;
  numeroComedores?: number;
  numeroGarajes?: number;
  descripcion?: string;
  foto?: string;
  fotos?: string[];
  habitaciones?: Habitacion[];
  cocinas?: Cocina[];
}