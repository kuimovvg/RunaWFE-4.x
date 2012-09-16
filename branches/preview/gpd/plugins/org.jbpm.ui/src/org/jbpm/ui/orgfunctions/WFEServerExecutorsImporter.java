package ru.runa.bpm.ui.orgfunctions;

import org.eclipse.core.runtime.IProgressMonitor;
import ru.runa.bpm.ui.sync.WFEServerConnector;

import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.impl.interfaces.ExecutorService;
import ru.runa.af.service.impl.interfaces.ExecutorServiceHome;

public class WFEServerExecutorsImporter extends ExecutorsImporter {

    private static WFEServerExecutorsImporter instance;

    private WFEServerExecutorsImporter() {
        super(WFEServerConnector.getInstance());
    }

    public static synchronized WFEServerExecutorsImporter getInstance() {
        if (instance == null) {
            instance = new WFEServerExecutorsImporter();
        }
        return instance;
    }

    @Override
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        ExecutorService executorService = WFEServerConnector.getInstance().getService(ExecutorServiceHome.JNDI_NAME, ExecutorServiceHome.class);
        BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        Executor[] loaded = executorService.getAll(WFEServerConnector.getInstance().getSubject(), batchPresentation);
        for (Executor executor : loaded) {
            executors.put(executor.getName(), executor instanceof Group);
        }
        monitor.worked(100);
    }
}
