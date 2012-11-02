package ru.runa.wf.office.shared;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.ConfigurationException;

import com.google.common.base.Throwables;

public abstract class OfficeFilesSupplierHandler<T extends FilesSupplierConfig> extends CommonHandler {
    protected static Log log = LogFactory.getLog(OfficeFilesSupplierHandler.class);

    protected T config;

    protected abstract FilesSupplierConfigParser<T> createParser();

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        try {
            FilesSupplierConfigParser<T> parser = createParser();
            this.config = parser.parse(configuration);
        } catch (Throwable th) {
            Throwables.propagateIfInstanceOf(th, ConfigurationException.class);
            Throwables.propagate(th);
        }
    }

}
