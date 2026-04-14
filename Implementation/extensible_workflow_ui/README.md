# Conductor UI

A Netflix Conductor-style workflow editor — JSON editor + live ReactFlow graph renderer.

## Quick Start

```bash
npm install
npm run dev
```

Open http://localhost:3000

## Build for Production

```bash
npm run build
# Output goes to /dist — serve it from your backend
```

## Integrating into a Backend Project

### Option A — Serve the built `/dist` as static files

```bash
npm run build
# Copy /dist into your backend's public/static folder
```

Express example:
```js
app.use('/conductor', express.static(path.join(__dirname, 'dist')))
```

### Option B — Use as a component inside an existing React app

Copy the `src/` folder into your project and install the peer dependency:

```bash
npm install reactflow
```

Then import:
```jsx
import ConductorUI from './components/ConductorUI/ConductorUI'

function App() {
  return <ConductorUI />
}
```

### Option C — Pass workflow JSON via props (API-driven)

Modify `ConductorUI.jsx` to accept an optional `initialWorkflow` prop:

```jsx
// In ConductorUI.jsx, add to ConductorUIInner:
export default function ConductorUI({ initialWorkflow }) {
  // On mount, if initialWorkflow is provided, render it
  useEffect(() => {
    if (initialWorkflow) {
      const str = JSON.stringify(initialWorkflow, null, 2)
      setJson(str)
      parseAndRender(str)
    }
  }, [initialWorkflow])
  // ...
}

// Usage:
<ConductorUI initialWorkflow={fetchedFromAPI} />
```

## Project Structure

```
src/
├── main.jsx                          # React entry point
├── App.jsx                           # Root component
├── index.css                         # Global CSS variables + resets
├── utils/
│   ├── parseWorkflow.js              # JSON → ReactFlow nodes/edges
│   └── exampleWorkflow.js            # Sample workflow definition
└── components/
    └── ConductorUI/
        ├── ConductorUI.jsx           # Main orchestrator
        ├── ConductorUI.module.css
        ├── TopBar.jsx                # Header bar
        ├── TopBar.module.css
        ├── JsonEditor.jsx            # Left panel textarea
        ├── JsonEditor.module.css
        ├── ConductorNode.jsx         # Custom ReactFlow node
        ├── ConductorNode.module.css
        ├── ConductorEdge.jsx         # Custom ReactFlow edge
        ├── Inspector.jsx             # Click-to-inspect panel
        └── Inspector.module.css
```

## Supported Task Types

| Type | Color |
|---|---|
| START / END | Green / Red |
| SIMPLE, HTTP, LAMBDA, EVENT, WAIT, INLINE | Purple |
| FORK_JOIN, FORK_JOIN_DYNAMIC, JOIN | Yellow |
| SWITCH, DECISION, EXCLUSIVE_JOIN | Orange |
| DYNAMIC | Blue |
| SUB_WORKFLOW | Lavender |

## Features

- **Live auto-render** — parses JSON 700ms after you stop typing
- **Ctrl+Enter** — force render immediately
- **Drag nodes** — reposition tasks freely on the canvas
- **Click node** — opens inspector panel with task details
- **Fit View** — auto-centers and scales the graph
- **MiniMap** — bottom-right overview
- **Pan & Zoom** — scroll to zoom, drag to pan
- **Fork/Join routing** — branch edges with labels
- **Switch/Decision routing** — case edges with labels
