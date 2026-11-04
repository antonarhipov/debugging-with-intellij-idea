# jvm_debug_set_exception_breakpoint
Use it to stop or log when a particular exception class is thrown, including its subclasses.<br/><br/>The exception class must be a fully qualified JVM class name. Use `java.lang.Throwable` to observe all exceptions.<br/>At least one of `notifyCaught` or `notifyUncaught` must be true. Both default to true.<br/><br/>The breakpoint is addressed by its exception class, not by ID: calling the tool again with the same class rewrites<br/>that breakpoint completely, so re-pass its full state instead of a single flag. When the class resolves to a<br/>breakpoint the user placed by hand, the rewrite still happens but ownership stays `user`, and `message` says so.<br/>Remove the breakpoint with `xdebug_remove_breakpoints`; there is no JVM-specific remove tool.

## Parameters
| Name | Type | Description |
| --- | --- | --- |
| exceptionClass* | string | Fully qualified JVM exception class, for example `java.lang.IllegalStateException` or `java.lang.Throwable`. |
| notifyCaught | boolean | Whether to break on caught exceptions. Default: true. |
| notifyUncaught | boolean | Whether to break on uncaught exceptions. Default: true. |
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
| &nbsp;&nbsp;exceptionClass | string? | Fully qualified exception class the breakpoint targets. Absent for the standard `Any exception` breakpoint. |
| &nbsp;&nbsp;isDefault* | boolean | Whether this is the standard `Any exception` breakpoint, which cannot be removed and can only be disabled. |
| &nbsp;&nbsp;notifyCaught* | boolean | Whether the breakpoint triggers on caught exceptions. |
| &nbsp;&nbsp;notifyUncaught* | boolean | Whether the breakpoint triggers on uncaught exceptions. |
| &nbsp;&nbsp;enabled* | boolean | Whether the breakpoint is enabled. |
| &nbsp;&nbsp;condition | string? | Conditional expression for triggering the breakpoint, if set. |
| &nbsp;&nbsp;logExpression | string? | Evaluate-and-log expression of the logpoint, if set (the value logged on every hit). |
| &nbsp;&nbsp;isLogMessage* | boolean | Whether the breakpoint logs source position when hit. |
| &nbsp;&nbsp;isLogStack* | boolean | Whether the breakpoint logs stack trace when hit. |
| &nbsp;&nbsp;suspendPolicy* | string | Breakpoint suspend policy (all/thread/none). |
| totalBreakpoints* | integer | Current total number of breakpoints in the project after the call. |
| message | string? | Warnings about the applied settings, if any. |

