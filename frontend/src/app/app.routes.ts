import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./pages/dashboard/dashboard')
        .then(m => m.Dashboard),
  },
  {
    path: 'transactions',
    loadComponent: () =>
      import('./pages/transactions/transactions')
        .then(m => m.Transactions),
  },
  {
    path: 'budget-limits',
    loadComponent: () =>
      import('./pages/budget-limits/budget-limits')
        .then(m => m.BudgetLimits),
  },
  {
    path: 'accounts/:id',
    loadComponent: () =>
      import('./pages/account-details/account-details')
        .then(m => m.AccountDetails),
  }
];
