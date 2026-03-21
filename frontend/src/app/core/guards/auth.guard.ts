import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { StorageService } from '../services/auth/storage.service';

export const authGuard: CanActivateFn = () => {
  const storageService = inject(StorageService);
  const router = inject(Router);

  if (storageService.isLoggedIn()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};