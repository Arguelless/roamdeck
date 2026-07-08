import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ItineraryRequest } from '../models/itinerary-request.model';
import { ItineraryResponse } from '../models/itinerary-response.model';

@Injectable({ providedIn: 'root' })
export class ItineraryApiService {
  private readonly http = inject(HttpClient);

  generate(request: ItineraryRequest): Observable<ItineraryResponse> {
    return this.http.post<ItineraryResponse>('/api/itineraries/generate', request);
  }
}
