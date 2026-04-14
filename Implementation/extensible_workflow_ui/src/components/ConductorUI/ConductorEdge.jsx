import React, { memo } from 'react'
import { getBezierPath, EdgeLabelRenderer, BaseEdge } from 'reactflow'

const ConductorEdge = memo(({ id, sourceX, sourceY, targetX, targetY,
  sourcePosition, targetPosition, label, selected, markerEnd }) => {

  const [edgePath, labelX, labelY] = getBezierPath({
    sourceX, sourceY, sourcePosition, targetX, targetY, targetPosition
  })

  return (
    <>
      <BaseEdge id={id} path={edgePath} markerEnd={markerEnd}
        style={{ stroke: selected ? '#2563eb' : '#d1d5db', strokeWidth: selected ? 2 : 1.5 }} />
      {label && (
        <EdgeLabelRenderer>
          <div style={{
            position: 'absolute',
            transform: `translate(-50%,-50%) translate(${labelX}px,${labelY}px)`,
            fontSize: 10, fontWeight: 500,
            color: selected ? '#2563eb' : '#6b7280',
            background: '#fff',
            border: '1px solid #e5e7eb',
            padding: '1px 6px',
            borderRadius: 4,
            whiteSpace: 'nowrap',
            pointerEvents: 'all',
          }}>
            {label}
          </div>
        </EdgeLabelRenderer>
      )}
    </>
  )
})

ConductorEdge.displayName = 'ConductorEdge'
export default ConductorEdge
