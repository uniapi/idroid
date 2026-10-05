# 🏆 Master Challenge: Multi-Stage Task Provisioning Wizard

## 🎯 The Objective
Your goal is to build an architectural **Multi-Stage Task Creation Wizard** entirely in pure Kotlin code (zero XML, zero AndroidX). 

This challenge tests the absolute limits of your Intent routing and Lifecycle coordination knowledge.
You must design a linear navigation workflow across three distinct windows: **`StageHomeActivity`** (The Hub) -> **`StageOneActivity`** (Define Meta) -> **`StageTwoActivity`** (Assign Resources).
Data must be safely accumulated through `Parcelable` boundaries across the chain, and upon completion at the final stage, the entire backstack must collapse instantly,
returning the complete configured payload to the home screen without leaking obsolete window layers.

---

## 🏗️ State Architecture & Workflow Matrix

Your state engine must manage a pipeline configuration sequence without storing temporary properties inside static fields or global singletons (which break during system memory reclaims):

```text
 [ StageHomeActivity ] ──(startActivityForResult)──► [ StageOneActivity ]
          ▲                                                   │
          │                                           (Passes Part 1 Payload)
    (Receives Final                                           │
    Parcelable Result)                                        ▼
          │                                          [ StageTwoActivity ]
          └───────────(Collapses Backstack)───────────────────┘
```

---

## 📐 Step-by-Step Architectural Blueprint

To guarantee implementation success without getting stuck, follow this strict, production-grade development sequence:

### Step 1: Design the Comprehensive Polymorphic Payload Structure
Create a `TaskPayload` object that implements `Parcelable`. It must safely encapsulate public, non-sensitive task characteristics:
*   `taskTitle: String` (Gathered in Stage 1)
*   `estimatedHours: Int` (Gathered in Stage 1)
*   `allocationDepartment: String` (Gathered in Stage 2)
*   `isUrgentPriority: Boolean` (Gathered in Stage 2)

*Tip: Write a robust manual `Parcelable` implementation supporting clean iterative reading and writing sequences.*

### Step 2: Establish the Parent Anchor Screen (`StageHomeActivity`)
1.  **Layout Construction**: Construct a vertical layout showcasing:
    *   `tvDashboard`: A `TextView` (TextSize `16sp`, monospace typeface, initial state: `"NO ACTIVE TASK DISPATCHED"`).
    *   `btnCreateTask`: A `Button` (Text: `"Initialize New Task Provisioning"`).
2.  **Navigation Interception**: 
    *   Bind `btnCreateTask` to launch `StageOneActivity` expecting a result code `500`.
    *   Override `onActivityResult()`. If the result code matches, extract the final `TaskPayload` and print a formatted summary onto `tvDashboard`.

### Step 3: Engineer the Progressive Aggregator Screen (`StageOneActivity`)
1.  **Layout Construction**: Set up a programmatic layout showing text summary placeholders and two interactive triggers:
    *   `btnSetMetaA`: A `Button` (Text: `"Option A: Deploy Server Patch (4 Hours)"`).
    *   `btnSetMetaB`: A `Button` (Text: `"Option B: Database Audit (12 Hours)"`).
2.  **State Logic**: Maintain a local reference to `var partialPayload = TaskPayload("", 0, "", false)`. Clicking option A or B mutates the title and hour integers respectively.
3.  **Forward Routing**: Add a third button, `"Next Stage ->"`, that launches `StageTwoActivity` via `startActivityForResult(..., 501)`.
You **must** pass the current `partialPayload` object forward inside this intent bundle.
4.  **Cascade Resolution**: Override `onActivityResult()` inside this screen. If `StageTwoActivity` returns a success code, it means the workflow is complete!
Immediately take that returned data payload intent, pipe it into your own `setResult(Activity.RESULT_OK, data)`, call `finish()`, and dissolve this screen.

### Step 4: Fabricate the Finalization Screen (`StageTwoActivity`)
1.  **Layout Unpacking**: Inside `onCreate()`, unpack the incoming `TaskPayload` object containing the Stage 1 choices.
2.  **Layout Construction**: Provide buttons to finalize variables:
    *   `btnSelectOps`: A `Button` (Text: `"Assign to Ops Department"`).
    *   `btnMarkUrgent`: A `Button` (Text: `"Toggle Urgent Priority Flag"`).
    *   `btnSubmitFinal`: A `Button` (Text: `"Commit Task and Disband Wizard"`, highlighted background).
3.  **The Termination Engine**: Tapping `btnSubmitFinal` completes the object mutation. Pack the finalized `TaskPayload` into a result intent,
invoke `setResult(Activity.RESULT_OK, returnIntent)`, and call `finish()`. This returns the data back to Stage 1, which instantly forwards it to the Home screen as configured in Step 3.

### Step 5: Enforce Device Rotation Safeguards
Every single screen in this wizard must remain impervious to sudden orientation flips.
Implement `onSaveInstanceState()` and `savedInstanceState` retrieval passes across `StageHomeActivity`, `StageOneActivity`, and `StageTwoActivity`
to protect the ongoing `TaskPayload` data state from resetting to defaults.

---

## 🧪 Verification Matrix & Edge-Case Evaluation

To pass this final master-level challenge successfully, verify your multi-activity pipeline survives these stress-testing vectors:

| Execution Vector | Expected System State Machine Output | Core Structural Guardrail |
| :--- | :--- | :--- |
| **Backstack Dissolution Pass** | Clicking "Commit Task" closes Stage 2 and Stage 1 instantly, popping the user directly back to the Home screen with the combined data array. | Cascading `onActivityResult` completion routines execute without visual freezes. |
| **Mid-Wizard Rotation Test** | Select "Option A" in Stage 1, click next to enter Stage 2. Rotate the device simulator twice. The underlying Stage 1 data must remain completely intact inside Stage 2's memory buffer. | `onSaveInstanceState` serialization layer preserves transient variables over state destruction thresholds. |
| **No Memory Leak Footprints** | Check your system logs during wizard finalization. `StageOneActivity` and `StageTwoActivity` must execute their `onDestroy()` blocks completely. | App drops layout view object tree allocations cleanly when screens finish. |

---
