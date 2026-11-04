# jvm_debug_get_thread_dump
The debuggee may be running or suspended. Collection temporarily suspends JVM threads when necessary and restores<br/>their suspend state afterward. The full UTF-8 dumps are written to temporary files to keep the MCP response bounded.<br/>A short preview and metadata are returned inline for each dump.<br/><br/>Set `count` to capture a series of dumps. The first dump is captured immediately. Later dumps are started at<br/>`interval` millisecond intervals measured from the start of the call. If a dump takes longer than the interval, the<br/>next dump starts as soon as the previous capture finishes. Failed attempts are reported alongside successful dumps,<br/>and the call fails only when no dump was captured successfully.<br/><br/>When `includeExtended` is true, each dump also requests registered extended providers, including virtual threads and<br/>Kotlin coroutines when their debugger integrations are available. Set it to false for platform JVM threads only.

## Parameters
| Name | Type | Description |
| --- | --- | --- |
| sessionId | string | Debug session ID. If omitted, the only active session is selected; the call fails when multiple sessions are active. |
| includeExtended | boolean | Whether to include virtual threads and other registered extended thread-dump providers. Default: true. |
| count | integer | Number of thread dumps to capture. Must be between 1 and 10. Default: 1. |
| interval | integer | Interval in milliseconds between the scheduled start of consecutive dumps. The first dump is immediate. count * interval must not exceed 300000 (5 minutes). Default: 1000. |
| timeout | integer | Timeout in milliseconds for collecting each dump. Default: 60000. |
| projectPath | string | The project path. Pass this value ALWAYS if you are aware of it. It reduces numbers of ambiguous calls. <br/>In the case you know only the current working directory you can use it as the project path.<br/>If you're not aware about the project path you can ask user about it. |

## Output
| Name | Type | Description |
| --- | --- | --- |
| sessionId* | string |  |
| intervalMilliseconds* | integer |  |
| includedExtended* | boolean |  |
| dumps* | array[object] |  |
| &nbsp;&nbsp;[].sequenceNumber* | integer |  |
| &nbsp;&nbsp;[].captureStartedAt* | string |  |
| &nbsp;&nbsp;[].platformThreadCount* | integer |  |
| &nbsp;&nbsp;[].dumpItemCount* | integer |  |
| &nbsp;&nbsp;[].byteCount* | integer |  |
| &nbsp;&nbsp;[].fullOutputPath* | string |  |
| &nbsp;&nbsp;[].preview* | string |  |
| &nbsp;&nbsp;[].previewTruncated* | boolean |  |
| failures* | array[object] |  |
| &nbsp;&nbsp;[].sequenceNumber* | integer |  |
| &nbsp;&nbsp;[].captureStartedAt* | string |  |
| &nbsp;&nbsp;[].timedOut* | boolean |  |
| &nbsp;&nbsp;[].error* | string |  |

