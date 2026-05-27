import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  @Output() onLoginSuccess = new EventEmitter<any>();

  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(private http: HttpClient) {}

  login() {
    if (!this.username || !this.password) {
      this.error = 'Introduce usuario y contraseña.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.http.post<{ token: string }>(`${environment.apiBaseUrl}/api/auth/login`, {
      username: this.username,
      password: this.password
    }).subscribe({
      next: (res) => {
        localStorage.setItem('vantryx_token', res.token);
        this.loading = false;
        this.onLoginSuccess.emit({ username: this.username });
      },
      error: () => {
        this.error = 'Usuario o contraseña incorrectos.';
        this.loading = false;
      }
    });
  }
}
