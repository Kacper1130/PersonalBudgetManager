export interface Transaction {
  id: string;
  accountId: string;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  category: string;
  description: string;
  dateTime: string;
}

export interface CreateTransactionRequest {
  accountId: string;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  category: string;
  description: string;
  dateTime?: string;
}

export interface TransactionCreatedResponse {
  transactionResponse: Transaction
  warning: string | null;
}

export interface TransactionFilters {
  from?: string;
  to?: string;
  category?: string;
}
