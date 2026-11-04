# jvm_debug_set_method_breakpoint
Use it to observe method entry, method exit, or both without choosing a source line.<br/><br/>Target the method with `classPattern` and `methodName`. At least one of `watchEntry` or `watchExit` must be true;<br/>entry defaults to true and exit defaults to false.<br/>For low-disturbance parameter capture, provide a side-effect-free `logExpression` and use `suspendPolicy=NONE`.<br/><br/>The breakpoint is addressed by its class and method pattern, not by ID: calling the tool again with the same<br/>target rewrites that breakpoint completely, so re-pass its full state instead of a single flag. When the target<br/>resolves to a breakpoint the user placed by hand, the rewrite still happens but ownership stays `user`, and<br/>`message` says so. Remove the breakpoint with `xdebug_remove_breakpoints`; there is no JVM-specific remove tool.<br/><br/>A wrong pattern produces silence rather than an error, so confirm the breakpoint is hit and re-check the pattern<br/>with `jvm_debug_list_method_breakpoints` when nothing is logged.

## Parameters
| Name | Type | Description |
| --- | --- | --- |
| classPattern* | string | JVM class pattern, for example `com.example.Service` or `com.example.*`. |
| methodName* | string | JVM method name pattern, for example `process` or `get*`. |
| watchEntry | boolean | Whether to watch method entry. Default: true. |
| watchExit | boolean | Whether to watch method exit. Default: false. |
| condition | string | Optional condition expression. Default: null. |
| logExpression | string | Optional side-effect-free Evaluate-and-log expression. Default: null. |
| isLogMessage | boolean | Whether to log the breakpoint position. Default: false. |
| isLogStack | boolean | Whether to log the current stack. Default: false. |
| suspendPolicy | ALL \\| THREAD \\| NONE | Suspend policy: ALL, THREAD, NONE. Default: ALL. |
| enabled | boolean | Whether the breakpoint is enabled. Default: true. |
| projectPath | string | The project path. Pass this value ALWAYS if you are aware of it. It reduces numbers of ambiguous calls. <br/>In the case you know only the current working directory you can use it as the project path.<br/>If you're not aware about the project path you can ask user about it. |

## Output
| Name | Type | Description |
| --- | --- | --- |
| breakpointId* | string | Canonical breakpoint ID, accepted by `xdebug_remove_breakpoints`. |
| created* | boolean | Whether this call created the breakpoint; false when an existing one was rewritten. |
| breakpoint* | object | State of the breakpoint after the call. |
| &nbsp;&nbsp;id* | string | Canonical breakpoint ID (stable across list/set/remove). |
| &nbsp;&nbsp;owner* | user \\| agent | Breakpoint ownership marker: `agent` if created by an MCP toolset, otherwise `user`. |
| &nbsp;&nbsp;classPattern* | string | JVM class pattern the breakpoint targets, wildcards included. |
| &nbsp;&nbsp;methodName* | string | JVM method name pattern the breakpoint targets, wildcards included. |
| &nbsp;&nbsp;watchEntry* | boolean | Whether method entry is watched. |
| &nbsp;&nbsp;watchExit* | boolean | Whether method exit is watched. |
| &nbsp;&nbsp;emulated* | boolean | Whether the breakpoint is emulated with line breakpoints at method entry/exit instead of much slower JVM method events. |
| &nbsp;&nbsp;enabled* | boolean | Whether the breakpoint is enabled. |
| &nbsp;&nbsp;condition | string? | Conditional expression for triggering the breakpoint, if set. |
| &nbsp;&nbsp;logExpression | string? | Evaluate-and-log expression of the logpoint, if set (the value logged on every hit). |
| &nbsp;&nbsp;isLogMessage* | boolean | Whether the breakpoint logs source position when hit. |
| &nbsp;&nbsp;isLogStack* | boolean | Whether the breakpoint logs stack trace when hit. |
| &nbsp;&nbsp;suspendPolicy* | string | Breakpoint suspend policy (all/thread/none). |
| totalBreakpoints* | integer | Current total number of breakpoints in the project after the call. |
| message | string? | Warnings about the applied settings, if any. |

