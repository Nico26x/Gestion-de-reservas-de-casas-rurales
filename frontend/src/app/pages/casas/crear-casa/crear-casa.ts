import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CasasService } from '../../../core/services/casas/casas.service';
import { CrearCasaRequest } from '../../../core/models/casas/crear-casa-request.model';
import {
  fireErrorAlert,
  fireSuccessAlert,
  fireWarningAlert
} from '../../../shared/utils/sweet-alert.util';

@Component({
  selector: 'app-crear-casa',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './crear-casa.html',
  styleUrl: './crear-casa.css'
})
export class CrearCasaComponent {
  @ViewChild('fotoInput') fotoInput?: ElementRef<HTMLInputElement>;

  private fb = inject(FormBuilder);
  private casasService = inject(CasasService);

  loading = false;
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  private imagePreviewUrl: string | null = null;

  crearCasaForm = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    direccion: ['', [Validators.required, Validators.minLength(5)]],
    poblacion: ['', [Validators.required, Validators.minLength(2)]],
    descripcion: [''],
    numeroHabitaciones: [3, [Validators.required, Validators.min(3)]],
    numeroBanos: [2, [Validators.required, Validators.min(2)]],
    numeroCocinas: [1, [Validators.required, Validators.min(1)]],
    numeroCamas: [1, [Validators.required, Validators.min(1)]],
    tieneBano: [false, [Validators.required]],
    tipoCama: ['', [Validators.required]]
  });

  get nombre() {
    return this.crearCasaForm.get('nombre');
  }

  get direccion() {
    return this.crearCasaForm.get('direccion');
  }

  get poblacion() {
    return this.crearCasaForm.get('poblacion');
  }

  get descripcion() {
    return this.crearCasaForm.get('descripcion');
  }

  get numeroHabitaciones() {
    return this.crearCasaForm.get('numeroHabitaciones');
  }

  get numeroBanos() {
    return this.crearCasaForm.get('numeroBanos');
  }

  get numeroCocinas() {
    return this.crearCasaForm.get('numeroCocinas');
  }

  get numeroCamas() {
    return this.crearCasaForm.get('numeroCamas');
  }

  get tieneBano() {
    return this.crearCasaForm.get('tieneBano');
  }

  get tipoCama() {
    return this.crearCasaForm.get('tipoCama');
  }

  increment(field: 'numeroHabitaciones' | 'numeroBanos' | 'numeroCocinas'): void {
    const current = Number(this.crearCasaForm.get(field)?.value ?? 0);
    this.crearCasaForm.get(field)?.setValue(current + 1);
  }

  decrement(field: 'numeroHabitaciones' | 'numeroBanos' | 'numeroCocinas', min: number): void {
    const current = Number(this.crearCasaForm.get(field)?.value ?? min);
    if (current > min) {
      this.crearCasaForm.get(field)?.setValue(current - 1);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] || null;

    if (file) {
      // Diagnóstico: imprimir información del archivo
      const fileSizeMb = file.size / (1024 * 1024);
      console.log(`[FILE VALIDATION] Archivo seleccionado:`);
      console.log(`  Nombre: ${file.name}`);
      console.log(`  Tamaño (bytes): ${file.size}`);
      console.log(`  Tamaño (MB): ${fileSizeMb.toFixed(2)}MB`);
      console.log(`  Tipo MIME: ${file.type}`);

      // Validar tamaño máximo: 25MB
      const MAX_FILE_SIZE_MB = 25;
      if (fileSizeMb > MAX_FILE_SIZE_MB) {
        console.warn(`[FILE VALIDATION] RECHAZADO: Archivo supera ${MAX_FILE_SIZE_MB}MB`);
        
        fireWarningAlert(
          'Archivo demasiado grande',
          `La imagen pesa ${fileSizeMb.toFixed(2)}MB. El límite es ${MAX_FILE_SIZE_MB}MB.`
        );

        // Limpiar selección
        this.selectedFile = null;
        this.imagePreview = null;
        if (this.imagePreviewUrl) {
          URL.revokeObjectURL(this.imagePreviewUrl);
          this.imagePreviewUrl = null;
        }
        if (input) {
          input.value = '';
        }
        return;
      }

      // Archivo válido: procesar preview
      console.log(`[FILE VALIDATION] ACEPTADO: ${fileSizeMb.toFixed(2)}MB <= ${MAX_FILE_SIZE_MB}MB`);
      this.selectedFile = file;

      if (this.imagePreviewUrl) {
        URL.revokeObjectURL(this.imagePreviewUrl);
        this.imagePreviewUrl = null;
      }

      this.imagePreviewUrl = URL.createObjectURL(file);
      this.imagePreview = this.imagePreviewUrl;
    } else {
      this.selectedFile = null;
      this.imagePreview = null;
    }
  }

  private resetFormState(): void {
    this.crearCasaForm.reset({
      nombre: '',
      direccion: '',
      poblacion: '',
      descripcion: '',
      numeroHabitaciones: 3,
      numeroBanos: 2,
      numeroCocinas: 1,
      numeroCamas: 1,
      tieneBano: false,
      tipoCama: ''
    });
    this.selectedFile = null;
    this.imagePreview = null;

    if (this.imagePreviewUrl) {
      URL.revokeObjectURL(this.imagePreviewUrl);
      this.imagePreviewUrl = null;
    }

    if (this.fotoInput) {
      this.fotoInput.nativeElement.value = '';
    }
  }

  onSubmit(): void {
    if (this.crearCasaForm.invalid || !this.selectedFile) {
      this.crearCasaForm.markAllAsTouched();

      fireWarningAlert(
        'Formulario incompleto',
        'Debes completar todos los campos y seleccionar una foto.'
      );

      return;
    }

    this.loading = true;

    const casaData: CrearCasaRequest = {
      nombre: this.crearCasaForm.value.nombre!,
      direccion: this.crearCasaForm.value.direccion!,
      poblacion: this.crearCasaForm.value.poblacion!,
      descripcion: this.crearCasaForm.value.descripcion || undefined,
      numeroHabitaciones: Number(this.crearCasaForm.value.numeroHabitaciones),
      numeroBanos: Number(this.crearCasaForm.value.numeroBanos),
      numeroCocinas: Number(this.crearCasaForm.value.numeroCocinas),
      numeroCamas: Number(this.crearCasaForm.value.numeroCamas),
      tieneBano: Boolean(this.crearCasaForm.value.tieneBano),
      tipoCama: this.crearCasaForm.value.tipoCama!,
      foto: this.selectedFile
    };

    this.casasService.crearCasa(casaData).subscribe({
      next: () => {
        this.loading = false;

        fireSuccessAlert(
          'Casa registrada',
          'La casa rural fue creada correctamente.'
        );

        this.resetFormState();
      },
      error: (error) => {
        this.loading = false;

        fireErrorAlert(
          'Error al registrar la casa',
          error?.error?.mensaje ||
            error?.error?.message ||
            'No se pudo crear la casa rural.'
        );
      }
    });
  }
}