import { Component, inject } from '@angular/core';
import { FormsModule } from "@angular/forms";
import { StreakService } from "../../services/streak-service";
import { Streak } from "../../model/streak";

// Angular Material Imports
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import {MatCheckbox} from "@angular/material/checkbox";

@Component({
  selector: 'app-streaks',
  standalone: true,
  imports: [
    FormsModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatCheckbox
  ],
  templateUrl: './streaks.component.html',
  styleUrl: './streaks.component.scss'
})
export class StreaksComponent {
  private streakService = inject(StreakService);

  streaks = this.streakService.streaks;
  newStreakName = '';

  add() {
    if (!this.newStreakName) return;

    const newStreak: Streak = {
      id: Date.now(),
      name: this.newStreakName,
      completed: false,
      currentStreak: 0,
      longestStreak: 0
    };

    this.streakService.addStreak(newStreak);
    this.newStreakName = '';
  }

  delete(id: number) {
    this.streakService.deleteStreak(id);
  }
}
