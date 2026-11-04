---
name: ij-debugger
description: "Debugger-first runtime root-cause analysis for JVM code in IntelliJ IDEA. Use when runtime state or control-flow evidence is needed (values, branches, call order, reachability), when the question spans long call chains across many files, or when the user explicitly requests the debugger. Evidence comes from logpoints/breakpoints, stepping, stacks, runtime values, expression evaluation, and control-flow observations."
allowed-tools: execute_tool
---

# IntelliJ IDEA Debugger Skill

## Invocation Mode (Mandatory)
Choose the invocation mode before the first debugger or run-configuration call:
1. Check whether `execute_tool` is available.
2. If `execute_tool` is available, **always use it as the primary and exclusive path** for every debugger and run-configuration call: `execute_tool(command="<tool> --arg value ...")`. Do not call the underlying handles directly.
3. Only if `execute_tool` is unavailable, **call the corresponding underlying tool handles directly**. This is the required fallback, not a blocker; do not stop, wait for the router, or fall back to workspace files while the required direct handles are available.

Do not mix router and direct-handle calls within one task unless tool availability actually changes. The presence of direct handles never justifies bypassing an available `execute_tool`.

All examples in this skill show the primary router path. In direct-handle mode, call the same named tool with equivalent structured arguments instead of wrapping them in a `command` string.

Router argument rules:
- Every `--param` must be followed by a value; bare flags are not supported.
- Boolean parameters need an explicit `true` or `false`. In `--breakpoints` JSON, use fields such as `"isLogStack":true`.
- **Quote every value that contains a space or an operator — expressions almost always do.** The router splits `command` on whitespace, so an unquoted multi-token value is misread as extra arguments and the call fails with an error like `Invalid argument format: '+'. Expected '--paramName value' format`. Wrap the whole value in single quotes so it stays one token:
  - `--expression 'user.getName().trim()'`
  - If the expression itself contains single quotes, wrap the outer value in double quotes. Do not JSON-encode top-level expression parameters.
- `xdebug_set_breakpoints --breakpoints` is the exception: pass one quoted JSON array whose objects contain the locations, expressions, and settings, for example `--breakpoints '[{"filePath":"src/Foo.java","line":42,"logExpression":"value","suspendPolicy":"NONE"}]'`.
- Omit optional params entirely instead of passing `""`, `"/"`, `"."`, `"__omit__"`, or other sentinels.

## Goal
Use debugger evidence to answer concrete runtime questions when static code reading is not enough, or to confirm ideas from static analysis — including whether execution reaches or does not reach agent-selected code locations. Gather this evidence with **minimal runtime disturbance**: by default observe without stopping threads and without editing source code.

## When To Use This Skill
Use this skill when at least one holds:
- a runtime value or control-flow fact is needed (actual values, branch taken, call order, thread context, or whether execution reaches a chosen location) and it cannot be derived confidently from source/logs;
- finding the relevant path would otherwise require reading many files or tracing a long call chain manually;
- the user explicitly asks to use the debugger (breakpoints, logpoints, stepping, inspect values, debugger session, or attach to a running process).

Manual activation by the user means launch and use the debugger when tools are available — do not reduce it to a conceptual discussion. Do not treat domain identifiers named `debug`/`debugger` in the user's own code as a debugger request; reason about what the user actually means.

## Tool Reference

Read [reference/tools.md](reference/tools.md) before selecting the first debugger or run-configuration tool.
Then read only the selected tool’s page under `reference/tools/` for its exact signature, parameters, outputs, and current constraints. Do
not preload unrelated tool pages.

## Activation Gate (tool availability)
Use this skill only when the minimum tools below are available through the selected invocation mode:
- `xdebug_set_breakpoints` (sets one or more breakpoints/logpoints via a JSON `--breakpoints` array)
- `xdebug_start_debugger_session`
- `xdebug_control_session`
- `xdebug_get_stack`
- at least one value tool: `xdebug_get_frame_values`, `xdebug_get_value_by_path`, or `xdebug_evaluate_expression`
- `get_run_configurations`

