package ru.runa.bp;

import java.util.List;

import ru.runa.alfresco.RemoteAlfConnection;
import ru.runa.alfresco.RemoteAlfConnector;
import ru.runa.wfe.extension.OrgFunctionException;
import ru.runa.wfe.extension.orgfunction.GetActorsOrgFunctionBase;

/**
 * Base class for RunaWFE organization function.
 * 
 * @author dofs
 */
public abstract class AlfOrgFunction extends GetActorsOrgFunctionBase {

    @Override
    protected List<Long> getActorCodes(final Object... parameters) {
        try {
            return new RemoteAlfConnector<List<Long>>() {
                @Override
                protected List<Long> code() throws Exception {
                    return getExecutorCodes(alfConnection, parameters);
                }
            }.runInSession();
        } catch (Throwable e) {
            throw new OrgFunctionException(e);
        }
    }

    public abstract List<Long> getExecutorCodes(RemoteAlfConnection session, Object[] parameters) throws Exception;

}
