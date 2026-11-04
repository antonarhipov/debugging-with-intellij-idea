# jvm_debug_list_exception_breakpoints
The result includes the exception class, caught/uncaught selection, default-breakpoint flag, stable ID, owner,<br/>enabled state, condition, logging settings, and suspend policy. The standard `Any Exception` breakpoint has no<br/>`exceptionClass` field and has `isDefault=true`.

## Parameters
| Name | Type | Description |
| --- | --- | --- |
| projectPath | string | The project path. Pass this value ALWAYS if you are aware of it. It reduces numbers of ambiguous calls. <br/>In the case you know only the current working directory you can use it as the project path.<br/>If you're not aware about the project path you can ask user about it. |

## Output
| Name | Type | Description |
| --- | --- | --- |
| breakpoints* | array[object] | All JVM exception breakpoints, including the standard `Any exception` one. |
| &nbsp;&nbsp;[].id* | string | Canonical breakpoint ID (stable across list/set/remove). |
| &nbsp;&nbsp;[].owner* | user \\| agent | Breakpoint ownership marker: `agent` if created by an MCP toolset, otherwise `user`. |
| &nbsp;&nbsp;[].exceptionClass | string? | Fully qualified exception class the breakpoint targets. Absent for the standard `Any exception` breakpoint. |
| &nbsp;&nbsp;[].isDefault* | boolean | Whether this is the standard `Any exception` breakpoint, which cannot be removed and can only be disabled. |
| &nbsp;&nbsp;[].notifyCaught* | boolean | Whether the breakpoint triggers on caught exceptions. |
| &nbsp;&nbsp;[].notifyUncaught* | boolean | Whether the breakpoint triggers on uncaught exceptions. |
| &nbsp;&nbsp;[].enabled* | boolean | Whether the breakpoint is enabled. |
| &nbsp;&nbsp;[].condition | string? | Conditional expression for triggering the breakpoint, if set. |
| &nbsp;&nbsp;[].logExpression | string? | Evaluate-and-log expression of the logpoint, if set (the value logged on every hit). |
| &nbsp;&nbsp;[].isLogMessage* | boolean | Whether the breakpoint logs source position when hit. |
| &nbsp;&nbsp;[].isLogStack* | boolean | Whether the breakpoint logs stack trace when hit. |
| &nbsp;&nbsp;[].suspendPolicy* | string | Breakpoint suspend policy (all/thread/none). |
| totalCount* | integer | Number of returned breakpoints. |
| enabledCount* | integer | Number of returned breakpoints that are enabled. |

