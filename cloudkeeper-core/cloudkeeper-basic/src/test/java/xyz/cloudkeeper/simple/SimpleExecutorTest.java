package xyz.cloudkeeper.simple;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.Factory;
import xyz.cloudkeeper.contracts.ModuleExecutorContract;
import xyz.cloudkeeper.model.api.executor.ModuleConnectorProvider;
import xyz.cloudkeeper.model.api.util.RecursiveDeleteVisitor;
import xyz.cloudkeeper.staging.MapStagingArea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleExecutorTest {
    private ExecutorService executorService;
    private Path tempDir;
    private LocalSimpleModuleExecutor simpleExecutor;

    public void setup() throws IOException {
        executorService = Executors.newFixedThreadPool(1);
        tempDir = Files.createTempDirectory(getClass().getSimpleName());

        ModuleConnectorProvider connectorProvider = new PrefetchingModuleConnectorProvider(tempDir);
        simpleExecutor = new LocalSimpleModuleExecutor.Builder(executorService, connectorProvider)
            .build();
    }

    @AfterSuite
    public void tearDown() throws IOException {
        Files.walkFileTree(tempDir, RecursiveDeleteVisitor.getInstance());
        executorService.shutdownNow();
    }

    @Factory
    public Object[] contractTests() throws IOException {
        setup();

        long awaitDurationMillis = 1000;
        return new Object[] {
            new ModuleExecutorContract(
                simpleExecutor,
                (identifier, runtimeContext, executionTrace) -> new MapStagingArea(runtimeContext, executionTrace),
                awaitDurationMillis
            )
        };
    }
}
