import React from 'react'

export default function TopBar({ workflowName, workflowId, format, nodeCount, status, onFitView }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: '0 20px', height: 48,
      background: '#fff', borderBottom: '1px solid #e5e7eb',
      flexShrink: 0,
    }}>
      <span style={{ fontWeight: 700, fontSize: 15, color: '#111827' }}>Workflow Viewer</span>

      {workflowName && (
        <span style={{ color: '#6b7280', fontSize: 13 }}>
          {workflowName}{workflowId && workflowId !== workflowName ? ` (${workflowId})` : ''}
        </span>
      )}

      {format && (
        <span style={{
          fontSize: 11, fontWeight: 600, padding: '2px 8px', borderRadius: 4,
          background: format === 'flows' ? '#eff6ff' : '#f0fdf4',
          color: format === 'flows' ? '#2563eb' : '#16a34a',
          border: `1px solid ${format === 'flows' ? '#bfdbfe' : '#bbf7d0'}`,
        }}>
          {format === 'flows' ? 'flows.definition' : 'Conductor'}
        </span>
      )}

      <div style={{ flex: 1 }} />

      {status === 'error' && (
        <span style={{ fontSize: 12, color: '#dc2626' }}>Parse error</span>
      )}
      {nodeCount !== null && (
        <span style={{ fontSize: 12, color: '#6b7280' }}>{nodeCount} steps</span>
      )}

      <button onClick={onFitView} style={{
        fontSize: 12, padding: '5px 12px',
        border: '1px solid #e5e7eb', borderRadius: 6,
        background: '#fff', color: '#374151', cursor: 'pointer',
      }}>
        Fit View
      </button>
    </div>
  )
}
