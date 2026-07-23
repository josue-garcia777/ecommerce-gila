import { createElement } from 'react'
import { Navigate, useRoutes } from 'react-router-dom'
import type { RouteObject } from 'react-router-dom'
import { Nav } from './components/Nav'
import AdminProductsPage from './pages/AdminProductsPage'
import CartPage from './pages/CartPage'
import ImportPage from './pages/ImportPage'
import OrderConfirmationPage from './pages/OrderConfirmationPage'
import OrdersPage from './pages/OrdersPage'
import StorefrontPage from './pages/StorefrontPage'

const routes: RouteObject[] = [
  {
    path: '/',
    Component: Nav,
    children: [
      { index: true, Component: StorefrontPage },
      { path: 'products', Component: AdminProductsPage },
      { path: 'imports', Component: ImportPage },
      { path: 'cart', Component: CartPage },
      { path: 'orders', Component: OrdersPage },
      { path: 'orders/:orderId', Component: OrdersPage },
      { path: 'orders/:orderId/confirmation', Component: OrderConfirmationPage },
      { path: '*', element: createElement(Navigate, { to: '/', replace: true }) },
    ],
  },
]

export function AppRoutes() {
  return useRoutes(routes)
}
