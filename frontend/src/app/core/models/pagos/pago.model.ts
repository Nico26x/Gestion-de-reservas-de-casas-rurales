/**
 * Modelo de pago pendiente (para listado)
 * Respuesta de GET /api/pagos/pendientes
 */
export interface Pago {
  id: number;
  fechaPago: string; // ISO 8601 date string
  monto: number;
  metodoPago: string;
  estadoPago: 'PENDIENTE_VERIFICACION' | 'VERIFICADO';
  reserva?: {
    id: number;
    numeroReserva: number;
    importe: number;
    estadoReserva: string;
  };
}
