export function Message({
  tone = 'info',
  children,
}: {
  tone?: 'info' | 'error' | 'success'
  children: React.ReactNode
}) {
  return (
    <div className={`message ${tone}`} role={tone === 'error' ? 'alert' : 'status'}>
      {children}
    </div>
  )
}
