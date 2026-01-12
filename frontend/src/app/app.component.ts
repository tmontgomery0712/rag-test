import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {MatCard, MatCardContent} from "@angular/material/card";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MatCardContent, MatCard],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'frontend';
}
