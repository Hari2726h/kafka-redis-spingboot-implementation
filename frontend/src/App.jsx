import React from 'react'
import ProductForm from './components/ProductForm'
import ProductList from './components/ProductList'
import HistoryList from './components/HistoryList'
import { getProducts, searchProducts } from './api'

export default function App() {
  const [products, setProducts] = React.useState([])
  const [loading, setLoading] = React.useState(true)
  const [error, setError] = React.useState('')
  const [searchTerm, setSearchTerm] = React.useState('')
  const [refreshToken, setRefreshToken] = React.useState(0)
  const [editingProduct, setEditingProduct] = React.useState(null)

  React.useEffect(() => {
    let alive = true

    async function loadProducts() {
      setLoading(true)
      setError('')

      try {
        const data = searchTerm.trim()
          ? await searchProducts(searchTerm.trim())
          : await getProducts()

        if (alive) {
          setProducts(data)
        }
      } catch (ex) {
        if (alive) {
          setError(ex.message || 'Failed to load products')
        }
      } finally {
        if (alive) {
          setLoading(false)
        }
      }
    }

    loadProducts()

    return () => {
      alive = false
    }
  }, [searchTerm, refreshToken])

  function handleSaved() {
    setEditingProduct(null)
    setRefreshToken(token => token + 1)
  }

  return (
    <div className="app-shell">
      <div className="background-orb orb-one" />
      <div className="background-orb orb-two" />

      <header className="hero panel">
        <div>
          <p className="eyebrow">Spring Boot + React + Kafka + Redis</p>
          <h1>Product Inventory Demo</h1>
          <p className="hero-copy">
            A compact mentor project showing CRUD, validation, enums, Swagger, embedded Kafka,
            Redis caching, Caffeine search cache, and a manual LRU cache.
          </p>
        </div>

        <div className="hero-stats">
          <div>
            <strong>Redis</strong>
            <span>Get-by-id cache</span>
          </div>
          <div>
            <strong>LRU</strong>
            <span>All-products cache</span>
          </div>
          <div>
            <strong>Caffeine</strong>
            <span>Search cache</span>
          </div>
        </div>
      </header>

      <main className="grid-layout">
        <section className="stack">
          <ProductForm
            editingProduct={editingProduct}
            onSaved={handleSaved}
            onCancel={() => setEditingProduct(null)}
          />

          <div className="panel search-panel">
            <div className="section-heading">
              <h2>Search</h2>
              <span className="muted">Hits the Caffeine cache path</span>
            </div>
            <input
              value={searchTerm}
              onChange={event => setSearchTerm(event.target.value)}
              placeholder="Search by product name"
            />
          </div>
        </section>

        <section className="stack">
          <ProductList
            products={products}
            loading={loading}
            error={error}
            onEdit={setEditingProduct}
            onRefresh={handleSaved}
          />

          <HistoryList refreshToken={refreshToken} />
        </section>
      </main>
    </div>
  )
}
