import { AbstractControl, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

// Misma regla que PasswordValidator del backend
export const validarPassword = Validators.pattern(
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/,
);

export function coinciden(campo: string, confirmacion: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const a = grupo.get(campo)?.value;
    const b = grupo.get(confirmacion)?.value;
    return a === b ? null : { noCoinciden: true };
  };
}
