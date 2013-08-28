package ru.runa.bp.handler;

import ru.runa.alfresco.AlfObject;
import ru.runa.alfresco.AlfSession;
import ru.runa.bp.AlfHandler;
import ru.runa.bp.AlfHandlerData;
import ru.runa.wfe.var.FileVariable;

import com.google.common.base.Charsets;

/**
 * Handler for setting cm:content property.
 * 
 * @author dofs
 */
public class AlfSetContent extends AlfHandler {

    @Override
    protected void executeAction(AlfSession session, AlfHandlerData alfHandlerData) throws Exception {
        Object data = alfHandlerData.getInputParam("data", null);
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
            mimetype = alfHandlerData.getInputParam("mimetype");
            content = data.toString().getBytes(Charsets.UTF_8);
        }
        AlfObject object = session.loadObjectNotNull(alfHandlerData.getInputParam(String.class, "uuid"));
        session.setContent(object, content, mimetype);
    }

}
