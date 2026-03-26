import Swal from 'sweetalert2';

export const fireSuccessAlert = (title: string, text: string) => {
  return Swal.fire({
    icon: 'success',
    title,
    text,
    confirmButtonText: 'Continuar',
    background: '#ffffff',
    color: '#10243a',
    confirmButtonColor: '#1f596b'
  });
};

export const fireErrorAlert = (title: string, text: string) => {
  return Swal.fire({
    icon: 'error',
    title,
    text,
    confirmButtonText: 'Entendido',
    background: '#ffffff',
    color: '#10213a',
    confirmButtonColor: '#1f576b'
  });
};

export const fireWarningAlert = (title: string, text: string) => {
  return Swal.fire({
    icon: 'warning',
    title,
    text,
    confirmButtonText: 'Entendido',
    background: '#ffffff',
    color: '#10223a',
    confirmButtonColor: '#1f546b'
  });
};
