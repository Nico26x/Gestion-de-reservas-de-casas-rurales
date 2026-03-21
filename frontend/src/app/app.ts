import { CommonModule } from '@angular/common';
import { Component, signal, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { NavbarComponent } from './layout/navbar/navbar';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');

  private router = inject(Router);
  showNavbar = true;

  constructor() {
    this.updateNavbarVisibility(this.router.url);

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.updateNavbarVisibility(event.urlAfterRedirects);
      });
  }

  private updateNavbarVisibility(url: string): void {
    const hiddenRoutes = ['/login', '/register'];
    this.showNavbar = !hiddenRoutes.includes(url);
  }
}