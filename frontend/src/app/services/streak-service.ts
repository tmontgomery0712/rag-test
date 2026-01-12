import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { tap, catchError, throwError } from "rxjs";
import { MatSnackBar } from '@angular/material/snack-bar'; // Import Material
import { Streak } from "../model/streak";

@Injectable({
  providedIn: 'root'
})
export class StreakService {
  private baseLocation = 'backend';
  private http = inject(HttpClient);
  private snackBar = inject(MatSnackBar); // Inject Material SnackBar

  // State Signal
  private streaksSignal = signal<Streak[]>([]);
  readonly streaks = this.streaksSignal.asReadonly();
  private loaded = false;

  loadStreaks() {
    if(this.loaded) {
      return;
    }
    this.http.get<Streak[]>(this.baseLocation).subscribe(streaks => {
        this.streaksSignal.set(streaks);
        this.loaded = true;
      }
    );
  }

  addStreak(newStreak: Streak) {
    const previousStreaks = this.streaksSignal(); // Snapshot

    // 1. Optimistic Update
    this.streaksSignal.update(current => [...current, newStreak]);

    // return this.http.post<Streak>(this.baseLocation, newStreak).pipe(
    //   tap((savedStreak) => {
    //     // Success: Replace temp ID with real ID
    //     this.streaksSignal.update(current =>
    //       current.map(s => s === newStreak ? savedStreak : s)
    //     );
    //   }),
    //   catchError((err) => {
    //     // 2. Rollback
    //     this.streaksSignal.set(previousStreaks);
    //
    //     // 3. Material Snackbar for Notification
    //     this.snackBar.open('Failed to save streak. Changes reverted.', 'Close', {
    //       duration: 3000,
    //       horizontalPosition: 'end',
    //       verticalPosition: 'bottom',
    //       panelClass: ['error-snackbar'] // We can style this red in global styles
    //     });
    //
    //     return throwError(() => err);
    //   })
    // ).subscribe();
  }

  deleteStreak(id: number) {
    const previousStreaks = this.streaksSignal();
    this.streaksSignal.update(current => current.filter(s => s.id !== id));

    // this.http.delete(`${this.baseLocation}/${id}`).pipe(
    //   catchError(err => {
    //     this.streaksSignal.set(previousStreaks);
    //     this.snackBar.open('Failed to delete. Item restored.', 'Close', { duration: 3000 });
    //     return throwError(() => err);
    //   })
    // ).subscribe();
  }
}
