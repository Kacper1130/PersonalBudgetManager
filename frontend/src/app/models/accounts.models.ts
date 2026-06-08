export interface AccountSummary {
  id: string;
  name: string;
}

export interface AccountDetailsData {
  id: string;
  name: string;
  balance: number;
}

export interface CreateAccountRequest {
  name: string;
  balance?: number;
}
