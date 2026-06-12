import React from 'react'
import { deleteProduct } from '../api'

export default function ProductList({ products, loading, error, onEdit, onRefresh }) {
  async function handleDelete(id) {
    const confirmed = window.confirm('Delete this product?')
    if (!confirmed) return

    await deleteProduct(id)
    onRefresh?.()
  }

  if (loading) {
    return <div className="panel">Loading products...</div>
  }

  if (error) {
    return <div className="panel error-panel">{error}</div>
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <h2>Products</h2>
        <span className="muted">{products.length} item(s)</span>
      </div>

      {products.length === 0 ? (
        <p className="muted empty-state">No products yet. Create one to see Redis and history in action.</p>
      ) : (
        <div className="table-card">
          {products.map(product => (
            <div key={product.id} className="table-row">
              <div>
                <strong>{product.name}</strong>
                <div className="muted">Status: {product.status}</div>
              </div>
              <div className="price">₹ {Number(product.price).toFixed(2)}</div>
              <div className="row-actions">
                <button className="ghost-button" type="button" onClick={() => onEdit(product)}>
                  Edit
                </button>
                <button className="danger-button" type="button" onClick={() => handleDelete(product.id)}>
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
