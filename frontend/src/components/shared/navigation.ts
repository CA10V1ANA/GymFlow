import type { LucideIcon } from 'lucide-react'
import {
  Activity,
  BarChart3,
  ClipboardList,
  Dumbbell,
  LayoutDashboard,
  ListChecks,
  Package,
  ShieldCheck,
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
      return '/attendance'
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
]
