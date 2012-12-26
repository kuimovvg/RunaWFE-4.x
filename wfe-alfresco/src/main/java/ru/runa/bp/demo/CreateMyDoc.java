package ru.runa.bp.demo;

import ru.runa.alfresco.AlfSession;
import ru.runa.bp.AlfHandler;
import ru.runa.bp.HandlerData;
import ru.runa.wfe.var.FileVariable;

public class CreateMyDoc extends AlfHandler {

    @Override
    protected void executeAction(AlfSession session, HandlerData handlerData) throws Exception {
        MyDoc myDoc = new MyDoc();
        session.createObject(myDoc);
        FileVariable var = handlerData.getInputParam(FileVariable.class, "file");
        session.setContent(myDoc, var.getData(), var.getContentType());
        handlerData.setOutputParam("uuid", myDoc.getUuidRef());
    }

}
