import { api } from '@/services/api'
import type { Page, PageParams } from '@/types/common'

export function createCrudService<TEntity, TPayload = Partial<TEntity>>(resourcePath: string) {
  return {
    list: async (params?: PageParams): Promise<Page<TEntity>> => {
      const { data } = await api.get<Page<TEntity>>(resourcePath, { params })
      return data
    },
    getById: async (id: string): Promise<TEntity> => {
      const { data } = await api.get<TEntity>(`${resourcePath}/${id}`)
      return data
    },
    create: async (payload: TPayload): Promise<TEntity> => {
      const { data } = await api.post<TEntity>(resourcePath, payload)
      return data
    },
    update: async (id: string, payload: TPayload): Promise<TEntity> => {
      const { data } = await api.put<TEntity>(`${resourcePath}/${id}`, payload)
      return data
    },
    remove: async (id: string): Promise<void> => {
      await api.delete(`${resourcePath}/${id}`)
    },
  }
}
