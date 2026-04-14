import React, { useState, useCallback, useRef, useMemo } from 'react'
import ReactFlow, {
  ReactFlowProvider,
  Background, BackgroundVariant,
  useNodesState, useEdgesState, useReactFlow,
  MiniMap, Controls, MarkerType, Position,
} from 'reactflow'
import 'reactflow/dist/style.css'

import TopBar    from './TopBar'
import JsonEditor from './JsonEditor'
import Inspector from './Inspector'
import ConductorNode from './ConductorNode'
import ConductorEdge from './ConductorEdge'
import { parseWorkflow, detectFormat } from '../../utils/parseWorkflow'
import {
  EXAMPLE_FLOWS_WORKFLOW,
  EXAMPLE_FLOWS_CONDITIONAL,
  EXAMPLE_CONDUCTOR_WORKFLOW,
} from '../../utils/exampleWorkflow'

const nodeTypes = { conductorNode: ConductorNode }
const edgeTypes = { conductorEdge: ConductorEdge }

const defaultEdgeOptions = {
  type: 'conductorEdge',
  markerEnd: { type: MarkerType.ArrowClosed, color: '#d1d5db' },
  sourcePosition: Position.Bottom,
  targetPosition: Position.Top,
}

function FlowCanvas({ nodes, edges, onNodesChange, onEdgesChange, onNodeClick, onPaneClick }) {
  return (
    <ReactFlow
      nodes={nodes} edges={edges}
      onNodesChange={onNodesChange} onEdgesChange={onEdgesChange}
      onNodeClick={onNodeClick} onPaneClick={onPaneClick}
      nodeTypes={nodeTypes} edgeTypes={edgeTypes}
      defaultEdgeOptions={defaultEdgeOptions}
      fitView fitViewOptions={{ padding: 0.25 }}
      minZoom={0.1} maxZoom={3}
      proOptions={{ hideAttribution: true }}
    >
      <Background variant={BackgroundVariant.Dots} gap={20} size={1} color="#e5e7eb" />
      <MiniMap
        nodeColor={(n) => n.data?.color || '#4f6ef7'}
        maskColor="rgba(247,247,248,0.7)"
        style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 6 }}
      />
      <Controls showInteractive={false} />
    </ReactFlow>
  )
}

function ConductorUIInner() {
  const [json, setJson]           = useState('')
  const [error, setError]         = useState(null)
  const [meta, setMeta]           = useState({ name: '', id: '', format: null })
  const [status, setStatus]       = useState('idle')
  const [selectedNode, setSelectedNode] = useState(null)
  const [nodes, setNodes, onNodesChange] = useNodesState([])
  const [edges, setEdges, onEdgesChange] = useEdgesState([])
  const { fitView } = useReactFlow()
  const autoTimer = useRef(null)

  const parseAndRender = useCallback((rawJson) => {
    const src = rawJson ?? json
    if (!src?.trim()) { setError(null); setStatus('idle'); return }

    let wf
    try { wf = JSON.parse(src) }
    catch (e) { setError(e.message); setStatus('error'); return }

    const format = detectFormat(wf)
    if (format === 'unknown') {
      setError('Unrecognised format. Need "tasks" (Conductor) or "root"/"steps" (flows.definition)')
      setStatus('error'); return
    }

    try {
      const { nodes: n, edges: e } = parseWorkflow(wf)
      const vertical = n.map((nd) => ({ ...nd, sourcePosition: Position.Bottom, targetPosition: Position.Top }))
      setNodes(vertical); setEdges(e)
      setError(null); setStatus('ok')
      setMeta({ name: wf.name || '', id: wf.id || '', format })
      setTimeout(() => fitView({ padding: 0.25, duration: 350 }), 50)
    } catch (e) {
      setError('Render error: ' + e.message); setStatus('error')
    }
  }, [json, setNodes, setEdges, fitView])

  const handleChange = useCallback((val) => {
    setJson(val)
    clearTimeout(autoTimer.current)
    autoTimer.current = setTimeout(() => parseAndRender(val), 600)
  }, [parseAndRender])

  const load = useCallback((obj) => {
    const str = JSON.stringify(obj, null, 2)
    setJson(str); parseAndRender(str)
  }, [parseAndRender])

  const nodeCount = useMemo(() => nodes.length > 0 ? Math.max(0, nodes.length - 2) : null, [nodes])

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
      <TopBar
        workflowName={meta.name} workflowId={meta.id}
        format={meta.format} nodeCount={nodeCount} status={status}
        onFitView={() => fitView({ padding: 0.25, duration: 350 })}
      />

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        <JsonEditor
          value={json} onChange={handleChange}
          onRender={() => parseAndRender()}
          error={error}
          onLoadExample={()       => load(EXAMPLE_FLOWS_WORKFLOW)}
          onLoadConditional={()   => load(EXAMPLE_FLOWS_CONDITIONAL)}
          onLoadConductor={()     => load(EXAMPLE_CONDUCTOR_WORKFLOW)}
        />

        <div style={{ flex: 1, position: 'relative', overflow: 'hidden' }}>
          <FlowCanvas
            nodes={nodes} edges={edges}
            onNodesChange={onNodesChange} onEdgesChange={onEdgesChange}
            onNodeClick={(_, node) => setSelectedNode(node)}
            onPaneClick={() => setSelectedNode(null)}
          />

          {selectedNode && (
            <Inspector node={selectedNode} onClose={() => setSelectedNode(null)} />
          )}

          {nodes.length === 0 && (
            <div style={{
              position: 'absolute', inset: 0,
              display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
              gap: 8, pointerEvents: 'none', color: '#9ca3af',
            }}>
              <div style={{ fontSize: 36 }}>⬡</div>
              <div style={{ fontSize: 14, fontWeight: 500 }}>No workflow rendered</div>
              <div style={{ fontSize: 12 }}>Paste JSON and click Render, or pick an example</div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default function ConductorUI() {
  return (
    <ReactFlowProvider>
      <ConductorUIInner />
    </ReactFlowProvider>
  )
}
