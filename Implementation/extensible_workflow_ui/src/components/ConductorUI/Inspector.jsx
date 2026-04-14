import React from 'react'
import { TYPE_LABEL, TYPE_COLOR } from '../../utils/parseWorkflow'

export default function Inspector({ node, onClose }) {
  if (!node) return null
  const { type, name, ref, raw, message } = node.data
  const color = TYPE_COLOR[type] || '#4f6ef7'

  const fields = [
    ['id / ref', ref || '—'],
    ['type', TYPE_LABEL[type] || type],
    ['name', name],
  ]
  if (message && message !== name) fields.push(['message', message])
  if (raw?.nextStepId)   fields.push(['nextStepId', raw.nextStepId])
  if (raw?.taskReferenceName) fields.push(['taskRef', raw.taskReferenceName])
  if (raw?.inputParameters)   fields.push(['inputs', Object.keys(raw.inputParameters).join(', ')])

  return (
    <div style={{
      position: 'absolute', top: 12, right: 12, width: 240,
      background: '#fff', border: '1px solid #e5e7eb',
      borderRadius: 8, padding: 14,
      boxShadow: '0 2px 12px rgba(0,0,0,0.08)',
      zIndex: 100, fontFamily: 'system-ui, sans-serif',
    }}>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 10 }}>
        <div style={{ width: 8, height: 8, borderRadius: '50%', background: color, marginRight: 8 }} />
        <span style={{ fontSize: 11, fontWeight: 700, color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.06em', flex: 1 }}>
          Inspector
        </span>
        <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#9ca3af', fontSize: 16, lineHeight: 1 }}>
          ×
        </button>
      </div>

      {fields.map(([k, v]) => (
        <div key={k} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6, gap: 8 }}>
          <span style={{ fontSize: 11, color: '#9ca3af', flexShrink: 0 }}>{k}</span>
          <span style={{ fontSize: 11, color: '#2563eb', textAlign: 'right', wordBreak: 'break-all' }}>{String(v)}</span>
        </div>
      ))}

      {raw && Object.keys(raw).filter(k => !['id','type','name','message','nextStepId','taskReferenceName','inputParameters'].includes(k) && raw[k] !== null && typeof raw[k] !== 'object').length > 0 && (
        <div style={{ marginTop: 8, paddingTop: 8, borderTop: '1px solid #f3f4f6' }}>
          {Object.entries(raw)
            .filter(([k, v]) => !['id','type','name','message','nextStepId','taskReferenceName','inputParameters'].includes(k) && v !== null && typeof v !== 'object')
            .map(([k, v]) => (
              <div key={k} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4, gap: 8 }}>
                <span style={{ fontSize: 11, color: '#9ca3af', flexShrink: 0 }}>{k}</span>
                <span style={{ fontSize: 11, color: '#374151', textAlign: 'right' }}>{String(v)}</span>
              </div>
            ))}
        </div>
      )}
    </div>
  )
}
