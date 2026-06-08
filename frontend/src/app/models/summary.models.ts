export interface SummaryResponse {
  totalIncome: number;
  totalExpense: number;
  expensesByCategory: Record<string, number>;
}
