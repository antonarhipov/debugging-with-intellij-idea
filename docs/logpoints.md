# Find the breakpoint with logpoints

Run `org.example.OrderFulfillment.main()` in IntelliJ IDEA, or run the standalone example from the project root:

```shell
java src/main/java/org/example/OrderFulfillment.java
```

The batch exits normally:

```text
Expected orders:   [ORD-1001, ORD-1002]
Dispatched orders: [ORD-1001]
```

The batch contains just two purchases and one replay:

| Entry | Order | Purchase | Expected behavior |
| --- | --- | --- | --- |
| 1 | `ORD-1001` | One keyboard for `C-01` | Ship |
| 2 | `ORD-1001` | Replay of the first order | Ignore |
| 3 | `ORD-1002` | Two more keyboards for `C-01` | Ship |

Both purchases are paid, have positive quantities, go to the supported country `NL`, and fit within the stock of 20 keyboards. **ORD-1002 should ship, but is missing from the result.** Each distinct order should ship once; another purchase from the same customer is allowed.

There is no exception or stack trace to start from. A breakpoint in `dispatch()` never sees the missing order. A breakpoint at the start of the batch still leaves several possible rejecting rules to investigate. The rules share one invocation site, so first learn which rule rejects this order.

## 1. Observe before suspending

A natural first move is to add a temporary print immediately before `if (!accepted)` in `isReadyToShip()`:

```java
System.out.println(order.id() + " | " + rule.name() + " | accepted=" + accepted);
```

Use the same expression as a logpoint instead. Right-click the breakpoint on the `if (!accepted)` line, clear **Suspend**, and enable **Evaluate and log** with:

```java
order.id() + " | " + rule.name() + " | accepted=" + accepted
```

Debug `main()` again. Keep the logpoint unfiltered initially to compare the first purchase, its replay, and the second purchase. The output shows validation, payment, stock, and dispatch-history decisions, including:

```text
ORD-1001 | dispatch history | accepted=true
...
ORD-1001 | dispatch history | accepted=false
ORD-1002 | validation | accepted=true
ORD-1002 | payment | accepted=true
ORD-1002 | stock | accepted=true
ORD-1002 | dispatch history | accepted=false
```

The repeated `ORD-1001` is correctly rejected. The separate order `ORD-1002` reaches the same rule and is also rejected. Now there is a specific place to investigate: `DispatchHistory.claim()`.

The logpoint belongs on the line **after** `accepted` is assigned: breakpoint expressions run before the selected statement executes. Do not evaluate `rule.check().test(order)` in the log expression; the history rule changes state. Log the already calculated result.

## 2. Suspend where the evidence points

Disable the discovery logpoint. Set a suspending breakpoint on `return previousOrderId == null` in `DispatchHistory.claim()` with the condition:

```java
order.id().equals("ORD-1002")
```

Debug `main()` again. This stops once, with the interesting values already available:

| Value | Observed state |
| --- | --- |
| `order.id()` | `ORD-1002` |
| `order.customerId()` | `C-01` |
| `order.sku()` | `KEYBOARD` |
| `key` | `DispatchKey[customerId=C-01, sku=KEYBOARD]` |
| `previousOrderId` | `ORD-1001` |

Expand `dispatchedOrders` and inspect the call stack. The key identifies a customer and product, so two separate purchases collide. The earlier order claims the slot; the later order is mistaken for a replay and never reaches `dispatch()`.

## 3. Optional live fix

The defect is intentional for the demo. To finish with a repair, key the history by order ID instead:

```java
private final Map<String, String> dispatchedOrders = new HashMap<>();

boolean claim(Order order) {
    String previousOrderId = dispatchedOrders.putIfAbsent(order.id(), order.id());
    return previousOrderId == null;
}
```

Rerun with a fresh `OrderFulfillment` instance. The actual result now matches `[ORD-1001, ORD-1002]`: both purchases ship, and the replay of `ORD-1001` is still ignored.

The demonstration moves from a missing result, through a trace of rule decisions, to one conditional breakpoint for object and stack inspection. All discovery messages come from the debugger; the only output statement in the example is the batch's final result.
