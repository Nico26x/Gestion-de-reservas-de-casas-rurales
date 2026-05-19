import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormArray } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CasasService } from '../../../core/services/casas/casas.service';
import { Casa } from '../../../core/models/casas/casa.model';
import {
  fireErrorAlert,
  fireSuccessAlert,
  fireWarningAlert
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
    const id = this.activatedRoute.snapshot.paramMap.get('id');
    
    if (!id || isNaN(Number(id))) {
      this.error = 'ID de casa no válido';
      fireErrorAlert('Error', this.error);
      return;
    }

    this.casaId = Number(id);
    this.cargarCasa();
    this.suscribirseACambiosDeContadores();
  }

  private inicializarFormArrays(): void {
    // Se inicializa vacío, se llena en cargarCasa
  }

  private crearHabitacionFormGroup(data?: any) {
    return this.fb.group({
      numeroCamas: [data?.numeroCamas ?? 1, [Validators.required, Validators.min(1)]],
      tipoCama: [data?.tipoCama ?? 'SIMPLE', [Validators.required]],
      tieneBano: [data?.tieneBano ?? false]
    });
  }

  private crearCocinaFormGroup(data?: any) {
    return this.fb.group({
      lavavajillas: [data?.lavavajillas ?? false],
      lavadora: [data?.lavadora ?? false]
    });
  }

  get habitacionesFormArray(): FormArray {
    return this.formulario.get('habitaciones') as FormArray;
  }

  get cocinasFormArray(): FormArray {
    return this.formulario.get('cocinas') as FormArray;
  }

  private suscribirseACambiosDeContadores(): void {
    this.formulario.get('numeroHabitaciones')?.valueChanges.subscribe(valor => {
      this.sincronizarHabitacionesConCantidad(Number(valor));
    });

    this.formulario.get('numeroCocinas')?.valueChanges.subscribe(valor => {
      this.sincronizarCocinasConCantidad(Number(valor));
    });
  }

  private sincronizarHabitacionesConCantidad(cantidad: number): void {
    cantidad = Math.max(cantidad, 3);

    const currentValue = this.formulario.get('numeroHabitaciones')?.value;
    if (currentValue !== cantidad) {
      this.formulario.patchValue(
        { numeroHabitaciones: cantidad },
        { emitEvent: false }
      );
    }

    while (this.habitacionesFormArray.length < cantidad) {
      this.habitacionesFormArray.push(this.crearHabitacionFormGroup());
    }

    while (this.habitacionesFormArray.length > cantidad) {
      this.habitacionesFormArray.removeAt(this.habitacionesFormArray.length - 1);
    }
  }

  private sincronizarCocinasConCantidad(cantidad: number): void {
    cantidad = Math.max(cantidad, 1);

    const currentValue = this.formulario.get('numeroCocinas')?.value;
    if (currentValue !== cantidad) {
      this.formulario.patchValue(
        { numeroCocinas: cantidad },
        { emitEvent: false }
      );
    }

    while (this.cocinasFormArray.length < cantidad) {
      this.cocinasFormArray.push(this.crearCocinaFormGroup());
    }

    while (this.cocinasFormArray.length > cantidad) {
      this.cocinasFormArray.removeAt(this.cocinasFormArray.length - 1);
    }
  }

  agregarHabitacion(): void {
    this.habitacionesFormArray.push(this.crearHabitacionFormGroup());
    this.formulario.patchValue(
      { numeroHabitaciones: this.habitacionesFormArray.length },
      { emitEvent: false }
    );
  }

  eliminarHabitacion(index: number): void {
    if (this.habitacionesFormArray.length > 3) {
      this.habitacionesFormArray.removeAt(index);
      this.formulario.patchValue(
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
    this.formulario.patchValue(
      { numeroCocinas: this.cocinasFormArray.length },
      { emitEvent: false }
    );
  }

  eliminarCocina(index: number): void {
    if (this.cocinasFormArray.length > 1) {
      this.cocinasFormArray.removeAt(index);
      this.formulario.patchValue(
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

  cargarCasa(): void {
    this.error = '';
    this.cargando = true;

    this.casasService.obtenerCasaPorId(this.casaId).subscribe({
      next: (casa: Casa) => {
        // Limpiar FormArrays
        while (this.habitacionesFormArray.length > 0) {
          this.habitacionesFormArray.removeAt(0);
        }
        while (this.cocinasFormArray.length > 0) {
          this.cocinasFormArray.removeAt(0);
        }

        // Cargar habitaciones existentes o crear 3 por defecto
        if (casa.habitaciones && casa.habitaciones.length > 0) {
          casa.habitaciones.forEach(hab => {
            this.habitacionesFormArray.push(
              this.crearHabitacionFormGroup({
                numeroCamas: hab.numeroCamas ?? 1,
                tipoCama: hab.tipoCama ?? 'SIMPLE',
                tieneBano: hab.tieneBano ?? false
              })
            );
          });
        }

        // Completar hasta mínimo 3 habitaciones
        while (this.habitacionesFormArray.length < 3) {
          this.habitacionesFormArray.push(this.crearHabitacionFormGroup());
        }

        // Cargar cocinas existentes o crear 1 por defecto
        if (casa.cocinas && casa.cocinas.length > 0) {
          casa.cocinas.forEach(cocina => {
            this.cocinasFormArray.push(
              this.crearCocinaFormGroup({
                lavavajillas: cocina.lavavajillas ?? false,
                lavadora: cocina.lavadora ?? false
              })
            );
          });
        } else {
          // Crear 1 cocina por defecto si no hay
          this.cocinasFormArray.push(this.crearCocinaFormGroup());
        }

        // Actualizar formulario con datos básicos
        this.formulario.patchValue({
          nombre: casa.nombre || '',
          direccion: casa.direccion || '',
          poblacion: casa.poblacion || '',
          descripcion: casa.descripcion || '',
          numeroHabitaciones: Math.max(this.habitacionesFormArray.length, 3),
          numeroBanos: casa.numeroBanos || 2,
          numeroCocinas: Math.max(this.cocinasFormArray.length, 1),
          numeroComedores: casa.numeroComedores || 1,
          numeroGarajes: casa.numeroGarajes || 0,
          numeroCamas: 1,
          tieneBano: false,
          tipoCama: 'SIMPLE'
        }, { emitEvent: false });

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
    // Validar formulario básico
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      fireWarningAlert(
        'Formulario incompleto',
        'Debes completar todos los campos requeridos.'
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

    this.guardando = true;

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
    const primeraHabitacion = habitaciones[0];
    const numeroCamasCompatibilidad = primeraHabitacion?.numeroCamas ?? 1;
    const tipoCamaCompatibilidad = primeraHabitacion?.tipoCama ?? 'SIMPLE';
    const tieneBanoCompatibilidad = primeraHabitacion?.tieneBano ?? false;

    const valores = this.formulario.value;
    const formData = new FormData();
    
    formData.append('nombre', String(valores.nombre));
    formData.append('direccion', String(valores.direccion));
    formData.append('poblacion', String(valores.poblacion));
    formData.append('descripcion', String(valores.descripcion ?? ''));
    formData.append('numeroHabitaciones', String(this.habitacionesFormArray.length));
    formData.append('numeroBanos', String(valores.numeroBanos));
    formData.append('numeroCocinas', String(this.cocinasFormArray.length));
    formData.append('numeroComedores', String(valores.numeroComedores));
    formData.append('numeroGarajes', String(valores.numeroGarajes));
    formData.append('numeroCamas', String(numeroCamasCompatibilidad));
    formData.append('tieneBano', String(tieneBanoCompatibilidad));
    formData.append('tipoCama', tipoCamaCompatibilidad);
    
    // Agregar habitacionesJson y cocinasJson
    formData.append('habitacionesJson', JSON.stringify(habitaciones));
    formData.append('cocinasJson', JSON.stringify(cocinas));

    if (this.fotosSeleccionadas.length > 0) {
      for (const foto of this.fotosSeleccionadas) {
        formData.append('fotos', foto);
      }
    }

    this.casasService.actualizarCasa(this.casaId, formData).subscribe({
      next: () => {
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
