import { format, formatDistanceToNow, parseISO } from 'date-fns'
import { ptBR } from 'date-fns/locale'

function toDate(value: string | Date): Date {
  return typeof value === 'string' ? parseISO(value) : value
}

export function formatDate(value?: string | Date | null, pattern = 'dd/MM/yyyy'): string {
  if (!value) return '-'
  try {
    return format(toDate(value), pattern, { locale: ptBR })
  } catch {
    return '-'
  }
}

export function formatDateTime(value?: string | Date | null): string {
  return formatDate(value, 'dd/MM/yyyy HH:mm')
}

export function formatRelativeDate(value?: string | Date | null): string {
  if (!value) return '-'
  try {
    return formatDistanceToNow(toDate(value), { locale: ptBR, addSuffix: true })
  } catch {
    return '-'
  }
}
