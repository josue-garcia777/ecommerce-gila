import { FormEvent, useState } from 'react'
import { Message } from '../components/Message'
import { isTerminalImportStatus, useProductImport } from '../hooks/useProductImport'

export default function ImportPage() {
  const [file, setFile] = useState<File | null>(null)
  const productImport = useProductImport()

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (!file) {
      productImport.setError('Choose a CSV file before uploading')
      return
    }
    await productImport.startImport(file)
  }

  return (
    <section className="content-section narrow">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Catalog operations</p>
          <h1>Product import</h1>
          <p>Upload UTF-8 CSV with the exact seven-column header.</p>
        </div>
      </div>

      <form className="upload-panel" onSubmit={submit}>
        <label className="drop-zone">
          <span className="upload-icon">↑</span>
          <strong>{file ? file.name : 'Choose a CSV file'}</strong>
          <small> .csv files only</small>
          <input
            type="file"
            accept=".csv,text/csv"
            onChange={(event) => setFile(event.target.files?.[0] ?? null)}
          />
        </label>
        <button className="primary" disabled={!file || productImport.uploading}>
          {productImport.uploading ? 'Submitting…' : 'Start import'}
        </button>
      </form>

      {productImport.error && <Message tone="error">{productImport.error}</Message>}
      {productImport.result && (
        <div className="import-result">
          <div className="result-header">
            <div>
              <p className="eyebrow">{productImport.result.filename}</p>
              <h2>{productImport.result.status.replaceAll('_', ' ')}</h2>
            </div>
            {!isTerminalImportStatus(productImport.result.status) && (
              <span className="spinner" aria-label="Import processing" />
            )}
          </div>
          <div className="summary-grid">
            <div>
              <strong>{productImport.result.summary.created}</strong>
              <span>Created</span>
            </div>
            <div>
              <strong>{productImport.result.summary.updated}</strong>
              <span>Updated</span>
            </div>
            <div>
              <strong>{productImport.result.summary.rejected}</strong>
              <span>Rejected</span>
            </div>
          </div>
          {productImport.result.status === 'FAILED' && (
            <Message tone="error">Processing failed unexpectedly. Upload the file again.</Message>
          )}
          {productImport.result.rejectedRows.length > 0 && (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Row</th>
                    <th>SKU</th>
                    <th>Reason</th>
                  </tr>
                </thead>
                <tbody>
                  {productImport.result.rejectedRows.map((row) => (
                    <tr key={row.rowNumber}>
                      <td>{row.rowNumber}</td>
                      <td>{row.sku ?? '—'}</td>
                      <td>{row.reason}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </section>
  )
}
