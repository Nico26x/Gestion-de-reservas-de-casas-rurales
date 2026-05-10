import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Casa } from '../../../core/models/casas/casa.model';
import { CasasService } from '../../../core/services/casas/casas.service';

@Component({
  selector: 'app-detalle-casa',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './detalle-casa.html',
  styleUrl: './detalle-casa.css'
})
export class DetalleCasaComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private casasService = inject(CasasService);

  casaId: number | null = null;
  casa: Casa | null = null;
  cargando = false;
  mensajeError: string | null = null;

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id && !isNaN(+id)) {
        this.casaId = +id;
        this.cargarDetalleCasa();
      } else {
        this.mensajeError = 'ID de casa inválido.';
      }
    });
  }

  cargarDetalleCasa(): void {
    if (!this.casaId) {
      this.mensajeError = 'No se pudo obtener el ID de la casa.';
      return;
    }

    this.cargando = true;
    this.mensajeError = null;

    this.casasService.obtenerCasaDetalle(this.casaId).subscribe({
      next: (data) => {
        this.casa = data;
        this.cargando = false;
      },
      error: (error) => {
        this.cargando = false;
        this.mensajeError = this.extraerMensajeError(error);
      }
    });
  }

  volver(): void {
    this.router.navigate(['/casas/buscar']);
  }

  extraerMensajeError(error: any): string {
    if (error?.error?.message) {
      return error.error.message;
    }
    if (error?.error?.mensaje) {
      return error.error.mensaje;
    }
    if (typeof error?.error === 'string') {
      return error.error;
    }
    return 'Error al cargar la casa. Intenta de nuevo.';
  }

  tieneFotos(): boolean {
    return this.casa?.fotos !== undefined && this.casa.fotos.length > 0;
  }

  tieneHabitaciones(): boolean {
    return this.casa?.habitaciones !== undefined && this.casa.habitaciones.length > 0;
  }
}
