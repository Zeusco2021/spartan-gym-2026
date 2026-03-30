import { useAuth } from './useAuth';

export function useRoleGuard(allowedRoles: Array<'client' | 'trainer' | 'admin'>) {
  const { user, isAuthenticated } = useAuth();
  const hasAccess = isAuthenticated && !!user && allowedRoles.includes(user.role);
  return { hasAccess, userRole: user?.role };
}
