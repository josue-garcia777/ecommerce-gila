import { createElement } from 'react'
import { Navigate, Outlet, useLocation, useRoutes } from 'react-router-dom'
import type { RouteObject } from 'react-router-dom'
import { Nav } from './components/Nav'
import { useAuth } from './hooks/useAuth'
import AdminProductsPage from './pages/AdminProductsPage'
import AuthPage from './pages/AuthPage'
import CartPage from './pages/CartPage'
import ImportPage from './pages/ImportPage'
import OrderConfirmationPage from './pages/OrderConfirmationPage'
import OrdersPage from './pages/OrdersPage'
import HomePage from './pages/HomePage'

const RequireCustomer = () => {
  const { hasRole, isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return createElement(Navigate, {
      to: '/login',
      replace: true,
      state: { from: `${location.pathname}${location.search}` },
    })
  }

  return hasRole('CUSTOMER')
    ? createElement(Outlet)
    : createElement(Navigate, { to: '/', replace: true })
}

const RequireAdmin = () => {
  const { hasRole, isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return createElement(Navigate, {
      to: '/login',
      replace: true,
      state: { from: `${location.pathname}${location.search}` },
    })
  }

  return hasRole('ADMIN')
    ? createElement(Outlet)
    : createElement(Navigate, { to: '/', replace: true })
}

const routes: RouteObject[] = [
  {
    path: '/',
    Component: Nav,
    children: [
      { index: true, Component: HomePage },
      { path: 'login', Component: AuthPage },
      { path: 'register', Component: AuthPage },
      {
        Component: RequireCustomer,
        children: [
          { path: 'cart', Component: CartPage },
          { path: 'orders', Component: OrdersPage },
          { path: 'orders/:orderId', Component: OrdersPage },
          { path: 'orders/:orderId/confirmation', Component: OrderConfirmationPage },
        ],
      },
      {
        Component: RequireAdmin,
        children: [
          { path: 'products', Component: AdminProductsPage },
          { path: 'imports', Component: ImportPage },
        ],
      },
      { path: '*', element: createElement(Navigate, { to: '/', replace: true }) },
    ],
  },
]

export const AppRoutes = () => useRoutes(routes)
