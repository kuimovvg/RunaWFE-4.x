package ru.runa.bp;

import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;
import ru.runa.wfe.validation.FieldValidator;

/**
 * Base class for RunaWFE validator.
 * 
 * @author dofs
 */
public abstract class AlfValidator extends FieldValidator {

    protected abstract void validate(AlfSession session);

    @Override
    public final void validate() {
        new AlfSessionWrapper<Object>() {

            @Override
            protected Object code() throws Exception {
                validate(session);
                return null;
            }

        }.runInSession();
    }

}
