import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CasasService } from '../../../core/services/casas/casas.service';
import { Casa } from '../../../core/models/casas/casa.model';
import {
  fireErrorAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-buscar-casas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './buscar-casas.html',
  styleUrl: './buscar-casas.css'
})
export class BuscarCasasComponent {
  private fb = inject(FormBuilder);
  private casasService = inject(CasasService);

  loading = false;
  resultados: Casa[] = [];

  busquedaForm = this.fb.group({
    poblacion: ['', [Validators.required, Validators.minLength(2)]]
  });

  buscar(): void {
    if (this.busquedaForm.invalid) {
      this.busquedaForm.markAllAsTouched();
      fireWarningAlert(
        'Búsqueda incompleta',
        'Debes ingresar una población para buscar casas.'
      );
      return;
    }

    this.loading = true;
    this.resultados = [];

    const poblacion = this.busquedaForm.value.poblacion!.trim();

    this.casasService.buscarPorPoblacion(poblacion).subscribe({
      next: (response) => {
        this.loading = false;
        this.resultados = response;
      },
      error: (error) => {
        this.loading = false;
        fireErrorAlert(
          'Error al buscar casas',
          error?.error?.mensaje ||
            error?.error?.message ||
            'No se pudieron obtener las casas.'
        );
      }
    });
  }

  cargarTodas(): void {
    this.loading = true;
    this.resultados = [];

    this.casasService.obtenerTodas().subscribe({
      next: (response) => {
        this.loading = false;
        this.resultados = response;
      },
      error: (error) => {
        this.loading = false;
        fireErrorAlert(
          'Error al cargar casas',
          error?.error?.mensaje ||
            error?.error?.message ||
            'No se pudieron obtener las casas.'
        );
      }
    });
  }
}