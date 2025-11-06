import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { authGuard } from './auth.guard';
import { HomeComponent } from './pages/home/home.component';
import { RegisterComponent } from './pages/register/register.component';
import { CloudComponent } from './pages/cloud/cloud.component';
import { UserProfileComponent } from './pages/profile/profile.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', component: HomeComponent, canActivate: [authGuard] },
  { path: 'register', component: RegisterComponent },
  { path: 'cloud', component: CloudComponent, canActivate: [authGuard] },
  { path: 'profile', component: UserProfileComponent },
  { path: '**', redirectTo: 'login' },
];
