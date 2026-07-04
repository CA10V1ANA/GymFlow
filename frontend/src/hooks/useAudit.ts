import { useQuery } from '@tanstack/react-query'
import { auditService } from '@/services/auditService'
import type { PageParams } from '@/types/common'

export function useAuditLogs(params?: PageParams) {
  return useQuery({
    queryKey: ['audit-logs', params],
    queryFn: () => auditService.list(params),
    placeholderData: (prev) => prev,
  })
}
