export interface ReservaNotificacion {
  reservaId: number;
  numeroReserva: number;
  casaId: number;
  nombreCasa: string;
  poblacionCasa: string;
  fechaEntrada: string;
  numeroNoches: number;
  telefonoCliente: string;
  importeTotal: number;
  anticipo: number;
  estadoReserva: string;
  fechaCreacion?: string;
}
