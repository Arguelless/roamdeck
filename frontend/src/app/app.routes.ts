import { Routes } from '@angular/router';

import { authGuard } from './features/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'itinerary',
    pathMatch: 'full'
  },
  {
    path: '',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES)
  },
  {
    path: 'itinerary',
    canActivate: [authGuard],
    loadChildren: () => import('./features/itinerary/itinerary.routes').then((m) => m.ITINERARY_ROUTES)
  }
];
