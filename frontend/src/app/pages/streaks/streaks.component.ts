import {Component, inject, OnInit, signal} from '@angular/core';
import { FormsModule } from "@angular/forms";
import { StreakService } from "../../services/streak-service";
import { Streak } from "../../model/streak";

// Angular Material Imports - Added MatCardModule and MatMenuModule
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCheckboxModule } from "@angular/material/checkbox";
import {NgClass} from "@angular/common"; // Corrected from MatCheckbox

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
    MatCheckboxModule,
    MatCardModule,
    MatMenuModule,
    NgClass
  ],
  templateUrl: './streaks.component.html',
  styleUrl: './streaks.component.scss'
})
export class StreaksComponent implements OnInit {

  private streakService = inject(StreakService);
  streaks = this.streakService.streaks;
  newStreakName = '';

  editingStreak = signal<Streak | null>(null);
  editedName = ''; // Holds the name while editing

  ngOnInit(): void {
    this.streakService.loadStreaks();
  }

  addStreak() {
    if (!this.newStreakName.trim()) return;

    //Refactor the Streak model with a wrapper that has user id and then a list of the current Streak model. Clean up the backend as well to match
    const newStreak: Streak = {
      id: Date.now(),
      name: this.newStreakName.trim(),
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

  onEditClick(streak: Streak): void {
    this.editingStreak.set(streak);
    this.editedName = streak.name;
  }

  toggleCompletion(streak: Streak) {
    const isNowCompleted = !streak.completed;

    let newStreakCount = isNowCompleted
      ? streak.currentStreak + 1
      : streak.currentStreak - 1;

    if (newStreakCount < 0) {
      newStreakCount = 0;
    }

    const newLongestStreak = Math.max(newStreakCount, streak.longestStreak);

    const updatedStreak = {
      ...streak,
      completed: isNowCompleted,
      currentStreak: newStreakCount,
      longestStreak: newLongestStreak,
    };
    this.streakService.updateStreak(updatedStreak);
  }

  update(): void {
    const streakToUpdate = this.editingStreak();
    if (!streakToUpdate || !this.editedName.trim()) return;

    this.streakService.updateStreak({ ...streakToUpdate, name: this.editedName.trim() });

    this.editingStreak.set(null); // Exit edit mode
  }


  cancelEdit(): void {
    this.editingStreak.set(null);
  }
}
