export interface AuditLog {
  id: string
  entity: string
  entityId: string
  action: string
  userName: string
  timestamp: string
  details?: string
}
