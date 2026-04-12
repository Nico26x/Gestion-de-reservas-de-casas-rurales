import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { StorageService } from '../services/auth/storage.service';

export const ownerGuard: CanActivateFn = () => {
  const storageService = inject(StorageService);
  const router = inject(Router);

  const token = storageService.getToken();
  const rol = storageService.getRol();

  const esPropietario =
    rol === 'PROPIETARIO' || rol === 'ROLE_PROPIETARIO';

  if (token && esPropietario) {
    return true;
  }

  router.navigate(['/home']);
  return false;
};