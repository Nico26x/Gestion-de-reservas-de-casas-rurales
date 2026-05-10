/**
 * Request DTO para registrar un nuevo pago
 * Enviado al endpoint POST /api/pagos
 */
export interface PagoRequest {
  numeroReserva: number;
  monto: number;
  fechaPago?: Date;
  metodoPago: 'EFECTIVO' | 'TRANSFERENCIA' | 'TARJETA';
}
