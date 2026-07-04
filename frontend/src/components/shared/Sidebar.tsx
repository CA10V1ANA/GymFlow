import { NavLink } from 'react-router-dom'
import { Dumbbell, PanelLeftClose, PanelLeftOpen, Sparkles } from 'lucide-react'
import { motion } from 'framer-motion'
import { cn } from '@/lib/utils'
import { useAuth } from '@/hooks/useAuth'
import { navItems } from '@/components/shared/navigation'

interface SidebarProps {
  collapsed: boolean
  onToggle: () => void
  mobileOpen?: boolean
  onCloseMobile?: () => void
}

export function Sidebar({ collapsed, onToggle, mobileOpen, onCloseMobile }: SidebarProps) {
  const { user } = useAuth()
  const items = navItems.filter((item) => !user || item.roles.includes(user.role))

  return (
    <>
      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 lg:hidden"
          onClick={onCloseMobile}
          aria-hidden
        />
      )}
      <motion.aside
        animate={{ width: collapsed ? 76 : 264 }}
        transition={{ duration: 0.2, ease: 'easeInOut' }}
        className={cn(
          'fixed inset-y-0 left-0 z-50 flex flex-col border-r border-sidebar-border bg-sidebar/95 text-sidebar-foreground shadow-[0_30px_80px_-48px_rgba(0,0,0,0.9)] backdrop-blur-xl transition-transform lg:translate-x-0 lg:static lg:z-0',
          mobileOpen ? 'translate-x-0' : '-translate-x-full',
        )}
      >
        <div className="border-b border-sidebar-border px-4 py-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-primary via-primary to-warning text-primary-foreground shadow-[0_20px_40px_-22px_rgba(244,123,63,0.95)]">
              <Dumbbell className="h-5 w-5" />
            </div>
            {!collapsed && (
              <div className="min-w-0">
                <p className="truncate text-base font-semibold">GymFlow Pro</p>
                <p className="text-xs uppercase tracking-[0.22em] text-sidebar-foreground/55">Athletic Ops</p>
              </div>
            )}
          </div>

          {!collapsed && (
            <div className="mt-4 rounded-2xl border border-sidebar-border bg-white/5 p-3">
              <div className="flex items-center gap-2 text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-sidebar-foreground/60">
                <Sparkles className="h-3.5 w-3.5 text-primary" />
                Centro de comando
              </div>
              <p className="mt-2 text-sm leading-5 text-sidebar-foreground/84">
                Tudo o que move a academia em uma superfície só.
              </p>
            </div>
          )}
        </div>

        <nav className="flex-1 space-y-1 overflow-y-auto p-3 scrollbar-thin">
          {items.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              onClick={onCloseMobile}
              className={({ isActive }) =>
                cn(
                  'group relative flex items-center gap-3 rounded-2xl px-3 py-3 text-sm font-medium transition-all duration-200',
                  isActive
                    ? 'bg-gradient-to-r from-sidebar-accent via-sidebar-accent to-primary/18 text-sidebar-accent-foreground shadow-[inset_0_1px_0_rgba(255,255,255,0.06)]'
                    : 'text-sidebar-foreground/70 hover:bg-sidebar-accent/70 hover:text-sidebar-accent-foreground',
                )
              }
            >
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-white/[0.06] bg-white/[0.05] transition-colors group-hover:bg-white/10">
                <item.icon className="h-4.5 w-4.5 shrink-0" />
              </div>
              {!collapsed && <span className="truncate">{item.label}</span>}
            </NavLink>
          ))}
        </nav>

        <button
          type="button"
          onClick={onToggle}
          className="hidden items-center gap-2 border-t border-sidebar-border px-4 py-4 text-sm text-sidebar-foreground/70 transition-colors hover:text-sidebar-foreground lg:flex"
        >
          {collapsed ? <PanelLeftOpen className="h-4.5 w-4.5" /> : <PanelLeftClose className="h-4.5 w-4.5" />}
          {!collapsed && 'Recolher menu'}
        </button>
      </motion.aside>
    </>
  )
}
