import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDto } from '../models/dtos/UserDto';
import { UserResponseDto } from '../models/dtos/UserResponseDto';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = environment.mainApiUrl;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAllUsers(): Observable<UserDto[]> {
    return this.http.get<UserDto[]>(`${this.apiUrl}/auth/users`);
  }

  getCurrentUserDetails(): Observable<UserResponseDto> {
    const headers = this.getAuthHeaders();
    return this.http.get<UserResponseDto>(`${this.apiUrl}/auth/current-user-details`, { headers: headers});
  }

}
