import { useLocation, useNavigate } from 'react-router-dom'
import { CalendarDays, LogOut, Menu, Moon, Sun, User } from 'lucide-react'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { navItems } from '@/components/shared/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useTheme } from '@/hooks/useTheme'

const roleLabels: Record<string, string> = {
  ADMIN: 'Administrador',
  RECEPTIONIST: 'Recepcionista',
  INSTRUCTOR: 'Instrutor',
  STUDENT: 'Aluno',
}

interface NavbarProps {
  onOpenMobileMenu: () => void
}

export function Navbar({ onOpenMobileMenu }: NavbarProps) {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const location = useLocation()

  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  const initials = user?.name
    ?.split(' ')
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase()

  const activeSection = navItems.find(
    (item) => location.pathname === item.path || location.pathname.startsWith(`${item.path}/`),
  )

  const todayLabel = new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: 'short',
  }).format(new Date())

  return (
    <header className="sticky top-0 z-30 border-b border-border/60 bg-background/70 px-4 py-3 backdrop-blur-xl sm:px-6">
      <div className="flex items-center justify-between gap-4">
        <div className="flex min-w-0 items-center gap-3">
          <Button variant="ghost" size="icon" className="lg:hidden" onClick={onOpenMobileMenu}>
            <Menu className="h-5 w-5" />
          </Button>

          <div className="min-w-0">
            <p className="text-[0.68rem] font-semibold uppercase tracking-[0.24em] text-primary/75">
              Operação ativa
            </p>
            <div className="flex items-center gap-2">
              <h2 className="truncate text-lg font-semibold">{activeSection?.label ?? 'Painel'}</h2>
              <span className="hidden rounded-full border border-border/70 bg-card/70 px-2.5 py-1 text-[0.68rem] font-medium uppercase tracking-[0.18em] text-muted-foreground md:inline-flex">
                <CalendarDays className="mr-1.5 h-3.5 w-3.5" />
                {todayLabel}
              </span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="icon"
            className="rounded-2xl border border-border/70 bg-card/70"
            onClick={toggleTheme}
            aria-label="Alternar tema"
          >
            {theme === 'dark' ? <Sun className="h-4.5 w-4.5" /> : <Moon className="h-4.5 w-4.5" />}
          </Button>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                type="button"
                className="flex items-center gap-3 rounded-2xl border border-border/70 bg-card/70 px-2.5 py-1.5 transition-colors hover:bg-accent"
              >
                <Avatar className="h-9 w-9 border border-border/70">
                  <AvatarFallback>{initials || <User className="h-4 w-4" />}</AvatarFallback>
                </Avatar>
                <div className="hidden text-left sm:block">
                  <p className="text-sm font-medium leading-none">{user?.name}</p>
                  <p className="mt-1 text-[0.72rem] uppercase tracking-[0.16em] text-muted-foreground">
                    {user ? roleLabels[user.role] : ''}
                  </p>
                </div>
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>
                <p className="font-medium">{user?.name}</p>
                <p className="text-xs font-normal text-muted-foreground">{user?.email}</p>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={handleLogout} className="text-destructive focus:text-destructive">
                <LogOut className="h-4 w-4" />
                Sair
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </header>
  )
}
