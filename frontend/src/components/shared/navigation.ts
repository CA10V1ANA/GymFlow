import type { LucideIcon } from 'lucide-react'
import {
  Activity,
  BarChart3,
  CalendarCheck,
  ClipboardList,
  Dumbbell,
  LayoutDashboard,
  ListChecks,
  Package,
  ShieldCheck,
  User,
  UserCog,
  Users,
  Wallet,
} from 'lucide-react'
import type { Role } from '@/types/common'

export interface NavItem {
  label: string
  path: string
  icon: LucideIcon
  roles: Role[]
}

export function getDefaultRouteForRole(role: Role): string {
  switch (role) {
    case 'ADMIN':
    case 'RECEPTIONIST':
      return '/dashboard'
    case 'INSTRUCTOR':
      return '/students'
    case 'STUDENT':
      return '/profile'
  }
}

export const navItems: NavItem[] = [
  {
    label: 'Dashboard',
    path: '/dashboard',
    icon: LayoutDashboard,
    roles: ['ADMIN', 'RECEPTIONIST'],
  },
  {
    label: 'Alunos',
    path: '/students',
    icon: Users,
    roles: ['ADMIN', 'RECEPTIONIST', 'INSTRUCTOR'],
  },
  {
    label: 'Planos',
    path: '/plans',
    icon: ListChecks,
    roles: ['ADMIN', 'RECEPTIONIST'],
  },
  {
    label: 'Matrículas',
    path: '/enrollments',
    icon: ClipboardList,
    roles: ['ADMIN', 'RECEPTIONIST'],
  },
  {
    label: 'Treinos',
    path: '/workouts',
    icon: Dumbbell,
    roles: ['ADMIN', 'INSTRUCTOR'],
  },
  {
    label: 'Exercícios',
    path: '/exercises',
    icon: Activity,
    roles: ['ADMIN', 'INSTRUCTOR'],
  },
  {
    label: 'Presença',
    path: '/attendance',
    icon: ListChecks,
    roles: ['ADMIN', 'RECEPTIONIST', 'INSTRUCTOR'],
  },
  {
    label: 'Funcionários',
    path: '/employees',
    icon: UserCog,
    roles: ['ADMIN'],
  },
  {
    label: 'Financeiro',
    path: '/financial',
    icon: Wallet,
    roles: ['ADMIN', 'RECEPTIONIST'],
  },
  {
    label: 'Produtos',
    path: '/products',
    icon: Package,
    roles: ['ADMIN', 'RECEPTIONIST'],
  },
  {
    label: 'Relatórios',
    path: '/reports',
    icon: BarChart3,
    roles: ['ADMIN', 'RECEPTIONIST'],
  },
  {
    label: 'Auditoria',
    path: '/audit',
    icon: ShieldCheck,
    roles: ['ADMIN'],
  },
  {
    label: 'Meu Perfil',
    path: '/profile',
    icon: User,
    roles: ['STUDENT'],
  },
  {
    label: 'Meus Treinos',
    path: '/my-workouts',
    icon: Dumbbell,
    roles: ['STUDENT'],
  },
  {
    label: 'Minha Presenca',
    path: '/my-attendance',
    icon: CalendarCheck,
    roles: ['STUDENT'],
  },
]