Attaching additionally requires `xdebug_attach_to_process`. Its absence blocks only the attach path, not debugger sessions that can be launched normally.

If the minimum set is unavailable: state the blocker explicitly, do not force a debugger workflow, and switch to the best fallback.

## Principle: Logpoint-First Debugging
**Logpoints are the primary way to communicate with the IntelliJ debugger.** A logpoint is a non-suspending breakpoint that does not stop execution: when the line is reached it evaluates an expression and logs the result. Reach for a logpoint first for almost every runtime question; treat suspending breakpoints, stepping, and expression evaluation as escalations you must justify, not as the default. Logpoints give runtime evidence without freezing threads — which matters for concurrent code, long-running services, flaky/timing-sensitive tests, and multi-agent workflows.

**Logpoint output is dumped to debugger events, and you read it back from there.** Each logpoint hit is captured as an event in the debug session's event buffer — not printed to your console and not written to any file. You collect that output by draining events: `execute_tool(command="xdebug_control_session --action DRAIN_EVENTS")` returns the accumulated logpoint output in `tracepointOutputsTail`. So the logpoint loop is: install logpoint(s) → run the scenario → drain events → map each event back to source. Reading events is the normal, expected way to get logpoint results; if you find yourself wanting to write a file or add a print statement to capture output, drain events instead.

Prefer the **least disruptive** tool that answers the question, in this priority order:

1. **Non-suspending logpoints** — `execute_tool(command="xdebug_set_breakpoints --breakpoints '[{\"filePath\":\"<p>\",\"line\":<n>,\"logExpression\":\"<expr>\",\"suspendPolicy\":\"NONE\"}]'")`. A logpoint item has a `logExpression` and `suspendPolicy=NONE`, so it does not stop execution.
   Use for runtime values, branch evidence, counts, identifiers, and path/reachability confirmation. This is the default first probe.
2. **Logpoints with condition / stack-trace logging** — add `condition` and `isLogStack=true` to the relevant JSON item.
   Use when events are too frequent, only specific cases matter, or a call path is needed.
3. **Thread / stack inspection of a suspended session** — `execute_tool(command="xdebug_get_threads")` + `execute_tool(command="xdebug_get_stack ...")`.
   These read a *paused* session: if the program is running, suspend it first with `execute_tool(command="xdebug_control_session --action PAUSE")`, then read threads/stack. Use for hangs, concurrency issues, deadlocks, thread-state questions, or unclear call paths.
4. **Ordinary suspending breakpoints** — use an item with `filePath` + `line` and the default `suspendPolicy=ALL`.
   Use only when you must inspect live object graphs, evaluate multiple expressions interactively, step through code, or modify state.
5. **Evaluate expression / Set value** — `execute_tool(command="xdebug_evaluate_expression ...")`, `execute_tool(command="xdebug_set_variable ...")`.
   Use only in a suspended context and only with explicit justification; these can alter what you are investigating.

Escalate down this list only when the cheaper option cannot answer the question, and say why you escalated.

On a JVM session, steps 1, 2, and 4 also come in a class/method-targeted form — `jvm_debug_set_method_breakpoint` and `jvm_debug_set_exception_breakpoint`. They sit at the same rungs, not below them: with `--logExpression --suspendPolicy NONE` a method-entry logpoint is exactly as cheap as a line logpoint. Choose them when the question is not tied to one source line — see JVM-Specific Breakpoints.

## Safe Logpoint Expressions (no side effects)
Breakpoint-item `logExpression` and `condition` values are evaluated inside the running program and **can change its behavior**. By default restrict them to **side-effect-free reads**: field/local/parameter access, non-mutating getters, arithmetic, `toString()` on trusted values, identity/`hashCode` checks.

