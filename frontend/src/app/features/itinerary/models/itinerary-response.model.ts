export interface Activity {
  timeOfDay: string;
  description: string;
}

export interface ItineraryDay {
  date: string;
  activities: Activity[];
}

export interface ItineraryResponse {
  days: ItineraryDay[];
}
