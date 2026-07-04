import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  productCategoryService,
  productService,
  stockMovementService,
  supplierService,
} from '@/services/productService'
import type { PageParams } from '@/types/common'
import type { ProductCategory, ProductPayload, StockMovementPayload, Supplier } from '@/types/product'

const PRODUCTS_KEY = 'products'
const CATEGORIES_KEY = 'product-categories'
const SUPPLIERS_KEY = 'suppliers'
const STOCK_MOVEMENTS_KEY = 'stock-movements'

export function useProducts(params?: PageParams) {
  return useQuery({
    queryKey: [PRODUCTS_KEY, params],
    queryFn: () => productService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useCreateProduct() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: ProductPayload) => productService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [PRODUCTS_KEY] })
      toast.success('Produto cadastrado com sucesso.')
    },
    onError: () => toast.error('Não foi possível cadastrar o produto.'),
  })
}

export function useUpdateProduct() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ProductPayload }) =>
      productService.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [PRODUCTS_KEY] })
      toast.success('Produto atualizado com sucesso.')
    },
    onError: () => toast.error('Não foi possível atualizar o produto.'),
  })
}

export function useDeleteProduct() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => productService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [PRODUCTS_KEY] })
      toast.success('Produto removido com sucesso.')
    },
    onError: () => toast.error('Não foi possível remover o produto.'),
  })
}

export function useProductCategories(params?: PageParams) {
  return useQuery({
    queryKey: [CATEGORIES_KEY, params],
    queryFn: () => productCategoryService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useCreateProductCategory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: Partial<ProductCategory>) => productCategoryService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [CATEGORIES_KEY] })
      toast.success('Categoria cadastrada com sucesso.')
    },
    onError: () => toast.error('Não foi possível cadastrar a categoria.'),
  })
}

export function useDeleteProductCategory() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => productCategoryService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [CATEGORIES_KEY] })
      toast.success('Categoria removida com sucesso.')
    },
    onError: () => toast.error('Não foi possível remover a categoria.'),
  })
}

export function useSuppliers(params?: PageParams) {
  return useQuery({
    queryKey: [SUPPLIERS_KEY, params],
    queryFn: () => supplierService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useCreateSupplier() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: Partial<Supplier>) => supplierService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [SUPPLIERS_KEY] })
      toast.success('Fornecedor cadastrado com sucesso.')
    },
    onError: () => toast.error('Não foi possível cadastrar o fornecedor.'),
  })
}

export function useDeleteSupplier() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => supplierService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [SUPPLIERS_KEY] })
      toast.success('Fornecedor removido com sucesso.')
    },
    onError: () => toast.error('Não foi possível remover o fornecedor.'),
  })
}

export function useStockMovements(params?: PageParams) {
  return useQuery({
    queryKey: [STOCK_MOVEMENTS_KEY, params],
    queryFn: () => stockMovementService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useCreateStockMovement() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: StockMovementPayload) => stockMovementService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [STOCK_MOVEMENTS_KEY] })
      queryClient.invalidateQueries({ queryKey: [PRODUCTS_KEY] })
      toast.success('Movimentação de estoque registrada.')
    },
    onError: () => toast.error('Não foi possível registrar a movimentação.'),
  })
}
