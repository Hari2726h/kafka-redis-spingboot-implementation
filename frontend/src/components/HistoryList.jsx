import React from 'react'
import { getHistory } from '../api'

export default function HistoryList({ refreshToken }) {
  const [history, setHistory] = React.useState([])
  const [loading, setLoading] = React.useState(true)
  const [error, setError] = React.useState('')

  React.useEffect(() => {
    let alive = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const data = await getHistory()
        if (alive) {
          setHistory(data)
        }
      } catch (ex) {
        if (alive) {
          setError(ex.message || 'Failed to load history')
        }
      } finally {
        if (alive) {
          setLoading(false)
        }
      }
    }

    load()

    return () => {
      alive = false
    }
  }, [refreshToken])

  return (
    <div className="panel">
      <div className="section-heading">
        <h2>History</h2>
        <span className="muted">Kafka consumer writes here</span>
      </div>

      {loading ? (
        <p className="muted">Loading history...</p>
      ) : error ? (
        <p className="error-text">{error}</p>
      ) : history.length === 0 ? (
        <p className="muted empty-state">No history yet. Create or update a product to generate a Kafka event.</p>
      ) : (
        <div className="table-card compact">
          {history.map(item => (
            <div key={item.id} className="table-row">
              <div>
                <strong>{item.action}</strong>
                <div className="muted">
                  {item.objectType} #{item.objectId}
                </div>
              </div>
              <div className="muted">{new Date(item.createdAt).toLocaleString()}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}