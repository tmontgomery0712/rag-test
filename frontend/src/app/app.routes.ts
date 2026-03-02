import { Routes } from '@angular/router';
import { StreaksComponent } from './pages/streaks/streaks.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { ForbiddenComponent } from './pages/forbidden/forbidden.component';
import { ErrorComponent } from './pages/error/error.component';
import { SettingsComponent } from './pages/settings/settings.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    title: 'Login'
  },
  {
    path: 'register',
    component: RegisterComponent,
    title: 'Register'
  },
  {
    path: 'forbidden',
    component: ForbiddenComponent,
    title: 'Access Denied'
  },
  {
    path: 'error',
    component: ErrorComponent,
    title: 'Error'
  },
  {
    path: 'settings',
    component: SettingsComponent,
    canActivate: [authGuard],
    title: 'Settings'
  },
  {
    path: 'streaks',
    component: StreaksComponent,
    canActivate: [authGuard],
    title: 'My Streaks'
  },
  {
    path: '',
    redirectTo: '/streaks',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
