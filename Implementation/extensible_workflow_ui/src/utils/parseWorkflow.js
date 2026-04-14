// ─────────────────────────────────────────────────────────────────
//  parseWorkflow.js
//
//  Supports TWO JSON formats:
//
//  Format A — flows.definition.Workflow  (new)
//  {
//    "id": "approvalFlow",
//    "name": "Approval Flow",
//    "root": { "id": "step1", "type": "TASK", "nextStepId": "step2", ... },
//    "steps": [ { "id": "step2", ... }, ... ]
//  }
//
//  Format B — Conductor-style  (original)
//  {
//    "name": "my_workflow",
//    "tasks": [ { "name": "t1", "type": "SIMPLE", ... }, ... ]
//  }
//
//  Layout: TOP → BOTTOM (vertical, BFS-layered)
// ─────────────────────────────────────────────────────────────────

export const NODE_W = 200
export const NODE_H = 64

// ── Colors per task type ──────────────────────────────────────────
export const TYPE_COLOR = {
  // flows.definition.Workflow types
  TASK:          '#4f6ef7',
  USER_TASK:     '#0891b2',
  SEQUENTIAL:    '#4f6ef7',
  PARALLEL:      '#d97706',
  CONDITIONAL:   '#ea580c',
  REPEAT:        '#7c3aed',
  // Conductor types
  SIMPLE:        '#4f6ef7',
  HTTP:          '#4f6ef7',
  LAMBDA:        '#7c3aed',
  EVENT:         '#0891b2',
  WAIT:          '#9ca3af',
  FORK_JOIN:     '#d97706',
  JOIN:          '#d97706',
  SWITCH:        '#ea580c',
  DECISION:      '#ea580c',
  SUB_WORKFLOW:  '#7c3aed',
  // Sentinels
  START:         '#16a96b',
  END:           '#e53e5e',
}

export const TYPE_LABEL = {
  TASK:          'Task',
  USER_TASK:     'User Task',
  SEQUENTIAL:    'Sequential',
  PARALLEL:      'Parallel',
  CONDITIONAL:   'Conditional',
  REPEAT:        'Repeat',
  SIMPLE:        'Simple',
  HTTP:          'HTTP',
  LAMBDA:        'Lambda',
  EVENT:         'Event',
  WAIT:          'Wait',
  FORK_JOIN:     'Fork / Join',
  JOIN:          'Join',
  SWITCH:        'Switch',
  DECISION:      'Decision',
  SUB_WORKFLOW:  'Sub-Workflow',
  START:         'Start',
  END:           'End',
}

// ── Detect which format the JSON is ──────────────────────────────
export function detectFormat(wf) {
  if (wf.root || (wf.steps && !wf.tasks)) return 'flows'
  if (wf.tasks) return 'conductor'
  return 'unknown'
}

// ── Main entry point ─────────────────────────────────────────────
export function parseWorkflow(wf) {
  const format = detectFormat(wf)
  if (format === 'flows') return parseFlowsWorkflow(wf)
  return parseConductorWorkflow(wf)
}

// ═════════════════════════════════════════════════════════════════
//  FORMAT A: flows.definition.Workflow
//  Traverses: root → nextStepId → steps[]
//  Also handles: step, initial, onSuccess, onFailure (nested)
// ═════════════════════════════════════════════════════════════════

