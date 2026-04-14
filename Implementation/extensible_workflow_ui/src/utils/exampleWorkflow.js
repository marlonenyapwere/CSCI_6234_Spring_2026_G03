// ── Format A: flows.definition.Workflow (new format) ─────────────
export const EXAMPLE_FLOWS_WORKFLOW = {
  id: "approvalFlow",
  name: "Approval Flow",
  root: {
    id: "step1",
    type: "TASK",
    nextStepId: "step2",
    message: "Start workflow"
  },
  steps: [
    {
      id: "step2",
      type: "USER_TASK",
      nextStepId: "step3",
      message: "Approve request?"
    },
    {
      id: "step3",
      type: "TASK",
      message: "Approved"
    }
  ]
}

export const EXAMPLE_FLOWS_CONDITIONAL = {
  id: "loanFlow",
  name: "Loan Approval Flow",
  root: {
    id: "assess",
    type: "TASK",
    message: "Assess application",
    onSuccess: {
      id: "approve",
      type: "TASK",
      message: "Approve loan"
    },
    onFailure: {
      id: "reject",
      type: "TASK",
      message: "Reject loan"
    }
  },
  steps: []
}

// ── Format B: Conductor-style (original format) ───────────────────
export const EXAMPLE_CONDUCTOR_WORKFLOW = {
  name: "order_fulfillment",
  version: 1,
  tasks: [
    { name: "validate_order",  taskReferenceName: "validate_ref",  type: "SIMPLE" },
    { name: "process_payment", taskReferenceName: "payment_ref",   type: "SIMPLE" },
    {
      name: "shipping_gate",
      taskReferenceName: "ship_gate",
      type: "SWITCH",
      decisionCases: {
        EXPRESS:  [{ name: "express_ship",   taskReferenceName: "express_ref",   type: "SIMPLE" }],
        STANDARD: [{ name: "standard_ship",  taskReferenceName: "standard_ref",  type: "SIMPLE" }],
      }
    },
    { name: "notify_customer", taskReferenceName: "notify_ref",   type: "HTTP" },
  ]
}

// Default example shown on first load
export const EXAMPLE_WORKFLOW = EXAMPLE_FLOWS_WORKFLOW
