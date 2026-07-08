import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ItineraryApiService } from '../data-access/itinerary-api.service';
import { ItineraryResponse } from '../models/itinerary-response.model';

@Component({
  selector: 'app-itinerary-page',
  imports: [ReactiveFormsModule],
  templateUrl: './itinerary-page.html',
  styleUrl: './itinerary-page.scss'
})
export class ItineraryPage {
  private readonly formBuilder = inject(FormBuilder);
  private readonly itineraryApi = inject(ItineraryApiService);

  protected readonly loading = signal(false);
  protected readonly result = signal<ItineraryResponse | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.formBuilder.nonNullable.group({
    destination: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    budget: ['', Validators.required],
    preferences: ['']
  });

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.itineraryApi.generate(this.form.getRawValue()).subscribe({
      next: (response) => {
        this.result.set(response);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo generar el itinerario. Inténtalo de nuevo.');
        this.loading.set(false);
      }
    });
  }
}
