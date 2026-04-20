export interface CrearPaqueteRequest {
  fechaInicio: string;
  fechaFin: string;
  precio?: number;
  precioHabitacion?: number;
  casaId: number;
  modalidad: string;
}