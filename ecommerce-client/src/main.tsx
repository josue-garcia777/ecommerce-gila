import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { SWRConfig } from 'swr'
import { swrConfig } from './config/swrConfig'
import { CartProvider } from './context/CartContext'
import { AppRoutes } from './route'
import './styles.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <SWRConfig value={swrConfig}>
      <BrowserRouter>
        <CartProvider>
          <AppRoutes />
        </CartProvider>
      </BrowserRouter>
    </SWRConfig>
  </StrictMode>,
)
