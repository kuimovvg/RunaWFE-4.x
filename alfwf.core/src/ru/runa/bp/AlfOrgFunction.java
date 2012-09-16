package ru.runa.bp;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.organizationfunction.OrganizationFunction;
import ru.runa.af.organizationfunction.OrganizationFunctionException;
import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;

/**
 * Base class for RunaWFE organization function.
 * @author dofs
 */
public abstract class AlfOrgFunction implements OrganizationFunction {
    protected Log log = LogFactory.getLog(getClass());
    @Autowired
    private ExecutorDAO executorDAO;

    public abstract List<Long> getExecutorCodes(AlfSession session, Object[] parameters) throws Exception;

    @Override
    public final List<Long> getExecutorIds(final Object[] parameters) throws OrganizationFunctionException {
        try {
            return new AlfSessionWrapper<List<Long>>() {
                @Override
                protected List<Long> code() throws Exception {
                    List<Long> codes = getExecutorCodes(session, parameters);
                    return executorDAO.getActorIdsByCodes(codes);
                }
            }.runInSession();
        } catch (Throwable e) {
            log.error("", e);
            throw new OrganizationFunctionException(e);
        }
    }

}
