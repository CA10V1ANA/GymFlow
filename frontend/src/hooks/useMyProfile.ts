import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { studentSelfService } from '@/services/studentSelfService'
import type { StudentSelfUpdatePayload } from '@/types/studentSelf'

const MY_PROFILE_KEY = 'myProfile'

export function useMyProfile() {
  return useQuery({
    queryKey: [MY_PROFILE_KEY],
    queryFn: studentSelfService.getMe,
  })
}

export function useUpdateMyProfile() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: StudentSelfUpdatePayload) => studentSelfService.updateMe(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [MY_PROFILE_KEY] })
      toast.success('Dados atualizados com sucesso.')
    },
    onError: () => toast.error('Nao foi possivel atualizar seus dados.'),
  })
}
