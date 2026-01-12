import { Routes } from '@angular/router';
import {StreaksComponent} from "./pages/streaks/streaks.component";

export const routes: Routes = [
  {
    path: '',
    component: StreaksComponent,
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: '',
  },
];
