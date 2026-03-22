import Swal from 'sweetalert2';

export const fireSuccessAlert = (title: string, text: string) => {
  return Swal.fire({
    icon: 'success',
    title,
    text,
    confirmButtonText: 'Continuar',
    background: '#051028',
    color: '#ffffff',
    confirmButtonColor: '#2f80ed'
  });
};

export const fireErrorAlert = (title: string, text: string) => {
  return Swal.fire({
    icon: 'error',
    title,
    text,
    confirmButtonText: 'Entendido',
    background: '#051028',
    color: '#ffffff',
    confirmButtonColor: '#2f80ed'
  });
};

export const fireWarningAlert = (title: string, text: string) => {
  return Swal.fire({
    icon: 'warning',
    title,
    text,
    confirmButtonText: 'Entendido',
    background: '#051028',
    color: '#ffffff',
    confirmButtonColor: '#2f80ed'
  });
};