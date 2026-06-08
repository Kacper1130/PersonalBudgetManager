import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {Notification} from './components/notification/notification';

@Component({
  selector: 'app-root',
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    Notification,
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