Do **not** use expressions that mutate state, perform I/O, advance iterators/streams, trigger side-effecting lazy initialization, or call methods with observable side effects — unless the user explicitly asked to enter a controlled mutation/debugging mode and accepted that program behavior may change.

## Logpoint Placement: Variable Scope And Initialization
A logpoint evaluates its `logExpression` field **at the moment execution reaches the line, before that line's statement runs**. Every symbol the expression references must already be **assigned and in scope** at that point, or evaluation fails (you will see an error such as `Variable 'x' has not been initialized` / `Cannot find local variable 'x'` in `breakpointErrorsTail`, and no value is logged).

Rules for picking the line:
- Do **not** place a logpoint on the line that *declares or initializes* the variable you want to log, nor in the middle of a multi-line statement that assigns it. At that position the variable does not hold its value yet.
- For a multi-line assignment such as a fluent/builder chain
  ```
  List<String> configured = jdbc.sql("…")   // line 20  ← declaration starts here
      .query(String.class)                   // line 21
      .list();                               // line 22  ← assignment completes here
  this.rate = new BigDecimal(configured.get(0)); // line 23
  ```
  a logpoint on line 21 logging `configured.get(0)` fails — `configured` is assigned only after line 22. Place it on the **first line after the statement's terminating `;`** (here line 23), where `configured` is initialized.
- General rule: put a line logpoint on the earliest executable line where every symbol in the expression is already initialized and in scope. To capture a value at a method's entry with a line logpoint, target the first executable line of the body, not the signature/parameter line.
- After installing, run once and read `breakpointErrorsTail`. If you see an uninitialized/unresolved-variable error, move the logpoint to a later line (or pick an expression whose inputs are already available) and rerun.

## JVM-Specific Breakpoints
These tools apply to JVM sessions only. They are targeted by JVM class/method or exception class, not by a source line. A line logpoint is more precise when a particular statement matters; a method breakpoint is the better target when the method boundary itself matters.

**Method entry / exit** — `jvm_debug_set_method_breakpoint --classPattern <pattern> --methodName <pattern>` with `--watchEntry true`, `--watchExit true`, or both. Strongly prefer it over a line breakpoint when the goal is simply to stop or log at method entry: target the class and method directly instead of finding a source line number and checking which line is executable. Emulated method breakpoints target method bytecode entry/exit, so they also work when the class has no line-number debug information (`LineNumberTable`). Use it to capture parameters across many call sites, implementations, or repeated invocations, or when generated/decompiled code has no reliable line for a probe. For low-disturbance parameter collection, add a side-effect-free `--logExpression` and `--suspendPolicy NONE`. Use method exit when completion, final visible state, or the exit call path matters. Keep patterns narrow: a broad pattern produces high event volume and slows the debuggee.

**Exceptions** — `jvm_debug_set_exception_breakpoint --exceptionClass <fully-qualified-name>` when the useful evidence starts where an exception is thrown and the relevant source line is unknown or has many throw sites. Select caught exceptions, uncaught exceptions, or both. Use `java.lang.Throwable` only when broad coverage is intentional, because it fires on every exception, including ones the program handles internally.

**Treat silence as inconclusive.** A wrong `classPattern`/`methodName` or exception class produces no hits, but neither does correctly targeted code that simply did not run. The list tools confirm only the stored target and settings, not whether the target resolved at runtime. If nothing was logged, re-read the configured target with `jvm_debug_list_method_breakpoints` / `jvm_debug_list_exception_breakpoints` to catch obvious mistakes, then gather independent reachability evidence before concluding the code was not reached.

**Inspecting and removing.** Read JVM targets only from `jvm_debug_list_method_breakpoints` / `jvm_debug_list_exception_breakpoints`: `xdebug_list_breakpoints` shows these breakpoints but not their class/method patterns or caught/uncaught selection, and you must never infer those from breakpoint IDs, source code, or creation order. Removal goes through the generic `xdebug_remove_breakpoints` tool. There is no JVM-specific remove tool. The standard `Any Exception` breakpoint is reported with `isDefault=true`; it cannot be deleted, only disabled.

