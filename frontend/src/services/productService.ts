import { api } from '@/services/api'
import type { Page, PageParams } from '@/types/common'
import type {
  Product,
  ProductCategory,
  ProductPayload,
  StockMovement,
  StockMovementPayload,
  Supplier,
} from '@/types/product'

interface ProductApiResponse {
  id: string
  name: string
  description?: string
  category?: { id: string; name: string }
  supplier?: { id: string; name: string }
  sku: string
  costPrice?: number
  salePrice: number
  stockQuantity: number
  minStock: number
  active: boolean
  createdAt: string
  updatedAt: string
}

interface SupplierApiResponse {
  id: string
  name: string
  document?: string
  phone?: string
  email?: string
  address?: string
}

interface StockMovementApiResponse {
  id: string
  productId: string
  productName: string
  type: 'ENTRY' | 'EXIT'
  quantity: number
  unitPrice?: number
  reason: 'PURCHASE' | 'SALE' | 'ADJUSTMENT' | 'LOSS'
  createdByName?: string
  createdAt: string
}

function normalizeProduct(data: ProductApiResponse): Product {
  return {
    id: data.id,
    name: data.name,
    description: data.description,
    sku: data.sku,
    price: data.salePrice,
    costPrice: data.costPrice,
    stockQuantity: data.stockQuantity,
    minStockQuantity: data.minStock,
    categoryId: data.category?.id ?? '',
    categoryName: data.category?.name,
    supplierId: data.supplier?.id,
    supplierName: data.supplier?.name,
    active: data.active,
    createdAt: data.createdAt,
    updatedAt: data.updatedAt,
  }
}

function toProductRequest(payload: ProductPayload) {
  return {
    name: payload.name,
    description: payload.description,
    categoryId: payload.categoryId,
    supplierId: payload.supplierId,
    sku: payload.sku,
    costPrice: payload.costPrice ?? 0,
    salePrice: payload.price,
    stockQuantity: payload.stockQuantity,
    minStock: payload.minStockQuantity ?? 0,
    active: payload.active,
  }
}

function normalizeSupplier(data: SupplierApiResponse): Supplier {
  return {
    id: data.id,
    name: data.name,
    document: data.document,
    phone: data.phone,
    email: data.email,
    address: data.address,
  }
}

function normalizeStockMovement(data: StockMovementApiResponse): StockMovement {
  return {
    id: data.id,
    productId: data.productId,
    productName: data.productName,
    type: data.type,
    quantity: data.quantity,
    unitPrice: data.unitPrice,
    reason: data.reason,
    createdByName: data.createdByName,
    createdAt: data.createdAt,
  }
}

export const productService = {
  list: async (params?: PageParams): Promise<Page<Product>> => {
    const { data } = await api.get<Page<ProductApiResponse>>('/products', { params })
    return {
      ...data,
      content: (data.content ?? []).map(normalizeProduct),
    }
  },
  getById: async (id: string): Promise<Product> => {
    const { data } = await api.get<ProductApiResponse>(`/products/${id}`)
    return normalizeProduct(data)
  },
  create: async (payload: ProductPayload): Promise<Product> => {
    const { data } = await api.post<ProductApiResponse>('/products', toProductRequest(payload))
    return normalizeProduct(data)
  },
  update: async (id: string, payload: ProductPayload): Promise<Product> => {
    const { data } = await api.put<ProductApiResponse>(`/products/${id}`, toProductRequest(payload))
    return normalizeProduct(data)
  },
  remove: async (id: string): Promise<void> => {
    await api.delete(`/products/${id}`)
  },
}

export const productCategoryService = {
  list: async (_params?: PageParams): Promise<Page<ProductCategory>> => {
    const { data } = await api.get<ProductCategory[]>('/product-categories')
    return {
      content: data,
      totalElements: data.length,
      totalPages: 1,
      number: 0,
      size: data.length,
    }
  },
  create: async (payload: Partial<ProductCategory>): Promise<ProductCategory> => {
    const { data } = await api.post<ProductCategory>('/product-categories', payload)
    return data
  },
  remove: async (id: string): Promise<void> => {
    await api.delete(`/product-categories/${id}`)
  },
}

export const supplierService = {
  list: async (params?: PageParams): Promise<Page<Supplier>> => {
    const { data } = await api.get<Page<SupplierApiResponse>>('/suppliers', { params })
    return {
      ...data,
      content: (data.content ?? []).map(normalizeSupplier),
    }
  },
  create: async (payload: Partial<Supplier>): Promise<Supplier> => {
    const { data } = await api.post<SupplierApiResponse>('/suppliers', payload)
    return normalizeSupplier(data)
  },
  remove: async (id: string): Promise<void> => {
    await api.delete(`/suppliers/${id}`)
  },
}

export const stockMovementService = {
  list: async (params?: PageParams): Promise<Page<StockMovement>> => {
    const { data } = await api.get<Page<StockMovementApiResponse>>('/stock-movements', { params })
    return {
      ...data,
      content: (data.content ?? []).map(normalizeStockMovement),
    }
  },
  create: async (payload: StockMovementPayload): Promise<StockMovement> => {
    const { data } = await api.post<StockMovementApiResponse>('/stock-movements', payload)
    return normalizeStockMovement(data)
  },
}
