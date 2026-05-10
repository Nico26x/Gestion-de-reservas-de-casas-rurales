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
  selectedFiles: File[] = [];
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
    numeroComedores: [1, [Validators.required, Validators.min(1)]],
    numeroGarajes: [0, [Validators.required, Validators.min(0)]],
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

  get numeroComedores() {
    return this.crearCasaForm.get('numeroComedores');
  }

  get numeroGarajes() {
    return this.crearCasaForm.get('numeroGarajes');
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

  increment(field: 'numeroHabitaciones' | 'numeroBanos' | 'numeroCocinas' | 'numeroComedores' | 'numeroGarajes'): void {
    const current = Number(this.crearCasaForm.get(field)?.value ?? 0);
    this.crearCasaForm.get(field)?.setValue(current + 1);
  }

  decrement(field: 'numeroHabitaciones' | 'numeroBanos' | 'numeroCocinas' | 'numeroComedores' | 'numeroGarajes', min: number): void {
    const current = Number(this.crearCasaForm.get(field)?.value ?? min);
    if (current > min) {
      this.crearCasaForm.get(field)?.setValue(current - 1);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files || []);

    if (files.length === 0) {
      this.selectedFiles = [];
      this.imagePreview = null;
      return;
    }

    // Validar archivos
    const MAX_FILE_SIZE_MB = 25;
    const validFiles: File[] = [];
    let hasError = false;

    for (const file of files) {
      const fileSizeMb = file.size / (1024 * 1024);
      console.log(`[FILE VALIDATION] Archivo seleccionado:`);
      console.log(`  Nombre: ${file.name}`);
      console.log(`  Tamaño (bytes): ${file.size}`);
      console.log(`  Tamaño (MB): ${fileSizeMb.toFixed(2)}MB`);
      console.log(`  Tipo MIME: ${file.type}`);

      if (fileSizeMb > MAX_FILE_SIZE_MB) {
        console.warn(`[FILE VALIDATION] RECHAZADO: Archivo supera ${MAX_FILE_SIZE_MB}MB`);
        
        fireWarningAlert(
          'Archivo demasiado grande',
          `La imagen "${file.name}" pesa ${fileSizeMb.toFixed(2)}MB. El límite es ${MAX_FILE_SIZE_MB}MB.`
        );
        hasError = true;
      } else {
        console.log(`[FILE VALIDATION] ACEPTADO: ${fileSizeMb.toFixed(2)}MB <= ${MAX_FILE_SIZE_MB}MB`);
        validFiles.push(file);
      }
    }

    if (hasError && validFiles.length === 0) {
      // Si todos los archivos fueron rechazados, limpiar
      this.selectedFiles = [];
      this.imagePreview = null;
      if (input) {
        input.value = '';
      }
      return;
    }

    // Guardar archivos válidos
    this.selectedFiles = validFiles;

    // Mostrar preview del primer archivo
    if (validFiles.length > 0) {
      if (this.imagePreviewUrl) {
        URL.revokeObjectURL(this.imagePreviewUrl);
        this.imagePreviewUrl = null;
      }

      this.imagePreviewUrl = URL.createObjectURL(validFiles[0]);
      this.imagePreview = this.imagePreviewUrl;
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
      numeroComedores: 1,
      numeroGarajes: 0,
      numeroCamas: 1,
      tieneBano: false,
      tipoCama: ''
    });
    this.selectedFiles = [];
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
    if (this.crearCasaForm.invalid || this.selectedFiles.length === 0) {
      this.crearCasaForm.markAllAsTouched();

      fireWarningAlert(
        'Formulario incompleto',
        'Debes completar todos los campos y seleccionar al menos una foto.'
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
      numeroComedores: Number(this.crearCasaForm.value.numeroComedores),
      numeroGarajes: Number(this.crearCasaForm.value.numeroGarajes),
      numeroCamas: Number(this.crearCasaForm.value.numeroCamas),
      tieneBano: Boolean(this.crearCasaForm.value.tieneBano),
      tipoCama: this.crearCasaForm.value.tipoCama!,
      fotos: this.selectedFiles
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