package ru.runa.wf.office.shared;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.extension.handler.CommonHandler;

public abstract class OfficeFilesSupplierHandler<T extends FilesSupplierConfig> extends CommonHandler {
    protected static Log log = LogFactory.getLog(OfficeFilesSupplierHandler.class);

    protected T config;

    protected abstract FilesSupplierConfigParser<T> createParser();

    @Override
    public void setConfiguration(String configuration) throws Exception {
        FilesSupplierConfigParser<T> parser = createParser();
        this.config = parser.parse(configuration);
    }

}
