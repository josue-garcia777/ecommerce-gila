import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useCart } from '../context/CartContext'

const navigationClassName = ({ isActive }: { isActive: boolean }) =>
  [
    'rounded-full px-3.5 py-2.5 text-sm text-[#445049] no-underline transition-colors',
    'hover:bg-[#e9eee4] hover:text-moss-dark max-[760px]:px-2.5 max-[760px]:py-2 max-[760px]:text-xs',
    isActive ? 'bg-[#e9eee4] text-moss-dark' : '',
  ].join(' ')

export const Nav = () => {
  const { itemCount } = useCart()
  const { isAdmin, isAuthenticated, logout, user } = useAuth()

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-20 flex h-[82px] items-center justify-between border-b border-line bg-paper/90 px-[clamp(20px,5vw,72px)] backdrop-blur-[14px] max-[760px]:h-auto max-[760px]:min-h-[72px] max-[760px]:items-start max-[760px]:px-4 max-[760px]:py-3">
        <Link
          className="flex items-center gap-3 text-left text-ink no-underline"
          to="/"
          aria-label="Open storefront"
        >
          <span>
            <strong className="block font-display text-lg max-[500px]:hidden">E-Commerce</strong>
          </span>
        </Link>
        <nav
          className="flex items-center gap-1 max-[760px]:flex-wrap max-[760px]:justify-end"
          aria-label="Primary navigation"
        >
          <NavLink to="/" end className={navigationClassName}>
            Store
          </NavLink>
          {isAdmin && (
            <>
              <NavLink to="/products" className={navigationClassName}>
                Admin
              </NavLink>
              <NavLink to="/imports" className={navigationClassName}>
                Import
              </NavLink>
            </>
          )}
          {isAuthenticated ? (
            <>
              <NavLink to="/orders" className={navigationClassName}>
                Orders
              </NavLink>
              <NavLink
                to="/cart"
                className={({ isActive }) =>
                  `ml-2 rounded-full bg-moss px-3.5 py-2.5 text-sm text-white no-underline transition-colors hover:bg-moss-dark max-[760px]:px-2.5 max-[760px]:py-2 max-[760px]:text-xs ${isActive ? 'bg-moss-dark' : ''}`
                }
              >
                Cart{' '}
                <span className="ml-1.5 inline-grid h-[21px] min-w-[21px] place-items-center rounded-full bg-lime px-1 text-xs font-bold text-ink">
                  {itemCount}
                </span>
              </NavLink>
              <button
                type="button"
                className={navigationClassName({ isActive: false })}
                onClick={logout}
                title={user?.email}
              >
                Log out
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={navigationClassName}>
                Login
              </NavLink>
              
              <NavLink to="/login" className={navigationClassName}>
                Go to Admin Panel
              </NavLink>
            </>
          )}
        </nav>
      </header>

      <main className="mx-auto w-full max-w-[1440px] flex-1">
        <Outlet />
      </main>
    </div>
  )
}
