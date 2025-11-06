import { Component } from '@angular/core';
import {
  FormBuilder,
  Validators,
  AbstractControl,
  ReactiveFormsModule,
  AsyncValidatorFn,
  ValidationErrors,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { Observable, of } from 'rxjs';
import {
  debounceTime,
  distinctUntilChanged,
  switchMap,
  map,
  first,
} from 'rxjs/operators';

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  submitted = false;
  errorMessage = '';

  registerForm: ReturnType<FormBuilder['group']>;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
  ) {
    this.registerForm = this.fb.group(
      {
        username: [
          '',
          {
            validators: [Validators.required],
            asyncValidators: [this.usernameExistsValidator(this.auth)],
            updateOn: 'blur',
          },
        ],
        password: ['', Validators.required],
        confirmPassword: ['', Validators.required],
        email: [''],
        phoneNumber: ['']
      },
      { validators: this.passwordMatchValidator },
    );
  }

  get f(): { [key: string]: AbstractControl } {
    return this.registerForm.controls;
  }

  passwordMatchValidator(group: AbstractControl) {
    const pw = group.get('password')?.value;
    const cpw = group.get('confirmPassword')?.value;
    return pw === cpw ? null : { mismatch: true };
  }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = '';

    if (this.registerForm.invalid) return;

    const { username, password, email, phoneNumber } = this.registerForm.value;

    this.auth.register(username!, password!, email, phoneNumber, '').subscribe({
      next: (res) => {
        console.log('Register success', res);
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Register error', err);
        this.errorMessage =
          err.error?.message || 'Registration failed. Please try again.';
      },
    });
  }

  usernameExistsValidator(authService: AuthService): AsyncValidatorFn {
    return (control): Observable<ValidationErrors | null> => {
      if (!control.value) {
        return of(null);
      }
      return of(control.value).pipe(
        debounceTime(500),
        distinctUntilChanged(),
        switchMap((username) => authService.checkUsernameExists(username)),
        map((exists) => (exists ? { usernameTaken: true } : null)),
        first(),
      );
    };
  }
}
