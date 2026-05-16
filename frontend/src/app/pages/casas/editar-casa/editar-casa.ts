import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CasasService } from '../../../core/services/casas/casas.service';
import { Casa } from '../../../core/models/casas/casa.model';
import {
  fireErrorAlert,
  fireSuccessAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-editar-casa',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editar-casa.html',
  styleUrl: './editar-casa.css'
})
export class EditarCasaComponent implements OnInit {
  private casasService = inject(CasasService);
  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  cargando = false;
  guardando = false;
  error = '';
  casaId!: number;
  fotosSeleccionadas: File[] = [];
  fotosActuales: string[] = [];

  formulario = this.fb.group({
    nombre: ['', [Validators.required]],
    direccion: ['', [Validators.required]],
    poblacion: ['', [Validators.required]],
    descripcion: [''],
    numeroHabitaciones: [0, [Validators.required, Validators.min(3)]],
    numeroBanos: [0, [Validators.required, Validators.min(2)]],
    numeroCocinas: [0, [Validators.required, Validators.min(1)]],
    numeroComedores: [0, [Validators.required, Validators.min(1)]],
    numeroCamas: [0, [Validators.required, Validators.min(1)]],
    numeroGarajes: [0, [Validators.required, Validators.min(0)]],
    tieneBano: [false, [Validators.required]],
    tipoCama: ['', [Validators.required]]
  });

  ngOnInit(): void {
    const id = this.activatedRoute.snapshot.paramMap.get('id');
    
    if (!id || isNaN(Number(id))) {
      this.error = 'ID de casa no válido';
      fireErrorAlert('Error', this.error);
      return;
    }

    this.casaId = Number(id);
    this.cargarCasa();
  }

  cargarCasa(): void {
    this.error = '';
    this.cargando = true;

    this.casasService.obtenerCasaPorId(this.casaId).subscribe({
      next: (casa: Casa) => {
        // Obtener tipoCama de la primera habitación si existe
        const tipoCama = casa.habitaciones && casa.habitaciones.length > 0 
          ? casa.habitaciones[0].tipoCama || ''
          : '';

        this.formulario.patchValue({
          nombre: casa.nombre || '',
          direccion: casa.direccion || '',
          poblacion: casa.poblacion || '',
          descripcion: casa.descripcion || '',
          numeroHabitaciones: casa.numeroHabitaciones || 0,
          numeroBanos: casa.numeroBanos || 0,
          numeroCocinas: casa.numeroCocinas || 0,
          numeroComedores: casa.numeroComedores || 0,
          numeroCamas: casa.numeroHabitaciones || 0,
          numeroGarajes: casa.numeroGarajes || 0,
          tieneBano: casa.fotos && casa.fotos.length > 0 ? true : false,
          tipoCama: tipoCama
        });

        if (casa.fotos && casa.fotos.length > 0) {
          this.fotosActuales = casa.fotos;
        }

        this.cargando = false;
      },
      error: (error) => {
        const mensajeError = this.obtenerMensajeError(error);
        this.error = mensajeError;
        fireErrorAlert('Error', mensajeError);
        this.cargando = false;
      }
    });
  }

  onFotosSeleccionadas(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.fotosSeleccionadas = Array.from(input.files);
    }
  }

  guardarCambios(): void {
    if (this.formulario.invalid) {
      Object.keys(this.formulario.controls).forEach(key => {
        this.formulario.get(key)?.markAsTouched();
      });
      fireErrorAlert('Validación', 'Por favor completa todos los campos requeridos con valores válidos.');
      return;
    }

    this.guardando = true;
    const valores = this.formulario.value;

    const formData = new FormData();
    formData.append('nombre', String(valores.nombre));
    formData.append('direccion', String(valores.direccion));
    formData.append('poblacion', String(valores.poblacion));
    formData.append('descripcion', String(valores.descripcion ?? ''));
    formData.append('numeroHabitaciones', String(valores.numeroHabitaciones));
    formData.append('numeroBanos', String(valores.numeroBanos));
    formData.append('numeroCocinas', String(valores.numeroCocinas));
    formData.append('numeroComedores', String(valores.numeroComedores));
    formData.append('numeroCamas', String(valores.numeroCamas));
    formData.append('numeroGarajes', String(valores.numeroGarajes));
    formData.append('tieneBano', String(valores.tieneBano));
    formData.append('tipoCama', String(valores.tipoCama));

    if (this.fotosSeleccionadas.length > 0) {
      for (const foto of this.fotosSeleccionadas) {
        formData.append('fotos', foto);
      }
    }

    this.casasService.actualizarCasa(this.casaId, formData).subscribe({
      next: (response) => {
        fireSuccessAlert('Éxito', 'Casa actualizada correctamente');
        this.guardando = false;
        this.router.navigate(['/casas/mis-casas']);
      },
      error: (error) => {
        const mensajeError = this.obtenerMensajeError(error);
        fireErrorAlert('Error', mensajeError);
        this.guardando = false;
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/casas/mis-casas']);
  }

  private obtenerMensajeError(error: any): string {
    if (typeof error?.error === 'string') {
      return error.error;
    }

    if (error?.error?.message) {
      return error.error.message;
    }

    if (error?.message) {
      return error.message;
    }

    return 'No se pudo actualizar la casa. Verifica los datos ingresados.';
  }
}
