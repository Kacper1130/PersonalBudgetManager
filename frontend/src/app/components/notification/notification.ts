import {Component, inject} from '@angular/core';
import {NotificationService} from '../../services/notification-service';
import {NotificationData} from '../../models/notification.models';

@Component({
  selector: 'app-notification',
  imports: [],
  templateUrl: './notification.html',
  styles: ``,
})
export class Notification {
  notificationService = inject(NotificationService);

  toastClasses(n: NotificationData): string {
    if (n.type === 'error')   return 'bg-red-50 border-[1.5px] border-red-300';
    if (n.type === 'warning') return 'bg-amber-50 border-[1.5px] border-amber-300';
    return 'bg-green-50 border-[1.5px] border-green-300';
  }

  iconClasses(n: NotificationData): string {
    if (n.type === 'error')   return 'bg-red-500 text-white';
    if (n.type === 'warning') return 'bg-amber-600 text-white';
    return 'bg-green-600 text-white';
  }

  messageClasses(n: NotificationData): string {
    if (n.type === 'error')   return 'text-red-900';
    if (n.type === 'warning') return 'text-amber-900';
    return 'text-green-900';
  }

  dismissClasses(n: NotificationData): string {
    if (n.type === 'error')   return 'text-red-800';
    if (n.type === 'warning') return 'text-amber-800';
    return 'text-green-800';
  }
}
