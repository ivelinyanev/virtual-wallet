import Swal from 'sweetalert2'

const base = Swal.mixin({
  customClass: {
    popup: 'vw-popup',
    title: 'vw-title',
    htmlContainer: 'vw-html',
    confirmButton: 'vw-btn-confirm',
    cancelButton: 'vw-btn-cancel',
    icon: 'vw-icon',
  },
  buttonsStyling: false,
  reverseButtons: true,
})

export function useDialog() {
  function confirm(options: { title: string; text?: string; confirmLabel?: string }) {
    return base.fire({
      icon: 'warning',
      title: options.title,
      text: options.text,
      showCancelButton: true,
      confirmButtonText: options.confirmLabel ?? 'Confirm',
      cancelButtonText: 'Cancel',
    })
  }

  function error(title: string, text?: string) {
    return base.fire({
      icon: 'error',
      title,
      text,
      showCancelButton: false,
      confirmButtonText: 'OK',
    })
  }

  function success(title: string, text?: string) {
    return base.fire({
      icon: 'success',
      title,
      text,
      showCancelButton: false,
      confirmButtonText: 'OK',
      timer: 2000,
      timerProgressBar: true,
    })
  }

  return { confirm, error, success }
}
