package ru.runa.bp;

import ru.runa.alfresco.AlfObject;
import ru.runa.alfresco.AlfSession;
import ru.runa.wfe.var.FileVariable;

import com.google.common.base.Charsets;

/**
 * Handler for setting cm:content property.
 * 
 * @author dofs
 */
public class AlfSetContent extends AlfHandler {

    @Override
    protected void executeAction(AlfSession session, HandlerData handlerData) throws Exception {
        Object data = handlerData.getInputParam("data", null);
        if (data == null) {
            log.warn("No data found in process, returning...");
            return;
        }
        final byte[] content;
        final String mimetype;
        if (data instanceof FileVariable) {
            mimetype = ((FileVariable) data).getContentType();
            content = ((FileVariable) data).getData();
        } else {
            mimetype = handlerData.getInputParam("mimetype");
            content = data.toString().getBytes(Charsets.UTF_8);
        }
        AlfObject object = session.loadObject(handlerData.getInputParam("uuid"));
        session.setContent(object, content, mimetype);
    }

}
