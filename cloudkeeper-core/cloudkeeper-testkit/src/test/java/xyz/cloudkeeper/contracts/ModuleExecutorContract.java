package xyz.cloudkeeper.contracts;

import org.testng.Assert;
import org.testng.annotations.Test;
import xyz.cloudkeeper.examples.modules.Fibonacci;
import xyz.cloudkeeper.examples.modules.ThrowingModule;
import xyz.cloudkeeper.model.api.RuntimeStateProvider;
import xyz.cloudkeeper.model.api.UserException;
import xyz.cloudkeeper.model.api.executor.SimpleModuleExecutor;
import xyz.cloudkeeper.model.api.executor.SimpleModuleExecutorResult;
import xyz.cloudkeeper.model.api.staging.StagingArea;
import xyz.cloudkeeper.model.immutable.element.Index;
import xyz.cloudkeeper.model.immutable.element.SimpleName;
import xyz.cloudkeeper.model.immutable.execution.ExecutionTrace;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Contract test for simple-module execution functionality.
 *
 * <p>This contract test verifies all fundamental functionality of a
 * {@link SimpleModuleExecutor} implementation.
 */
public final class ModuleExecutorContract {
    private final SimpleModuleExecutor simpleModuleExecutor;
    private final StagingAreaContractProvider stagingAreaContractProvider;
    private final long awaitDurationMillis;

    public ModuleExecutorContract(SimpleModuleExecutor simpleModuleExecutor,
            StagingAreaContractProvider stagingAreaContractProvider, long awaitDurationMillis) {
        this.simpleModuleExecutor = simpleModuleExecutor;
        this.stagingAreaContractProvider = stagingAreaContractProvider;
        this.awaitDurationMillis = awaitDurationMillis;
    }

    private <T> T await(CompletableFuture<T> future) throws Exception {
        return future.get(awaitDurationMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Verifies that exceptions in a module are propagated correctly.
     */
    @Test
    public void submitTest() throws Exception {
        StagingAreaContractHelper helper = new StagingAreaContractHelper(stagingAreaContractProvider, Fibonacci.class);
        StagingArea stagingArea = helper.createStagingArea("submitTest")
            .resolveDescendant(
                ExecutionTrace.empty()
                    .resolveContent().resolveModule(SimpleName.identifier("loop"))
                    .resolveContent().resolveIteration(Index.index(0))
                    .resolveContent().resolveModule(SimpleName.identifier("sum"))
            );
        await(stagingArea.putObject(ExecutionTrace.empty().resolveInPort(SimpleName.identifier("num1")), 4));
        await(stagingArea.putObject(ExecutionTrace.empty().resolveInPort(SimpleName.identifier("num2")), 6));

        ExecutionTrace sumTrace = ExecutionTrace.empty().resolveOutPort(SimpleName.identifier("sum"));
        Assert.assertFalse(await(stagingArea.exists(sumTrace)));

        RuntimeStateProvider runtimeStateProvider = RuntimeStateProvider.of(helper.getRuntimeContext(), stagingArea);
        SimpleModuleExecutorResult result = await(simpleModuleExecutor.submit(runtimeStateProvider));
        Assert.assertNull(result.getExecutionException());
        Assert.assertEquals(await(stagingArea.getObject(sumTrace)), 10);
        Assert.assertNull(result.getExecutionException());
    }

    @Test
    public void submitTestThrowingModule() throws Exception {
        StagingAreaContractHelper helper
            = new StagingAreaContractHelper(stagingAreaContractProvider, ThrowingModule.class);
        StagingArea stagingArea = helper.createStagingArea("submitTestThrowingModule");
        await(stagingArea.putObject(ExecutionTrace.empty().resolveInPort(SimpleName.identifier("string")), "foo"));

        RuntimeStateProvider runtimeStateProvider = RuntimeStateProvider.of(helper.getRuntimeContext(), stagingArea);
        SimpleModuleExecutorResult result
            = await(simpleModuleExecutor.submit(runtimeStateProvider));
        Assert.assertFalse(
            await(stagingArea.exists(ExecutionTrace.empty().resolveOutPort(SimpleName.identifier("size"))))
        );
        try {
            throw result.getExecutionException();
        } catch (UserException exception) {
            Assert.assertTrue(exception.getCause() instanceof ThrowingModule.ExpectedException);
        }
    }
}
