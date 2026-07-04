export interface ProductCategory {
  id: string
  name: string
  description?: string
}

export interface Supplier {
  id: string
  name: string
  document?: string
  phone?: string
  email?: string
  address?: string
}

export interface Product {
  id: string
  name: string
  description?: string
  sku: string
  price: number
  costPrice?: number
  stockQuantity: number
  minStockQuantity?: number
  categoryId: string
  categoryName?: string
  supplierId?: string
  supplierName?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export type ProductPayload = Omit<
  Product,
  'id' | 'createdAt' | 'updatedAt' | 'categoryName' | 'supplierName'
>

export type StockMovementType = 'ENTRY' | 'EXIT'
export type StockMovementReason = 'PURCHASE' | 'SALE' | 'ADJUSTMENT' | 'LOSS'

export interface StockMovement {
  id: string
  productId: string
  productName: string
  type: StockMovementType
  quantity: number
  unitPrice?: number
  reason: StockMovementReason
  createdByName?: string
  createdAt: string
}

export interface StockMovementPayload {
  productId: string
  type: StockMovementType
  quantity: number
  unitPrice: number
  reason: StockMovementReason
}
