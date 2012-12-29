package ru.runa.bp;

import java.util.List;

import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;
import ru.runa.wfe.os.OrgFunctionBase;
import ru.runa.wfe.os.OrgFunctionException;

/**
 * Base class for RunaWFE organization function.
 * 
 * @author dofs
 */
public abstract class AlfOrgFunction extends OrgFunctionBase {

    @Override
    protected List<Long> getExecutorCodes(final Object... parameters) {
        try {
            return new AlfSessionWrapper<List<Long>>() {
                @Override
                protected List<Long> code() throws Exception {
                    return getExecutorCodes(session, parameters);
                }
            }.runInSession();
        } catch (Throwable e) {
            throw new OrgFunctionException(e);
        }
    }

    public abstract List<Long> getExecutorCodes(AlfSession session, Object[] parameters) throws Exception;

}
