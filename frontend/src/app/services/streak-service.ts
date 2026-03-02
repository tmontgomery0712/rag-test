import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { tap, catchError, throwError } from "rxjs";
import { MatSnackBar } from '@angular/material/snack-bar';
import { Streak } from "../model/streak";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class StreakService {

  // In dev: environment.apiUrl === '' so this resolves to '/streaks' and uses the dev proxy.
  // In prod: environment.apiUrl === 'http://localhost:8080' (for now) so it becomes 'http://localhost:8080/streaks'.
  private baseLocation = 'api/streaks';
  private http = inject(HttpClient);
  private snackBar = inject(MatSnackBar);

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

    // 2. Persist to Backend
    return this.http.post<Streak>(`${this.baseLocation}/add-streak`, newStreak).pipe(
      tap((savedStreak) => {
        // Success: Replace temp object with the one from the server (which may have a real ID)
        this.streaksSignal.update(current =>
          current.map(s => s.id === newStreak.id ? savedStreak : s)
        );
      }),
      catchError((err) => {
        // Rollback on error
        this.streaksSignal.set(previousStreaks);

        // Notify user
        this.snackBar.open('Failed to save streak. Changes reverted.', 'Close', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'bottom',
        });

        return throwError(() => err);
      })
    ).subscribe();
  }

  deleteStreak(id: number) {
    const previousStreaks = this.streaksSignal(); // Snapshot

    // Optimistic Update
    this.streaksSignal.update(current => current.filter(s => s.id !== id));

    this.http.delete(`${this.baseLocation}/${id}`).pipe(
      catchError(err => {
        // Rollback on error
        this.streaksSignal.set(previousStreaks);

        // Notify user
        this.snackBar.open('Failed to delete streak. Item restored.', 'Close', { duration: 3000 });

        return throwError(() => err);
      })
    ).subscribe();
  }

  updateStreak(updatedStreak: Streak) {
    const previousStreaks = this.streaksSignal(); // Snapshot

    // 1. Optimistic Update
    this.streaksSignal.update(current =>
      current.map(s => s.id === updatedStreak.id ? updatedStreak : s)
    );

    // Note: no trailing slash in path; Spring Boot 3 does not match trailing slashes by default
    this.http.put<Streak>(`${this.baseLocation}/update-streak`, updatedStreak).pipe(
      catchError(err => {
        // 3. Rollback on error
        this.streaksSignal.set(previousStreaks);

        // 4. Notify user
        this.snackBar.open('Failed to update streak. Changes reverted.', 'Close', { duration: 3000 });

        return throwError(() => err);
      })
    ).subscribe();
  }
}