## Initial Triage Before Debugging
Start with minimal static triage (error text, stack trace, nearby source). Start debugger work when: static analysis leaves multiple plausible runtime hypotheses; deciding between them requires concrete values or exact control flow; stepping is cheaper than reading a large cross-file path; or the user explicitly asked. Install the first logpoint(s) early and collect evidence before changing runtime behavior.

## Mandatory Runtime Evidence
Before editing code for a runtime-behavior issue when debugger evidence is available, capture:
- for reachability/branch questions: logpoint output proving execution did or did not hit a chosen location (or its absence across a full reproduction);
- for state questions: at least one concrete runtime value, via a logpoint expression or — in a suspended session — `xdebug_get_frame_values` / `xdebug_get_value_by_path` / `xdebug_evaluate_expression`;
- when you suspend: the paused location (file/line) and the top call path via `xdebug_get_stack`.

Do not assert runtime conclusions before this evidence is captured unless a clear blocker is already stated.

## Hypothesis-Driven Batch Logpoints
Logpoints are cheap and non-suspending, so prefer installing a **batch** derived from your hypotheses, then run the scenario once:
1. Form 2–5 concrete hypotheses about where/why runtime behavior diverges.
2. Install all logpoints in one `xdebug_set_breakpoints` call, one JSON item per hypothesis anchor (branch decision, mapping boundary, state transition, dynamic dispatch, error construction, queue handoff).
3. Run the scenario once via `execute_tool(command="xdebug_start_debugger_session ...")`.
4. Read output with `execute_tool(command="xdebug_control_session --action DRAIN_EVENTS")` and map each line back to its source location and hypothesis.
5. Refine hypotheses or escalate to stronger tools only when logpoint evidence is insufficient.

## Reproduction Mode Selection
1. `AUTO`: you can run/rerun the scenario directly. Use `xdebug_start_debugger_session` to launch with debugging, or `execute_run_configuration` for a non-debug run when you only need output/exit code.
2. `ASSISTED`: reproduction needs a user-only action (UI flow, auth, external dependency, hardware). If the relevant process is already running on the IDE backend's local machine, prepare probes first and use `xdebug_attach_to_process` instead of restarting it.
3. `HYBRID`: try `AUTO` once or twice, then switch to `ASSISTED` if it does not reproduce.

If the candidate target is a test, default to `AUTO`. Never ask the user to reproduce before logpoints/breakpoints are prepared.

## Test Task Execution
- To debug a test: `execute_tool(command="xdebug_start_debugger_session --filePath <p> --line <n>")` pointing at the test method, or `--configurationName <name>` for an existing test run configuration. To run without debugging (e.g. confirm it fails first): `execute_tool(command="execute_run_configuration ...")` with the same targeting modes.
- When the exact test `filePath` and `line` are known, launch it directly; do not call `get_run_configurations` first.
- After triggering, inspect state with `execute_tool(command="xdebug_get_debugger_status")` and continue inside the resulting session.
- Do not ask the user to rerun a test manually unless no available path can reproduce it or repro requires user-only setup.

## Argument Hygiene
- Optional params are omitted or set to a real value; omitted means omitted.
- Never encode omitted params with placeholder strings such as `""`, `"/"`, `"__omit__"`, `"."`, or fake paths.
- Treat debugger IDs and paths as opaque runtime data: copy them from tool outputs, do not synthesize them.
- `xdebug_attach_to_process --debuggerKind` is not an ID: it is a human-readable debugger display name or a unique case-insensitive substring. Omit it when no debugger is preferred. If the result is `selection_required`, choose from `availableDebuggers` and promptly repeat the call with the same PID and the exact returned name as `--debuggerKind`. If a requested kind does not match, choose from the available display names listed by the error and retry with the same PID.

