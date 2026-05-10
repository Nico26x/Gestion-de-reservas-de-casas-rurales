/**
 * Response DTO recibido del backend
 * Respuesta de endpoints: POST /api/pagos y PUT /api/pagos/{id}/verificar
 */
export interface PagoResponse {
  idPago: number;
  numeroReserva: number;
  monto: number;
  fechaPago: string; // ISO 8601 date string
  metodoPago: string;
  importeTotal: number;
  anticipo: number;
  montoRestante: number;
  numeroCuentaBancaria: string;
  estadoPago: 'PENDIENTE_VERIFICACION' | 'VERIFICADO';
  estadoReserva: string;
  mensaje: string;
}
