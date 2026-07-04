import { api } from '@/services/api'
import type { Page, PageParams } from '@/types/common'
import type { AuditLog } from '@/types/audit'

interface AuditLogApiResponse {
  id: string
  userName: string
  action: string
  entityName: string
  entityId: string
  details?: string
  createdAt: string
}

export const auditService = {
  list: async (params?: PageParams): Promise<Page<AuditLog>> => {
    const { data } = await api.get<Page<AuditLogApiResponse>>('/audit-logs', { params })
    return {
      ...data,
      content: (data.content ?? []).map((log) => ({
        id: log.id,
        userName: log.userName,
        action: log.action,
        entity: log.entityName,
        entityId: log.entityId,
        details: log.details,
        timestamp: log.createdAt,
      })),
    }
  },
}