## Session Binding
- Before preparing logpoints/breakpoints or launching, call `execute_tool(command="xdebug_get_debugger_status")`.
- If an active relevant session exists, continue inside it instead of starting another.
- Capture the exact `sessionId` from status/start/attach and reuse it in all session-scoped calls (`--sessionId <id>`).
- If an attach request was submitted but waiting for the session times out, refresh `xdebug_get_debugger_status` before retrying: the attached session may appear after the timeout.
- Keep the returned `sessionId`. Use `xdebug_get_debugger_status --sessionId <id>` after the session stops to read its retained state.
- Active-only tools return `SESSION_NOT_ACTIVE` for a retained stopped session. `SESSION_EXPIRED` means that its diagnostics left the five-session retention window.
- After the paused location changes, re-read `frameIndex`/`path` from a fresh `xdebug_get_stack`.

## Run Configuration Resolution
- Resolve `configurationName` only from `execute_tool(command="get_run_configurations")`; do not pass a test method name or other derived identifier.
- If you can process tool results outside model context, filter the `get_run_configurations` response there. Keep only exact matches or at most five candidates, unless the user requests the full list.
- Use `supportsDynamicLaunchOverrides` from `get_run_configurations` as the source of truth for `--programArguments`, `--workingDirectory`, and `--envs`.
- Use launch overrides only when they materially improve reproducibility/observability and the run config supports them.

## Breakpoint And Logpoint Ownership And Hygiene
- Start with `execute_tool(command="xdebug_list_breakpoints")` and treat returned `owner` as source of truth (`user`/`agent`). Logpoints appear here too.
- Snapshot the **full** baseline of each breakpoint you may touch — `enabled`, `logExpression`, `condition`, `isLogMessage`, `isLogStack`, `suspendPolicy`, and `temporary` (all returned by `xdebug_list_breakpoints`). You need this because every update rewrites the whole breakpoint (see Targeting Modes).
- For JVM method/exception breakpoints the baseline also includes `watchEntry`/`watchExit` or `notifyCaught`/`notifyUncaught`, which only `jvm_debug_list_method_breakpoints` / `jvm_debug_list_exception_breakpoints` return. Snapshot from there before calling `jvm_debug_set_*` on a target that may already exist.
- `owner` follows whoever *created* the breakpoint. Rewriting a `user` breakpoint does not make it yours: the tool applies your settings, keeps `owner=user`, and says so in `message`. Restore the baseline yourself when you are done with it — agent-scoped cleanup will not.
- Avoid broad breakpoint churn between iterations — change the minimum set needed for the next probe.
- Each `xdebug_remove_breakpoints --breakpoints` item defaults to `owner=agent`. For global cleanup, use one batch with `[{"owner":"agent"},{"owner":"user"}]`.

## Breakpoint And Logpoint Targeting Modes
Each item in `xdebug_set_breakpoints --breakpoints` has two mutually exclusive modes — never mix them in one item:
- **Location mode**: provide `filePath` + `line` (1-based); omit `breakpointId`.
- **`breakpointId` mode**: provide `breakpointId` (an opaque canonical id from a prior `xdebug_set_breakpoints`/`xdebug_list_breakpoints`).

`breakpointId` mode rewrites the whole breakpoint, it does not patch it. Treat an update as "write the full breakpoint", never "tweak one flag".

`jvm_debug_set_method_breakpoint` and `jvm_debug_set_exception_breakpoint` add a third mode — **target mode**. They are keyed by `--classPattern`+`--methodName` or `--exceptionClass`, so a second call with the same target does not create a second breakpoint: it rewrites the existing one, whoever created it. Check `created` in the response to see which happened, and re-pass the breakpoint's complete state on every call.

After each `xdebug_set_breakpoints` call, inspect every successful result's `lineText`. Failed items have `success=false` and an `error`. JVM target-based calls do not return `lineText`; inspect the returned breakpoint's target and complete state instead. A successful response does not prove `condition` or `logExpression` is valid. Verify the event tails after the next run.

