package ru.runa.bp.handler;

import ru.runa.alfresco.AlfConnection;
import ru.runa.alfresco.AlfObject;
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
    protected void executeAction(AlfConnection alfConnection, AlfHandlerData alfHandlerData) throws Exception {
        Object data = alfHandlerData.getInputParamValue("data");
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
            mimetype = alfHandlerData.getInputParamValueNotNull("mimetype");
            content = data.toString().getBytes(Charsets.UTF_8);
        }
        AlfObject object = alfConnection.loadObjectNotNull(alfHandlerData.getInputParamValueNotNull(String.class, "uuid"));
        alfConnection.setContent(object, content, mimetype);
    }

}
