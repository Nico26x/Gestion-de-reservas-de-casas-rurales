import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { StorageService } from '../services/auth/storage.service';

export const loginGuard: CanActivateFn = () => {
  const storageService = inject(StorageService);
  const router = inject(Router);

  if (storageService.isLoggedIn()) {
    router.navigate(['/home']);
    return false;
  }

  return true;
};