function parseFlowsWorkflow(wf) {
  const rfNodes = []
  const rfEdges = []
  const visited = new Set()

  // Build a flat stepMap (id → step) mirroring Java's buildStepMap()
  const stepMap = {}

  function indexStep(step) {
    if (!step) return
    if (step.id) stepMap[step.id] = step
    ;(step.steps || []).forEach(indexStep)
    indexStep(step.step)
    indexStep(step.initial)
    indexStep(step.onSuccess)
    indexStep(step.onFailure)
  }

  indexStep(wf.root)
  ;(wf.steps || []).forEach(indexStep)

  // ── Node factory ─────────────────────────────────────────────
  function makeNode(id, step) {
    const type = (step.type || 'TASK').toUpperCase()
    return {
      id,
      type: 'conductorNode',
      position: { x: 0, y: 0 },
      data: {
        type,
        name: step.name || step.message || step.id || id,
        ref:  step.id || id,
        raw:  step,
        color: TYPE_COLOR[type] || '#4f6ef7',
        message: step.message || null,
      },
      draggable: true,
    }
  }

  // ── Edge factory ─────────────────────────────────────────────
  const edgeSet = new Set()
  function addEdge(source, target, label = '') {
    const key = `${source}→${target}:${label}`
    if (edgeSet.has(key)) return
    edgeSet.add(key)
    rfEdges.push({
      id: `e-${source}-${target}-${label}`,
      source, target, label,
      type: 'conductorEdge',
      markerEnd: { type: 'arrowclosed' },
    })
  }

  // ── Recursive step traversal ──────────────────────────────────
  function visitStep(step, parentId) {
    if (!step) return null
    const id = step.id || `__anon_${Math.random().toString(36).slice(2)}`
    if (visited.has(id)) {
      if (parentId) addEdge(parentId, id)
      return id
    }
    visited.add(id)

    rfNodes.push(makeNode(id, step))
    if (parentId) addEdge(parentId, id)

    // ── Nested children ───────────────────────────────────────
    // CONDITIONAL / onSuccess / onFailure
    if (step.initial) {
      visitStep(step.initial, id)
    }
    if (step.onSuccess) {
      const sid = visitStep(step.onSuccess, null)
      if (sid) addEdge(id, sid, 'success')
    }
    if (step.onFailure) {
      const fid = visitStep(step.onFailure, null)
      if (fid) addEdge(id, fid, 'failure')
    }

    // SEQUENTIAL / child steps inline
    if (step.step) {
      visitStep(step.step, id)
    }

    // PARALLEL / nested steps array
    if (step.steps && step.steps.length > 0) {
      step.steps.forEach((child) => visitStep(child, id))
    }

    // nextStepId pointer — look up in stepMap
    if (step.nextStepId) {
      const next = stepMap[step.nextStepId]
      if (next) {
        visitStep(next, id)
      } else {
        // nextStepId not yet in stepMap — will be visited later
        addEdge(id, step.nextStepId)
      }
    }

    return id
  }

  // START sentinel
  rfNodes.push({
    id: '__START__', type: 'conductorNode', position: { x: 0, y: 0 },
    data: { type: 'START', name: 'Start', ref: '', raw: {}, color: TYPE_COLOR.START },
    draggable: true,
  })

  // Visit root first, then any top-level steps not reached via nextStepId
  const rootId = wf.root ? visitStep(wf.root, '__START__') : null

  // Visit any steps[] entries not already visited
  ;(wf.steps || []).forEach((step) => {
    if (step.id && !visited.has(step.id)) {
      visitStep(step, null)
    }
  })

  // END sentinel — connect leaf nodes (nodes with no outgoing edge)
  rfNodes.push({
    id: '__END__', type: 'conductorNode', position: { x: 0, y: 0 },
    data: { type: 'END', name: 'End', ref: '', raw: {}, color: TYPE_COLOR.END },
    draggable: true,
  })

  const sourcesWithEdges = new Set(rfEdges.map((e) => e.source))
  rfNodes.forEach((n) => {
    if (n.id !== '__START__' && n.id !== '__END__' && !sourcesWithEdges.has(n.id)) {
      addEdge(n.id, '__END__')
    }
  })

  if (!rootId && rfNodes.length === 2) {
    addEdge('__START__', '__END__')
  }

  computePositions(rfNodes, rfEdges)
  return { nodes: rfNodes, edges: rfEdges }
}

// ═════════════════════════════════════════════════════════════════
//  FORMAT B: Conductor-style  { tasks: [...] }
// ═════════════════════════════════════════════════════════════════

