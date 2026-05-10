import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { PaquetesService } from '../../../core/services/paquetes/paquetes.service';
import { Paquete } from '../../../core/models/paquetes/paquete.model';

@Component({
  selector: 'app-mis-paquetes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mis-paquetes.html',
  styleUrl: './mis-paquetes.css'
})
export class MisPaquetesComponent implements OnInit {
  private paquetesService = inject(PaquetesService);
  private router = inject(Router);

  paquetes: Paquete[] = [];
  cargando = false;
  mensajeError: string | null = null;
  mensajeExito: string | null = null;

  ngOnInit(): void {
    this.cargarPaquetes();
  }

  cargarPaquetes(): void {
    this.cargando = true;
    this.mensajeError = null;
    this.mensajeExito = null;

    this.paquetesService.obtenerPaquetesDelPropietario().subscribe({
      next: (paquetes) => {
        this.cargando = false;
        this.paquetes = paquetes;
      },
      error: (error) => {
        this.cargando = false;
        this.mensajeError = this.extraerMensajeError(error);
      }
    });
  }

  editarPaquete(paqueteId: number): void {
    this.router.navigate([`/paquetes/${paqueteId}/editar`]);
  }

  refrescar(): void {
    this.cargarPaquetes();
  }

  private extraerMensajeError(error: any): string {
    if (error?.error?.message) {
      return error.error.message;
    }
    if (error?.error && typeof error.error === 'string') {
      return error.error;
    }
    if (error?.error?.mensaje) {
      return error.error.mensaje;
    }
    return 'Ocurrió un error al cargar los paquetes. Por favor, intenta de nuevo.';
  }
}
