import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { attendanceService } from '@/services/attendanceService'
import type { PageParams } from '@/types/common'
import type { CheckInPayload } from '@/types/attendance'

const ATTENDANCES_KEY = 'attendances'

export function useAttendances(params?: PageParams) {
  return useQuery({
    queryKey: [ATTENDANCES_KEY, params],
    queryFn: () => attendanceService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useCheckIn() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CheckInPayload) => attendanceService.checkIn(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ATTENDANCES_KEY] })
      toast.success('Check-in realizado com sucesso.')
    },
    onError: () => toast.error('Não foi possível registrar o check-in.'),
  })
}

export function useMyAttendanceHistory() {
  return useQuery({
    queryKey: [ATTENDANCES_KEY, 'me'],
    queryFn: attendanceService.listMine,
  })
}

export function useMyAttendanceFrequency() {
  return useQuery({
    queryKey: [ATTENDANCES_KEY, 'me', 'frequency'],
    queryFn: attendanceService.myFrequency,
  })
}

export function useCheckOut() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => attendanceService.checkOut(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ATTENDANCES_KEY] })
      toast.success('Check-out realizado com sucesso.')
    },
    onError: () => toast.error('Não foi possível registrar o check-out.'),
  })
}
