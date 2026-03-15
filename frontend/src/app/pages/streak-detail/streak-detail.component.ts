import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgFor } from '@angular/common';
import { StreakService } from '../../services/streak-service';
import { Streak } from '../../model/streak';
import { HeatmapResponse } from '../../model/heatmap';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';

export interface MonthLabel {
  month: string;
  weekIndex: number;
}

@Component({
  selector: 'app-streak-detail',
  standalone: true,
  imports: [
    NgFor,
    MatCardModule, MatButtonModule, MatIconModule,
    MatSelectModule, MatTooltipModule, FormsModule
  ],
  templateUrl: './streak-detail.component.html',
  styleUrl: './streak-detail.component.scss'
})
export class StreakDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private streakService = inject(StreakService);

  streak = signal<Streak | null>(null);
  heatmapData = signal<HeatmapResponse | null>(null);
  selectedDays = signal<number>(30);
  monthLabels = signal<MonthLabel[]>([]);
  weekCount = signal<number>(5);

  private monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.loadData(id);
    }
  }

  loadData(id: number) {
    const foundStreak = this.streakService.getStreakById(id);

    if (foundStreak) {
      this.streak.set(foundStreak);
      this.loadHeatmap(id);
    } else {
      this.goBack();
    }
  }

  loadHeatmap(id: number) {
    this.streakService.getStreakHeatmap(id, this.selectedDays()).subscribe({
      next: (data) => {
        this.heatmapData.set(data);
        this.computeMonthLabels(data);
      },
      error: (err) => console.error('Failed to load heatmap', err)
    });
  }

  onDaysChanged(days: number) {
    this.selectedDays.set(days);
    const currentStreak = this.streak();
    if (currentStreak) {
      this.loadHeatmap(currentStreak.id);
    }
  }

  getStartingRow(): number {
    const data = this.heatmapData();
    if (!data?.grid || data.grid.length === 0) return 1;
    const firstDate = new Date(data.grid[0].date + 'T00:00:00');
    return firstDate.getDay() + 1;
  }

  private computeMonthLabels(heatmap: HeatmapResponse) {
    if (!heatmap.grid || heatmap.grid.length === 0) {
      this.monthLabels.set([]);
      this.weekCount.set(5);
      return;
    }

    const months: MonthLabel[] = [];
    let currentMonth = -1;

    const firstDate = new Date(heatmap.grid[0].date + 'T00:00:00');
    const firstDayOffset = firstDate.getDay();

    for (let i = 0; i < heatmap.grid.length; i++) {
      const date = new Date(heatmap.grid[i].date + 'T00:00:00');
      const month = date.getMonth();

      if (month !== currentMonth) {
        const weekIndex = Math.floor((i + firstDayOffset) / 7);

        if (months.length > 0) {
          const prev = months[months.length - 1];
          if (weekIndex - prev.weekIndex < 3) {
            months.pop();
          }
        }

        months.push({
          month: this.monthNames[month],
          weekIndex: weekIndex
        });
        currentMonth = month;
      }
    }

    const totalWeeks = Math.ceil((heatmap.grid.length + firstDayOffset) / 7);
    this.monthLabels.set(months);
    this.weekCount.set(totalWeeks);
  }

  goBack() {
    this.router.navigate(['/streaks']);
  }
}
