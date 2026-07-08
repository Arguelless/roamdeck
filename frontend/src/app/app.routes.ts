import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'itinerary',
    pathMatch: 'full'
  },
  {
    path: 'itinerary',
    loadChildren: () => import('./features/itinerary/itinerary.routes').then((m) => m.ITINERARY_ROUTES)
  }
];
