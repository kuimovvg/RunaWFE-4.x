package ru.runa.bp;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;
import ru.runa.wfe.os.OrgFunction;
import ru.runa.wfe.os.OrgFunctionException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Lists;

/**
 * Base class for RunaWFE organization function.
 * 
 * @author dofs
 */
public abstract class AlfOrgFunction implements OrgFunction {
    protected Log log = LogFactory.getLog(getClass());

    @Autowired
    protected ExecutorDAO executorDAO;

    @Override
    public List<? extends Executor> getExecutors(final Object... parameters) throws OrgFunctionException {
        try {
            return new AlfSessionWrapper<List<Actor>>() {
                @Override
                protected List<Actor> code() throws Exception {
                    List<Actor> actors = Lists.newArrayList();
                    Long[] codes = getExecutorCodes(session, parameters);
                    for (Long code : codes) {
                        actors.add(executorDAO.getActorByCode(code));
                    }
                    return actors;
                }
            }.runInSession();
        } catch (Throwable e) {
            throw new OrgFunctionException(e);
        }
    }

    public abstract Long[] getExecutorCodes(AlfSession session, Object[] parameters) throws Exception;

}
