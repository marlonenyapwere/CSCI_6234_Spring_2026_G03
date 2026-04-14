import React, { memo } from 'react'
import { Handle, Position } from 'reactflow'
import { TYPE_LABEL } from '../../utils/parseWorkflow'

const ConductorNode = memo(({ data, selected }) => {
  const { type, name, color, message } = data
  const label = TYPE_LABEL[type] || type

  return (
    <div style={{
      width: 200,
      background: '#fff',
      border: `1.5px solid ${selected ? color : '#e5e7eb'}`,
      borderRadius: 8,
      boxShadow: selected ? `0 0 0 3px ${color}22` : '0 1px 3px rgba(0,0,0,0.07)',
      fontFamily: 'system-ui, sans-serif',
      cursor: 'grab',
      transition: 'border-color 0.15s, box-shadow 0.15s',
    }}>
      <Handle type="target" position={Position.Top}
        style={{ background: color, borderColor: color, width: 8, height: 8 }} />

      {/* Color indicator stripe */}
      <div style={{ height: 3, background: color, borderRadius: '6px 6px 0 0' }} />

      <div style={{ padding: '8px 12px' }}>
        <div style={{ fontSize: 10, fontWeight: 600, color, textTransform: 'uppercase',
                      letterSpacing: '0.08em', marginBottom: 3 }}>
          {label}
        </div>
        <div style={{ fontSize: 13, fontWeight: 500, color: '#111827',
                      overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {name}
        </div>
        {message && message !== name && (
          <div style={{ fontSize: 11, color: '#6b7280', marginTop: 2,
                        overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {message}
          </div>
        )}
      </div>

      <Handle type="source" position={Position.Bottom}
        style={{ background: color, borderColor: color, width: 8, height: 8 }} />
    </div>
  )
})

ConductorNode.displayName = 'ConductorNode'
export default ConductorNode