function parseConductorWorkflow(wf) {
  const tasks = wf.tasks || []
  const nodeMap = {}
  const rfNodes = []
  const rfEdges = []

  const makeNode = (id, type, name, ref, data = {}) => ({
    id, type: 'conductorNode', position: { x: 0, y: 0 },
    data: { type, name, ref, raw: data, color: TYPE_COLOR[type] || '#4f6ef7' },
    draggable: true,
  })

  rfNodes.push(makeNode('__START__', 'START', 'Start', ''))
  nodeMap['__START__'] = true

  const inlineRefs = new Set()
  tasks.forEach((task) => {
    if (task.type === 'FORK_JOIN' && task.forkTasks)
      task.forkTasks.forEach((b) => b.forEach((t) => inlineRefs.add(t.taskReferenceName || t.name)))
    if ((task.type === 'SWITCH' || task.type === 'DECISION') && task.decisionCases)
      Object.values(task.decisionCases).forEach((b) => b.forEach((t) => inlineRefs.add(t.taskReferenceName || t.name)))
  })

  const registerTask = (task) => {
    const id = task.taskReferenceName || task.name
    if (nodeMap[id]) return id
    const n = makeNode(id, (task.type || 'SIMPLE').toUpperCase(), task.name || id, task.taskReferenceName || '', task)
    rfNodes.push(n)
    nodeMap[id] = true
    return id
  }

  tasks.forEach((task) => {
    registerTask(task)
    if (task.type === 'FORK_JOIN' && task.forkTasks)
      task.forkTasks.forEach((b) => b.forEach(registerTask))
    if ((task.type === 'SWITCH' || task.type === 'DECISION') && task.decisionCases)
      Object.values(task.decisionCases).forEach((b) => b.forEach(registerTask))
  })

  rfNodes.push(makeNode('__END__', 'END', 'End', ''))

  const edgeSet = new Set()
  const addEdge = (source, target, label = '') => {
    const key = `${source}→${target}:${label}`
    if (edgeSet.has(key)) return
    edgeSet.add(key)
    rfEdges.push({ id: `e-${source}-${target}-${label}`, source, target, label, type: 'conductorEdge', markerEnd: { type: 'arrowclosed' } })
  }

  const topLevel = tasks.filter((t) => !inlineRefs.has(t.taskReferenceName || t.name))
  if (topLevel.length > 0) addEdge('__START__', topLevel[0].taskReferenceName || topLevel[0].name)
  else addEdge('__START__', '__END__')

  topLevel.forEach((task, i) => {
    const id = task.taskReferenceName || task.name
    const nextId = i + 1 < topLevel.length ? (topLevel[i + 1].taskReferenceName || topLevel[i + 1].name) : '__END__'

    if (task.type === 'FORK_JOIN' && task.forkTasks) {
      task.forkTasks.forEach((branch, bi) => {
        if (!branch.length) return
        const firstId = branch[0].taskReferenceName || branch[0].name
        addEdge(id, firstId, `branch ${bi + 1}`)
        for (let j = 0; j < branch.length - 1; j++)
          addEdge(branch[j].taskReferenceName || branch[j].name, branch[j + 1].taskReferenceName || branch[j + 1].name)
        addEdge(branch[branch.length - 1].taskReferenceName || branch[branch.length - 1].name, nextId)
      })
    } else if ((task.type === 'SWITCH' || task.type === 'DECISION') && task.decisionCases) {
      Object.entries(task.decisionCases).forEach(([caseKey, caseTasks]) => {
        if (!caseTasks.length) return
        const firstId = caseTasks[0].taskReferenceName || caseTasks[0].name
        addEdge(id, firstId, caseKey)
        for (let j = 0; j < caseTasks.length - 1; j++)
          addEdge(caseTasks[j].taskReferenceName || caseTasks[j].name, caseTasks[j + 1].taskReferenceName || caseTasks[j + 1].name)
        addEdge(caseTasks[caseTasks.length - 1].taskReferenceName || caseTasks[caseTasks.length - 1].name, nextId)
      })
    } else {
      addEdge(id, nextId)
    }
  })

  computePositions(rfNodes, rfEdges)
  return { nodes: rfNodes, edges: rfEdges }
}

// ═════════════════════════════════════════════════════════════════
//  LAYOUT — BFS-layered top → bottom
// ═════════════════════════════════════════════════════════════════

function assignLayers(nodeIds, edgeList) {
  const layers = {}
  const visited = new Set()
  const start = nodeIds.find((id) => id === '__START__') || nodeIds[0]
  const queue = [{ id: start, layer: 0 }]

  while (queue.length > 0) {
    const { id, layer } = queue.shift()
    if (visited.has(id)) continue
    visited.add(id)
    layers[id] = Math.max(layers[id] ?? 0, layer)
    edgeList.filter((e) => e.source === id).forEach((e) =>
      queue.push({ id: e.target, layer: layer + 1 })
    )
  }

  nodeIds.forEach((id) => { if (layers[id] === undefined) layers[id] = 1 })
  return layers
}

function computePositions(rfNodes, rfEdges) {
  const LAYER_GAP_Y = 120
  const NODE_GAP_X  = 240
  const CENTER_X    = 380

  const layers = assignLayers(rfNodes.map((n) => n.id), rfEdges)
  const byLayer = {}
  rfNodes.forEach((n) => {
    const l = layers[n.id] ?? 0
    if (!byLayer[l]) byLayer[l] = []
    byLayer[l].push(n)
  })

  Object.keys(byLayer).map(Number).sort((a, b) => a - b).forEach((l) => {
    const group = byLayer[l]
    const totalW = group.length * NODE_W + (group.length - 1) * NODE_GAP_X
    group.forEach((n, i) => {
      n.position = {
        x: CENTER_X - totalW / 2 + i * (NODE_W + NODE_GAP_X),
        y: l * LAYER_GAP_Y + 40,
      }
    })
  })
}
