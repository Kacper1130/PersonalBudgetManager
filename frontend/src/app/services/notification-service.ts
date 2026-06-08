import {Injectable, signal} from '@angular/core';
import {NotificationData} from '../models/notification.models';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  notifications = signal<NotificationData[]>([]);

  show(message: string, type: NotificationData['type'] = 'success', timeout: number = 4000): void {
    const id = Date.now() + Math.random();
    this.notifications.update(t => [...t, { id, message, type }]);
    setTimeout(() => this.dismiss(id), timeout  );
  }

  dismiss(id: number): void {
    this.notifications.update(t => t.filter(x => x.id !== id));
  }
}
