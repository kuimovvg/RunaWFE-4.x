package ru.runa.bp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.alfresco.AlfSession;
import ru.runa.alfresco.AlfSessionWrapper;
import ru.runa.commons.validation.ValidationException;
import ru.runa.validators.FieldValidatorSupport;


/**
 * Base class for RunaWFE validator.
 * @author dofs
 */
public abstract class AlfValidator extends FieldValidatorSupport {
    protected Log log = LogFactory.getLog(getClass());

    protected abstract void validate(AlfSession session) throws ValidationException;

    public final void validate() throws ValidationException {
        new AlfSessionWrapper<Object>() {

            @Override
            protected Object code() throws Exception {
                validate(session);
                return null;
            }
            
        }.runInSession();
    }
    
}
