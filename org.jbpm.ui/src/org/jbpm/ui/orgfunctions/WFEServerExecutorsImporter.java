package org.jbpm.ui.orgfunctions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jbpm.ui.sync.WFEServerConnector;

import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.presentation.BatchPresentationFactory;
import ru.runa.service.af.ExecutorService;

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
        List<Executor> loaded = executorService.getAll(WFEServerConnector.getInstance().getSubject(), batchPresentation);
        for (Executor executor : loaded) {
            executors.put(executor.getName(), executor instanceof Group);
        }
        monitor.worked(100);
    }
}
