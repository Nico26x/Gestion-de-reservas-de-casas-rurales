import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, FormArray } from '@angular/forms';
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
export class CrearCasaComponent implements OnInit {
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
    numeroCamas: [1],
    tieneBano: [false],
    tipoCama: ['SIMPLE'],
    habitaciones: this.fb.array([]),
    cocinas: this.fb.array([])
  });

  constructor() {
    this.inicializarFormArrays();
  }

  ngOnInit(): void {
    this.suscribirseACambiosDeContadores();
  }

  private suscribirseACambiosDeContadores(): void {
    // Suscribirse a cambios de numeroHabitaciones
    this.crearCasaForm.get('numeroHabitaciones')?.valueChanges.subscribe(valor => {
      this.sincronizarHabitacionesConCantidad(Number(valor));
    });

    // Suscribirse a cambios de numeroCocinas
    this.crearCasaForm.get('numeroCocinas')?.valueChanges.subscribe(valor => {
      this.sincronizarCocinasConCantidad(Number(valor));
    });
  }

  private sincronizarHabitacionesConCantidad(cantidad: number): void {
    // Normalizar a mínimo 3
    cantidad = Math.max(cantidad, 3);

    // Actualizar el control sin emitir evento para evitar bucles
    const currentValue = this.crearCasaForm.get('numeroHabitaciones')?.value;
    if (currentValue !== cantidad) {
      this.crearCasaForm.patchValue(
        { numeroHabitaciones: cantidad },
        { emitEvent: false }
      );
    }

    // Agregar habitaciones si hay menos de las requeridas
    while (this.habitacionesFormArray.length < cantidad) {
      this.habitacionesFormArray.push(this.crearHabitacionFormGroup());
    }

    // Eliminar habitaciones desde el final si hay más de las requeridas
    while (this.habitacionesFormArray.length > cantidad) {
      this.habitacionesFormArray.removeAt(this.habitacionesFormArray.length - 1);
    }
  }

  private sincronizarCocinasConCantidad(cantidad: number): void {
    // Normalizar a mínimo 1
    cantidad = Math.max(cantidad, 1);

    // Actualizar el control sin emitir evento para evitar bucles
    const currentValue = this.crearCasaForm.get('numeroCocinas')?.value;
    if (currentValue !== cantidad) {
      this.crearCasaForm.patchValue(
        { numeroCocinas: cantidad },
        { emitEvent: false }
      );
    }

    // Agregar cocinas si hay menos de las requeridas
    while (this.cocinasFormArray.length < cantidad) {
      this.cocinasFormArray.push(this.crearCocinaFormGroup());
    }

    // Eliminar cocinas desde el final si hay más de las requeridas
    while (this.cocinasFormArray.length > cantidad) {
      this.cocinasFormArray.removeAt(this.cocinasFormArray.length - 1);
    }
  }

  private inicializarFormArrays(): void {
    // Inicializar 3 habitaciones por defecto
    for (let i = 0; i < 3; i++) {
      this.habitacionesFormArray.push(this.crearHabitacionFormGroup());
    }
    // Inicializar 1 cocina por defecto
    this.cocinasFormArray.push(this.crearCocinaFormGroup());
  }

  crearHabitacionFormGroup() {
    return this.fb.group({
      numeroCamas: [1, [Validators.required, Validators.min(1)]],
      tipoCama: ['SIMPLE', [Validators.required]],
      tieneBano: [false]
    });
  }

  crearCocinaFormGroup() {
    return this.fb.group({
      lavavajillas: [false],
      lavadora: [false]
    });
  }

  get habitacionesFormArray(): FormArray {
    return this.crearCasaForm.get('habitaciones') as FormArray;
  }

  get cocinasFormArray(): FormArray {
    return this.crearCasaForm.get('cocinas') as FormArray;
  }

  agregarHabitacion(): void {
    this.habitacionesFormArray.push(this.crearHabitacionFormGroup());
    // Actualizar el contador de habitaciones sin emitir evento
    this.crearCasaForm.patchValue(
      { numeroHabitaciones: this.habitacionesFormArray.length },
      { emitEvent: false }
    );
  }

  eliminarHabitacion(index: number): void {
    if (this.habitacionesFormArray.length > 3) {
      this.habitacionesFormArray.removeAt(index);
      // Actualizar el contador de habitaciones sin emitir evento
      this.crearCasaForm.patchValue(
        { numeroHabitaciones: this.habitacionesFormArray.length },
        { emitEvent: false }
      );
    } else {
      fireWarningAlert(
        'Mínimo de habitaciones',
        'Debe mantener al menos 3 habitaciones.'
      );
    }
  }

  agregarCocina(): void {
    this.cocinasFormArray.push(this.crearCocinaFormGroup());
    // Actualizar el contador de cocinas sin emitir evento
    this.crearCasaForm.patchValue(
      { numeroCocinas: this.cocinasFormArray.length },
      { emitEvent: false }
    );
  }

  eliminarCocina(index: number): void {
    if (this.cocinasFormArray.length > 1) {
      this.cocinasFormArray.removeAt(index);
      // Actualizar el contador de cocinas sin emitir evento
      this.crearCasaForm.patchValue(
        { numeroCocinas: this.cocinasFormArray.length },
        { emitEvent: false }
      );
    } else {
      fireWarningAlert(
        'Mínimo de cocinas',
        'Debe mantener al menos 1 cocina.'
      );
    }
  }

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
      tipoCama: 'SIMPLE'
    });

    // Limpiar FormArrays
    while (this.habitacionesFormArray.length > 0) {
      this.habitacionesFormArray.removeAt(0);
    }
    while (this.cocinasFormArray.length > 0) {
      this.cocinasFormArray.removeAt(0);
    }

    // Reinicializar con valores por defecto
    this.inicializarFormArrays();

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
    // Validar formulario básico
    if (this.crearCasaForm.invalid) {
      this.crearCasaForm.markAllAsTouched();
      fireWarningAlert(
        'Formulario incompleto',
        'Debes completar todos los campos requeridos.'
      );
      return;
    }

    // Validar que haya fotos seleccionadas
    if (this.selectedFiles.length === 0) {
      fireWarningAlert(
        'Fotos requeridas',
        'Debes seleccionar al menos una foto.'
      );
      return;
    }

    // Validar que haya mínimo 3 habitaciones
    if (this.habitacionesFormArray.length < 3) {
      fireWarningAlert(
        'Habitaciones insuficientes',
        'Debe haber al menos 3 habitaciones.'
      );
      return;
    }

    // Validar que haya mínimo 1 cocina
    if (this.cocinasFormArray.length < 1) {
      fireWarningAlert(
        'Cocinas insuficientes',
        'Debe haber al menos 1 cocina.'
      );
      return;
    }

    // Validar que las habitaciones sean válidas
    for (let i = 0; i < this.habitacionesFormArray.length; i++) {
      const hab = this.habitacionesFormArray.at(i);
      if (hab?.invalid) {
        fireWarningAlert(
          'Habitación incompleta',
          `Habitación ${i + 1} tiene campos vacíos o inválidos.`
        );
        return;
      }
    }

    // Validar que las cocinas sean válidas
    for (let i = 0; i < this.cocinasFormArray.length; i++) {
      const cocina = this.cocinasFormArray.at(i);
      if (cocina?.invalid) {
        fireWarningAlert(
          'Cocina incompleta',
          `Cocina ${i + 1} tiene campos vacíos o inválidos.`
        );
        return;
      }
    }

    this.loading = true;

    // Construir arrays de habitaciones y cocinas
    const habitaciones = this.habitacionesFormArray.value.map((h: any) => ({
      numeroCamas: Number(h.numeroCamas),
      tipoCama: h.tipoCama,
      tieneBano: Boolean(h.tieneBano)
    }));

    const cocinas = this.cocinasFormArray.value.map((c: any) => ({
      lavavajillas: Boolean(c.lavavajillas),
      lavadora: Boolean(c.lavadora)
    }));

    // Obtener valores de compatibilidad desde la primera habitación
    const primeraHabitacion = this.habitacionesFormArray.at(0)?.value;
    const numeroCamasCompatibilidad = Number(primeraHabitacion?.numeroCamas ?? 1);
    const tipoCamaCompatibilidad = primeraHabitacion?.tipoCama ?? 'SIMPLE';
    const tieneBanoCompatibilidad = Boolean(primeraHabitacion?.tieneBano);

    const casaData: CrearCasaRequest = {
      nombre: this.crearCasaForm.value.nombre!,
      direccion: this.crearCasaForm.value.direccion!,
      poblacion: this.crearCasaForm.value.poblacion!,
      descripcion: this.crearCasaForm.value.descripcion || undefined,
      numeroHabitaciones: this.habitacionesFormArray.length,
      numeroBanos: Number(this.crearCasaForm.value.numeroBanos),
      numeroCocinas: this.cocinasFormArray.length,
      numeroComedores: Number(this.crearCasaForm.value.numeroComedores),
      numeroGarajes: Number(this.crearCasaForm.value.numeroGarajes),
      numeroCamas: numeroCamasCompatibilidad,
      tieneBano: tieneBanoCompatibilidad,
      tipoCama: tipoCamaCompatibilidad,
      fotos: this.selectedFiles,
      habitacionesJson: JSON.stringify(habitaciones),
      cocinasJson: JSON.stringify(cocinas)
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