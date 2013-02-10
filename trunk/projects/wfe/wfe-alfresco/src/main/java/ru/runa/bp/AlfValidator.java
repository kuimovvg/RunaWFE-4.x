package ru.runa.bp;

import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;
import ru.runa.wfe.validation.impl.FieldValidatorSupport;

/**
 * Base class for RunaWFE validator.
 * 
 * @author dofs
 */
public abstract class AlfValidator extends FieldValidatorSupport {

    protected abstract void validate(AlfSession session) throws Exception;

    @Override
    public final void validate() throws Exception {
        new AlfSessionWrapper<Object>() {

            @Override
            protected Object code() throws Exception {
                validate(session);
                return null;
            }

        }.runInSession();
    }

}
