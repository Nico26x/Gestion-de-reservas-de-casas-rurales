export interface Paquete {
  id?: number;
  fechaInicio?: string;
  fechaFin?: string;
  precio?: number | null;
  precioHabitacion?: number | null;
  modalidad?: string;
  casaId?: number;
  nombreCasa?: string;
  poblacionCasa?: string;
}