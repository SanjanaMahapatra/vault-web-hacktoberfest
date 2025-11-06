import { Component, OnInit } from '@angular/core';
import {
  FormGroup,
  FormControl,
  ReactiveFormsModule,
  FormBuilder,
  Validators,
} from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { UserResponseDto } from '../../models/dtos/UserResponseDto';
import { UserService } from '../../services/user.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CommonModule } from '@angular/common';
import { MatButton } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { HttpStatusCode, HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButton,
    MatInputModule,
    MatFormFieldModule,
  ],
})
export class UserProfileComponent implements OnInit {
  private apiUrl = environment.mainApiUrl;

  userProfileForm: ReturnType<FormBuilder['group']>;
  currentUserData: UserResponseDto | null = null;
  isLoading: boolean = true;
  error: string | null = null;
  currentUserName: string | null = null;
  usernameDisabled: boolean = true;
  isDataSaved: boolean = false;
  successMsg: string | null = null;
  selectedImageUrl: string | null = null;
  selectedFile: File | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private auth: AuthService,
    private userService: UserService,
    private http: HttpClient,
  ) {
    this.userProfileForm = this.formBuilder.group({
      username: new FormControl(this.auth.getUsername()),
      email: new FormControl('', [Validators.email]),
      phoneNumber: new FormControl('', [Validators.pattern('^[6-9][0-9]{9}$')]),
      profilePicture: new FormControl(''),
    });
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.auth.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  }

  // userProfileForm = new FormGroup({
  //     email: new FormControl(''),
  //     phoneNumber: new FormControl(''),
  //     profilePicture: new FormControl('')
  // })

  ngOnInit(): void {
    // Fetching the user details

    this.currentUserName = this.auth.getUsername();
    console.log('current user logged in ==> ' + this.currentUserName);

    this.userService.getCurrentUserDetails().subscribe({
      next: (data: UserResponseDto) => {
        this.currentUserData = data;
        this.isLoading = false;

        if(data.profilePicture) {
            this.selectedImageUrl = data.profilePicture;
        }

        this.userProfileForm.patchValue({
          email: data.email || '',
          phoneNumber: data.phoneNumber || '',
          profilePicture: data.profilePicture || '',
          username: this.currentUserName,
        });
        this.successMsg = 'User details loaded successfully !!';

        setTimeout(() => {
            this.successMsg = null;
        }, 3000)
      },
      error: () => {
        ((this.error = 'Failed to load the user details!!'),
          (this.isLoading = false));
      },
    });
  }


  onFileSelected(event: any): void {
        const file = event.target.files[0];
        if (file) {
            // Validate file type
            if (!file.type.startsWith('image/')) {
                this.error = 'Please select a valid image file.';
                return;
            }

            // Validate file size (e.g., max 5MB)
            const maxSize = 5 * 1024 * 1024; // 5MB
            if (file.size > maxSize) {
                this.error = 'File size must be less than 5MB.';
                return;
            }

            this.selectedFile = file;
            this.error = null;

            // Create a preview URL for the selected image
            const reader = new FileReader();
            reader.onload = (e: any) => {
                this.selectedImageUrl = e.target.result;
            };
            reader.readAsDataURL(file);

            // Update the form control
            this.userProfileForm.patchValue({
                profilePicture: file.name // or handle file upload differently based on your backend
            });
        }
    }

  protected get isResetButtonDisabled() {
    return this.isDataSaved;
  }

  protected get isSaveButtonDisabled() {
    return this.userProfileForm.pristine || !this.userProfileForm.valid;
  }

  protected saveUserData() {
    this.isDataSaved = true;
    this.error = null;
    this.successMsg = null;

    // disabling the form when the user details are being saved
    this.disableForm();

    this.saveUserData$().subscribe({
      next: (data: UserResponseDto) => {
        this.isDataSaved = false;
        this.currentUserData = data;
        console.log('User profile updated successfully !!', data);
        this.enableForm();
        this.successMsg = 'Profile updated successfully !!';

        setTimeout(() => {
          this.successMsg = null;
        }, 3000);
      },
      error: (error) => {
        this.isDataSaved = false;

        this.enableForm();

        if (error instanceof HttpErrorResponse) {
          if (error.status === HttpStatusCode.BadRequest) {
            this.error = 'Invalid data provided. Please check your inputs.';
          } else if (error.status === HttpStatusCode.Unauthorized) {
            this.error = 'You are not authorized. Please login again.';
          } else if (error.status === HttpStatusCode.NotFound) {
            this.error = 'User not found.';
          } else if (error.status === HttpStatusCode.InternalServerError) {
            this.error = 'Server error. Please try again later.';
          } else {
            this.error = 'Failed to update profile. Please try again.';
          }
        }
      },
    });
  }

  private disableForm() {
    Object.values(this.userProfileForm.controls).forEach((control) => {
      control.disable();
    });
  }

  private enableForm() {
    Object.values(this.userProfileForm.controls).forEach((control) => {
      control.enable();
    });
  }

  private saveUserData$(): Observable<UserResponseDto> {
    const headers = this.getAuthHeaders();

    if(this.selectedFile) {
        this.userProfileForm.patchValue({
            profilePicture: this.selectedFile
        })
    }

    return this.http.put<UserResponseDto>(
      `${this.apiUrl}/auth/update-user-profile?username=${this.currentUserName}`,
      this.userProfileForm.value,
      { headers: headers },
    );
  }

  protected restoreFormData() {
    this.currentUserData &&
      this.userProfileForm.patchValue({
        email: this.currentUserData.email || '',
        phoneNumber: this.currentUserData.phoneNumber || '',
        profilePicture: this.currentUserData.profilePicture || '',
        username: this.currentUserName,
      });

      this.selectedImageUrl = this.currentUserData?.profilePicture || null;
      this.selectedFile = null;
  }
}
