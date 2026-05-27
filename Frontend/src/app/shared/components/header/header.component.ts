import { Component } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';
import { AsyncPipe, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [MatIcon, NgIf, AsyncPipe, RouterLink],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent {
  title = 'Assistant IO';
  isAuthenticated$: Observable<boolean>;

  constructor(private authService: AuthService) {
    this.isAuthenticated$ = this.authService.isAuthenticated$;
  }

  get userName(): string {
    return this.authService.getUserName();
  }

  login(): void {
    this.authService.login();
  }

  logout(): void {
    this.authService.logout();
  }
}