## Core Workflow
1. Scope the failure: capture exact error text and expected vs actual; identify 2–5 candidate anchors and the value at each that would confirm/refute a hypothesis.
2. Prepare: `execute_tool(command="xdebug_get_debugger_status")` → reuse a relevant session or plan a new one; pick `AUTO`/`ASSISTED`/`HYBRID`; install logpoints first, verifying `lineText` for line logpoints or the returned target and state for JVM target-based logpoints.
3. Run: reuse an active session, start one with `execute_tool(command="xdebug_start_debugger_session ...")`, or attach to an already-running local process with `execute_tool(command="xdebug_attach_to_process --pid <n> ...")`. On `selection_required`, choose an exact `availableDebuggers` name and repeat with the same PID plus `--debuggerKind '<name>'`; on `attached`, bind the returned `sessionId` and let the scenario run (logpoints do not suspend).
4. Collect: `execute_tool(command="xdebug_control_session --action DRAIN_EVENTS")`; map each output line to source + hypothesis.
5. Escalate only if needed: if a logpoint cannot capture the needed object graph or interactive evidence, add a suspending breakpoint at the proven-relevant line, then `--action WAIT_FOR_PAUSE` and inspect stack + values.
6. If the session is paused, use `--action RESUME --timeout 0` to finish an inspection or let logpoints run.
   Leave a running session running.
   To continue to an expected suspending breakpoint, use `--action RESUME` and inspect its returned state.
   Call `WAIT_FOR_PAUSE` only if the session is still running and that expected stop remains relevant.
