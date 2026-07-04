import { type LucideIcon } from 'lucide-react'
import { motion } from 'framer-motion'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { cn } from '@/lib/utils'

interface StatCardProps {
  label: string
  value: string
  icon: LucideIcon
  isLoading?: boolean
  trend?: { value: string; positive: boolean }
  accent?: 'primary' | 'success' | 'warning' | 'destructive'
}

const accentClasses: Record<NonNullable<StatCardProps['accent']>, string> = {
  primary: 'border-primary/20 bg-primary/[0.12] text-primary shadow-[0_18px_30px_-24px_rgba(244,123,63,0.9)]',
  success: 'border-success/20 bg-success/[0.12] text-success shadow-[0_18px_30px_-24px_rgba(61,188,152,0.9)]',
  warning: 'border-warning/20 bg-warning/[0.12] text-warning shadow-[0_18px_30px_-24px_rgba(245,165,36,0.9)]',
  destructive: 'border-destructive/20 bg-destructive/[0.12] text-destructive shadow-[0_18px_30px_-24px_rgba(239,68,68,0.9)]',
}

export function StatCard({ label, value, icon: Icon, isLoading, trend, accent = 'primary' }: StatCardProps) {
  return (
    <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
      <Card className="overflow-hidden border-border/70 bg-card/80 backdrop-blur-sm">
        <CardContent className="relative flex items-start justify-between gap-4 p-5">
          <div className="absolute inset-x-5 top-0 h-px bg-gradient-to-r from-transparent via-primary/40 to-transparent" />
          <div className="space-y-2">
            <p className="text-[0.72rem] font-semibold uppercase tracking-[0.18em] text-muted-foreground">{label}</p>
            {isLoading ? (
              <Skeleton className="h-7 w-24" />
            ) : (
              <p className="text-3xl font-semibold tracking-tight">{value}</p>
            )}
            {trend && !isLoading && (
              <p className={cn('text-xs font-medium uppercase tracking-[0.12em]', trend.positive ? 'text-success' : 'text-destructive')}>
                {trend.value}
              </p>
            )}
          </div>
          <div
            className={cn(
              'flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border',
              accentClasses[accent],
            )}
          >
            <Icon className="h-5 w-5" />
          </div>
        </CardContent>
      </Card>
    </motion.div>
  )
}
