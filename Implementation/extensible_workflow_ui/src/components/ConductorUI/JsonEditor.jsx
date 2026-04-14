import React, { useRef, useEffect } from 'react'

const PLACEHOLDER = `Paste your workflow JSON here...

Supports two formats:

1) flows.definition.Workflow:
{
  "id": "myFlow",
  "name": "My Flow",
  "root": { "id": "step1", "type": "TASK", "nextStepId": "step2", "message": "First step" },
  "steps": [
    { "id": "step2", "type": "USER_TASK", "message": "Review" }
  ]
}

2) Conductor-style:
{
  "name": "my_workflow",
  "tasks": [
    { "name": "task_1", "type": "SIMPLE" }
  ]
}`

export default function JsonEditor({ value, onChange, onRender, error, onLoadExample, onLoadConditional, onLoadConductor }) {
  const ref = useRef(null)

  useEffect(() => {
    const el = ref.current
    if (!el) return
    const handler = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') { e.preventDefault(); onRender() }
    }
    el.addEventListener('keydown', handler)
    return () => el.removeEventListener('keydown', handler)
  }, [onRender])

  return (
    <div style={{
      width: 380, flexShrink: 0,
      display: 'flex', flexDirection: 'column',
      borderRight: '1px solid #e5e7eb',
      background: '#fff',
    }}>
      {/* Header */}
      <div style={{
        padding: '8px 14px', borderBottom: '1px solid #e5e7eb',
        fontSize: 11, fontWeight: 600, color: '#6b7280',
        textTransform: 'uppercase', letterSpacing: '0.06em',
        display: 'flex', alignItems: 'center', gap: 8,
      }}>
        <span>JSON Editor</span>
        <div style={{ flex: 1 }} />
      </div>

      {/* Textarea */}
      <textarea
        ref={ref}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={PLACEHOLDER}
        spellCheck={false}
        style={{
          flex: 1, resize: 'none', border: 'none', outline: 'none',
          fontFamily: 'ui-monospace, monospace', fontSize: 12,
          lineHeight: 1.6, padding: 14, color: '#111827',
          background: '#fafafa',
        }}
      />

      {/* Error */}
      {error && (
        <div style={{
          padding: '6px 14px', fontSize: 11,
          color: '#dc2626', background: '#fef2f2',
          borderTop: '1px solid #fecaca',
        }}>
          ⚠ {error}
        </div>
      )}

      {/* Footer */}
      <div style={{
        padding: '8px 14px', borderTop: '1px solid #e5e7eb',
        display: 'flex', gap: 6, alignItems: 'center',
        background: '#f9fafb',
      }}>
        <span style={{ fontSize: 10, color: '#9ca3af', flex: 1 }}>Ctrl+Enter to render</span>
        <button onClick={onLoadExample} style={btnStyle('#fff', '#e5e7eb', '#374151')}>
          Simple
        </button>
        <button onClick={onLoadConditional} style={btnStyle('#fff', '#e5e7eb', '#374151')}>
          Conditional
        </button>
        <button onClick={onLoadConductor} style={btnStyle('#fff', '#e5e7eb', '#374151')}>
          Conductor
        </button>
        <button onClick={onRender} style={btnStyle('#2563eb', '#2563eb', '#fff')}>
          ▶ Render
        </button>
      </div>
    </div>
  )
}

const btnStyle = (bg, border, color) => ({
  fontSize: 11, fontWeight: 600,
  padding: '4px 10px', borderRadius: 5,
  border: `1px solid ${border}`,
  background: bg, color, cursor: 'pointer',
})
