// ─── Auth ────────────────────────────────────────────────────────────────────

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  first_name: string
  last_name: string
  username: string
  email: string
  phone_number: string
  password: string
}

export interface VerifyRequest {
  email: string
  verification_code: string
}

// ─── Users ───────────────────────────────────────────────────────────────────

export interface PublicUserResponse {
  id: number
  username: string
  first_name: string
  last_name: string
  photo_url: string | null
}

export interface PrivateUserResponse extends PublicUserResponse {
  email: string
  phone_number: string
  is_verified: boolean
  is_blocked: boolean
  roles: string[]
}

export interface UpdateUserRequest {
  first_name?: string
  last_name?: string
  email?: string
  phone_number?: string
  password?: string
  photo_url?: string
}

// ─── Wallets ─────────────────────────────────────────────────────────────────

export type Currency = 'EUR' | 'USD' | 'GBP'

export interface WalletResponse {
  id: number
  owner_name: string
  wallet_name: string
  balance: number
  currency: Currency
}

export interface WalletCreateRequest {
  name: string
  currency: Currency
}

export interface TopUpRequest {
  wallet_id: number
  card_id: number
  amount: number
}

export interface WithdrawRequest {
  wallet_id: number
  card_id: number
  amount: number
}

// ─── Cards ───────────────────────────────────────────────────────────────────

export interface CardResponse {
  id: number
  card_brand: string
  last4: string
  expiration_month: number
  expiration_year: number
  card_holder: string
}

export interface CardCreateRequest {
  card_number: string
  first_name: string
  last_name: string
  exp_month: number
  exp_year: number
  cvv: string
}

// ─── Transactions ────────────────────────────────────────────────────────────

export type TransactionType = 'TOP_UP' | 'TRANSFER_IN' | 'TRANSFER_OUT' | 'WITHDRAWAL'
export type TransactionStatus = 'SUCCESSFUL' | 'PENDING' | 'FAILED'

export interface UserTransactionResponse {
  id: number
  amount: number
  currency: Currency
  type: TransactionType
  status: TransactionStatus
  timestamp: string
  wallet_name: string | null
  counterparty_username: string | null
  card_last4: string | null
}

export interface AdminTransactionResponse {
  id: number
  amount: number
  currency: Currency
  type: TransactionType
  status: TransactionStatus
  timestamp: string
  wallet_name: string | null
  wallet_owner_username: string | null
  counter_party_wallet_name: string | null
  counterparty_wallet_owner_username: string | null
}

// Alias kept for backwards compat within user-facing pages
export type TransactionResponse = UserTransactionResponse

export interface TransactionFilterRequest {
  type?: TransactionType
  status?: TransactionStatus
  from?: string      // ISO datetime, maps to backend `from` (LocalDateTime)
  to?: string        // ISO datetime, maps to backend `to` (LocalDateTime)
  page?: number
  size?: number
}

// ─── Transfers ───────────────────────────────────────────────────────────────

export interface TransferRequest {
  from_wallet_id: number
  to_username: string
  amount: number
}

export interface OwnWalletTransferRequest {
  from_wallet_id: number
  to_wallet_id: number
  amount: number
}

// ─── Pagination ──────────────────────────────────────────────────────────────

export interface Page<T> {
  content: T[]
  total_elements: number
  total_pages: number
  number: number
  size: number
}

// ─── Auth response ───────────────────────────────────────────────────────────

export interface AuthResponse {
  token: string
  user: PrivateUserResponse
}
