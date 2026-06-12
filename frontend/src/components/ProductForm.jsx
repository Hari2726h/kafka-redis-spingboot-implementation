import React from 'react'
import { createProduct, updateProduct } from '../api'

const emptyForm = { name: '', price: '' }

export default function ProductForm({ editingProduct, onSaved, onCancel }) {
  const [form, setForm] = React.useState(emptyForm)
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState('')

  React.useEffect(() => {
    if (editingProduct) {
      setForm({
        name: editingProduct.name ?? '',
        price: editingProduct.price ?? ''
      })
    } else {
      setForm(emptyForm)
    }
    setError('')
  }, [editingProduct])

  async function submit(event) {
    event.preventDefault()
    setLoading(true)
    setError('')

    const payload = {
      name: form.name.trim(),
      price: Number(form.price)
    }

    try {
      if (editingProduct) {
        await updateProduct(editingProduct.id, payload)
      } else {
        await createProduct(payload)
      }

      setForm(emptyForm)
      onSaved?.()
    } catch (ex) {
      setError(ex.message || 'Failed to save product')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form className="panel form-panel" onSubmit={submit}>
      <div className="section-heading">
        <h2>{editingProduct ? 'Update Product' : 'Create Product'}</h2>
        {editingProduct && (
          <button type="button" className="ghost-button" onClick={onCancel}>
            Cancel edit
          </button>
        )}
      </div>

      <label className="field">
        <span>Name</span>
        <input
          value={form.name}
          onChange={event => setForm(current => ({ ...current, name: event.target.value }))}
          placeholder="Laptop"
          required
        />
      </label>

      <label className="field">
        <span>Price</span>
        <input
          type="number"
          min="0"
          step="0.01"
          value={form.price}
          onChange={event => setForm(current => ({ ...current, price: event.target.value }))}
          placeholder="49999"
          required
        />
      </label>

      <div className="actions-row">
        <button type="submit" className="primary-button" disabled={loading}>
          {loading ? 'Saving...' : editingProduct ? 'Update Product' : 'Create Product'}
        </button>
      </div>

      {error && <p className="error-text">{error}</p>}
    </form>
  )
}
