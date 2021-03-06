package xyz.cloudkeeper.interpreter;

import xyz.cloudkeeper.model.api.WorkflowExecutionException;
import xyz.cloudkeeper.model.immutable.execution.ExecutionTrace;
import xyz.cloudkeeper.model.runtime.execution.RuntimeExecutionTrace;

/**
 * Signals an interpreter exception for which an execution trace is available.
 */
public class InterpreterException extends WorkflowExecutionException {
    private static final long serialVersionUID = -3720061577849119083L;

    private final ExecutionTrace executionTrace;

    public InterpreterException(RuntimeExecutionTrace executionTrace, String message, Throwable cause) {
        super("[" + executionTrace + "] " + message, cause);
        this.executionTrace = ExecutionTrace.copyOf(executionTrace);
    }

    public InterpreterException(RuntimeExecutionTrace executionTrace, String message) {
        super("[" + executionTrace + "] " + message);
        this.executionTrace = ExecutionTrace.copyOf(executionTrace);
    }

    public final ExecutionTrace getExecutionTrace() {
        return executionTrace;
    }
}
