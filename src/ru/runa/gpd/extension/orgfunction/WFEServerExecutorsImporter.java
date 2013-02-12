package ru.runa.gpd.extension.orgfunction;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ru.runa.gpd.wfe.WFEServerConnector;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

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
        ExecutorService executorService = WFEServerConnector.getInstance().getService("ExecutorServiceBean");
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createDefault();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        List<Executor> loaded = executorService.getAll(WFEServerConnector.getInstance().getUser(), batchPresentation);
        for (Executor executor : loaded) {
            executors.put(executor.getName(), executor instanceof Group);
        }
        monitor.worked(100);
    }
}
