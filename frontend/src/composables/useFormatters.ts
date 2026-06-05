import type { TransactionType, TransactionStatus } from '@/types'

export function useFormatters() {
  function formatCurrency(amount: number, currency = 'EUR') {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount)
  }

  function formatDate(iso: string) {
    return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
  }

  function formatDateTime(iso: string) {
    return new Date(iso).toLocaleString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    })
  }

  function txLabel(type: TransactionType) {
    if (type === 'TOP_UP') return 'Top Up'
    if (type === 'TRANSFER_IN') return 'Received'
    if (type === 'WITHDRAWAL') return 'Withdrawal'
    return 'Sent'
  }

  function statusLabel(status: TransactionStatus) {
    if (status === 'SUCCESSFUL') return 'Successful'
    if (status === 'PENDING') return 'Pending'
    return 'Failed'
  }

  function amountClass(type: TransactionType) {
    return type === 'TRANSFER_OUT' || type === 'WITHDRAWAL' ? 'debit' : 'credit'
  }

  return { formatCurrency, formatDate, formatDateTime, txLabel, statusLabel, amountClass }
}
