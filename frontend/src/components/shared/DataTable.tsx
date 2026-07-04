import { type ReactNode, useEffect, useState } from 'react'
import { ArrowDown, ArrowUp, ArrowUpDown, ChevronLeft, ChevronRight, Search } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useDebounce } from '@/hooks/useDebounce'
import { cn } from '@/lib/utils'

export interface DataTableColumn<T> {
  key: string
  header: string
  sortable?: boolean
  render: (row: T) => ReactNode
  className?: string
}

interface DataTableProps<T> {
  columns: DataTableColumn<T>[]
  data: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
  isLoading?: boolean
  onPageChange: (page: number) => void
  onSearchChange?: (search: string) => void
  onSortChange?: (sort: string) => void
  sort?: string
  searchPlaceholder?: string
  toolbar?: ReactNode
  emptyMessage?: string
  rowKey: (row: T) => string
}

export function DataTable<T>({
  columns,
  data,
  totalElements,
  totalPages,
  page,
  size,
  isLoading,
  onPageChange,
  onSearchChange,
  onSortChange,
  sort,
  searchPlaceholder = 'Buscar...',
  toolbar,
  emptyMessage = 'Nenhum registro encontrado.',
  rowKey,
}: DataTableProps<T>) {
  const [searchTerm, setSearchTerm] = useState('')
  const debounced = useDebounce(searchTerm, 400)

  useEffect(() => {
    onSearchChange?.(debounced)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debounced])

  const [sortKey, sortDir] = (sort ?? '').split(',')

  function toggleSort(key: string) {
    if (!onSortChange) return
    if (sortKey === key) {
      onSortChange(sortDir === 'asc' ? `${key},desc` : `${key},asc`)
    } else {
      onSortChange(`${key},asc`)
    }
  }

  const from = totalElements === 0 ? 0 : page * size + 1
  const to = Math.min((page + 1) * size, totalElements)

  return (
    <div className="space-y-3">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        {onSearchChange ? (
          <div className="relative w-full sm:max-w-xs">
            <Search className="pointer-events-none absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder={searchPlaceholder}
              className="pl-8"
            />
          </div>
        ) : (
          <div />
        )}
        {toolbar}
      </div>

      <div className="rounded-lg border border-border">
        <Table>
          <TableHeader>
            <TableRow>
              {columns.map((column) => (
                <TableHead key={column.key} className={column.className}>
                  {column.sortable ? (
                    <button
                      type="button"
                      onClick={() => toggleSort(column.key)}
                      className="flex items-center gap-1 font-medium hover:text-foreground"
                    >
                      {column.header}
                      {sortKey === column.key ? (
                        sortDir === 'asc' ? (
                          <ArrowUp className="h-3.5 w-3.5" />
                        ) : (
                          <ArrowDown className="h-3.5 w-3.5" />
                        )
                      ) : (
                        <ArrowUpDown className="h-3.5 w-3.5 opacity-40" />
                      )}
                    </button>
                  ) : (
                    column.header
                  )}
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading &&
              Array.from({ length: 5 }).map((_, index) => (
                <TableRow key={`skeleton-${index}`}>
                  {columns.map((column) => (
                    <TableCell key={column.key}>
                      <Skeleton className="h-4 w-full max-w-[160px]" />
                    </TableCell>
                  ))}
                </TableRow>
              ))}

            {!isLoading && data.length === 0 && (
              <TableRow>
                <TableCell colSpan={columns.length} className="py-10 text-center text-muted-foreground">
                  {emptyMessage}
                </TableCell>
              </TableRow>
            )}

            {!isLoading &&
              data.map((row) => (
                <TableRow key={rowKey(row)}>
                  {columns.map((column) => (
                    <TableCell key={column.key} className={column.className}>
                      {column.render(row)}
                    </TableCell>
                  ))}
                </TableRow>
              ))}
          </TableBody>
        </Table>
      </div>

      {totalElements > 0 && (
        <div className="flex flex-col items-center justify-between gap-3 sm:flex-row">
          <p className="text-sm text-muted-foreground">
            Mostrando <span className="font-medium text-foreground">{from}</span>-
            <span className="font-medium text-foreground">{to}</span> de{' '}
            <span className="font-medium text-foreground">{totalElements}</span>
          </p>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="icon"
              disabled={page <= 0}
              onClick={() => onPageChange(page - 1)}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <span className="text-sm text-muted-foreground">
              Página {totalPages === 0 ? 0 : page + 1} de {totalPages}
            </span>
            <Button
              variant="outline"
              size="icon"
              disabled={page + 1 >= totalPages}
              onClick={() => onPageChange(page + 1)}
            >
              <ChevronRight className={cn('h-4 w-4')} />
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}
