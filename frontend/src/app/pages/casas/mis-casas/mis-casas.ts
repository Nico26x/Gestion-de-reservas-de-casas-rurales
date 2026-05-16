import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CasasService } from '../../../core/services/casas/casas.service';
import { Casa } from '../../../core/models/casas/casa.model';
import {
  fireErrorAlert,
  fireSuccessAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-mis-casas',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './mis-casas.html',
  styleUrl: './mis-casas.css'
})
export class MisCasasComponent implements OnInit {
  private casasService = inject(CasasService);

  casas: Casa[] = [];
  cargando = false;
  eliminandoId: number | null = null;

  ngOnInit(): void {
    this.cargarMisCasas();
  }

  cargarMisCasas(): void {
    this.cargando = true;
    this.casasService.listarMisCasas().subscribe({
      next: (data) => {
        this.casas = data;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar casas:', error);
        fireErrorAlert('Error', 'No se pudieron cargar tus casas');
        this.cargando = false;
      }
    });
  }

  eliminarCasa(casa: Casa): void {
    if (!casa.id) {
      fireErrorAlert('Error', 'ID de casa no válido');
      return;
    }

    const confirmacion = window.confirm(
      `¿Estás seguro de que deseas eliminar esta casa? Esta acción no se puede deshacer.`
    );

    if (!confirmacion) {
      return;
    }

    this.eliminandoId = casa.id;

    this.casasService.eliminarCasa(casa.id).subscribe({
      next: (response) => {
        fireSuccessAlert('Éxito', 'Casa eliminada correctamente');
        this.casas = this.casas.filter(c => c.id !== casa.id);
        this.eliminandoId = null;
      },
      error: (error) => {
        const mensajeError = error.error || 
          'No se pudo eliminar la casa. Verifica que no tenga reservas activas o que tengas permisos.';
        fireErrorAlert('Error', mensajeError);
        this.eliminandoId = null;
      }
    });
  }
}