7. On wait timeout: `--action PAUSE`, re-check enabled breakpoints and the expected path; after 2 consecutive timeouts on the same wait, stop retrying and expand logpoint coverage.
8. Read stdout/stderr only when available state, debugger events, or structured results do not answer the current question. Follow [Process Output](#process-output).
9. Continue until the first incorrect state transition is proven or you have a clearly stated blocker; then summarize root cause with concrete evidence.

## Process Output

- Prefer an accessible `fullOutputPath` or output resource; otherwise use `xdebug_get_process_output` for relevant ranges.
- When processing outside model context is available and useful, filter or parse output there. Otherwise read small excerpts and expand as needed.
- Do not print both the text and structured copies of the same MCP output. Reuse previously retrieved output when available.
- Preserve range and completeness metadata. Process termination does not mean that all output has been read.

## Default First Probe
For a reproducible runtime issue:
1. `execute_tool(command="xdebug_get_debugger_status")`
2. Reuse an active relevant session, otherwise install the hypothesis batch with `execute_tool(command="xdebug_set_breakpoints --breakpoints '<json-array>'")`
3. If no relevant session exists, start it with `execute_tool(command="xdebug_start_debugger_session ...")`; if the target process is already running locally and must not be restarted, attach with `execute_tool(command="xdebug_attach_to_process --pid <n> ...")`, resolving `selection_required` with a second call as described below
4. `execute_tool(command="xdebug_control_session --action DRAIN_EVENTS")` to read logpoint output
5. Map output to source; escalate to a suspending breakpoint + `xdebug_get_stack` / `xdebug_get_frame_values` only if logpoints are insufficient

## Events, Logpoints, And Tracepoints
Logpoint and tracepoint output is buffered as **debugger events** on the session; you consume it by draining, not by reading a console or file.

- **Read logpoint output:** `execute_tool(command="xdebug_control_session --action DRAIN_EVENTS")` returns buffered output in `tracepointOutputsTail` for `xdebug_set_breakpoints` logpoint/tracepoint items.
- **Each event maps back to source.** A drained event carries the evaluated `message`, plus `breakpointId`, `filePath`, `line`, and a timestamp — use these to attribute every line to the logpoint and hypothesis that produced it. Distinguish logpoints by their location/expression so concurrent or interleaved hits stay separable.
- **Drain is consuming:** events are removed from the buffer when drained, so each call returns only output produced since the last drain. Drain after the scenario finishes; for long-running or high-volume scenarios, drain periodically during the run so the bounded buffer does not overflow and drop the oldest events. Use `--eventsLimit <n>` to cap how many are returned per call.
- **Errors come back as events too:** every `xdebug_control_session` response carries `breakpointErrorsTail` with breakpoint/logpoint validation or runtime errors (e.g. a bad `logExpression` field). After installing any logpoint/condition/tracepoint, do not trust it until you have drained and read both tails.
- Event tails are currently populated by JVM-based debuggers (Java, Kotlin, etc.).

## Expression Discipline
- Breakpoint-item `logExpression` and `condition` run in the program; keep them side-effect-free (see Safe Logpoint Expressions).
- `--expression` (`xdebug_evaluate_expression`) runs in the current paused frame; pass raw expression text in that frame's language.
- `--newValue` (`xdebug_set_variable`) must be a raw expression assignable to the target value.
- Expression values nearly always contain spaces or operators (`+`, `==`, `&&`, method args), so they MUST be quoted as a single token (see Router argument rules). An unquoted expression fails with `Invalid argument format: '<token>'`. Do not pass JSON-escaped or backslash-escaped quoted text into expression params. The router parses the outer `command` with a shell-like splitter: when the expression contains single quotes wrap the outer string in double quotes, and vice versa.
- Prefer fully-qualified names in `condition` and `logExpression` fields, and in `--expression`, to avoid unresolved-reference errors from missing imports.
- If paused, preflight a risky condition/log expression with `xdebug_evaluate_expression` before relying on it.

## Anti-Patterns
- adding temporary `print`/log statements to source (or trying to capture logpoint output to a file) instead of installing a logpoint and draining its events;
- concluding from a logpoint without draining events (`DRAIN_EVENTS`), or assuming a re-drain will return output already consumed by an earlier drain;
- defaulting to suspending breakpoints when a non-suspending logpoint would answer the question;
- using side-effecting log/condition expressions without explicit user consent;
- placing a logpoint on the line that initializes the variable it logs (or inside a multi-line assignment), so the variable is not yet initialized when the expression runs;
- claiming root cause without runtime values;
- calling debugger handles directly while `execute_tool` is available;
- encoding omitted params with placeholder strings;
- treating `debuggerKind` as an opaque ID or requiring an exact display name instead of using its case-insensitive substring semantics;
- treating `selection_required` as an attached session instead of repeating the call with the same PID and an exact returned debugger name;
- immediately retrying `xdebug_attach_to_process` after a session-wait timeout without first refreshing `xdebug_get_debugger_status`;
- reusing stale `sessionId`/`frameIndex`/`path` after the paused location changes;
- mixing location mode and `breakpointId` mode in one batch item;
- updating a breakpoint by id without re-passing its full state, clobbering a user's `logExpression`/`condition` or resetting its log/suspend flags;
- stopping at a downstream symptom without tracing producing state;
- asking the user to reproduce before probes are prepared;
- prolonged static-only analysis when one logpoint probe can disambiguate;
- ignoring library frames instead of reading their decompiled source.

## Wrap-up
After debugging:
1. Clean up: `execute_tool(command="xdebug_remove_breakpoints --breakpoints '[{\"owner\":\"agent\"}]'")`, restore disabled user breakpoints to baseline, and `execute_tool(command="xdebug_control_session --action STOP")` if the session is no longer needed.
2. Report: root cause in one sentence; causal chain in 3–6 bullets; runtime evidence with exact observed values; code references (absolute path + line); if you fell back instead of debugging, the exact reason and what evidence is still missing.
3. Next action: if the user asked for diagnosis only, conclude with the report and a recommended fix; if the task implies a code change, implement the fix based on the proven root cause and collected evidence.
