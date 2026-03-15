export interface HeatmapItem {
  date: string; // Spring Boot's LocalDate comes over the wire as a string 'YYYY-MM-DD'
  completed: boolean;
}

export interface HeatmapResponse {
  grid: HeatmapItem[];
  completionPercentage: number;
  totalDays: number;
  completedDays: number;
}
