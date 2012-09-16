package ru.runa.bpm.ui.custom;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.eclipse.jface.resource.ImageDescriptor;
import ru.runa.bpm.ui.SharedImages;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.util.IOUtils;

public class ConfigBasedProvider extends ParamBasedProvider {

    @Override
    protected ImageDescriptor getLogo() {
        return SharedImages.getImageDescriptor(bundle, "/icons/logo.gif", false);
    }

    @Override
    protected ParamDefConfig getParamConfig(Delegable delegable) {
        String path = "/conf/" + getSimpleClassName(delegable.getDelegationClassName()) + ".xml";
        try {
            InputStream is = bundle.getEntry(path).openStream();
            Document doc = DocumentHelper.parseText(IOUtils.readStream(is));
            ParamDefConfig config = ParamDefConfig.parse(doc);
            return config;
        } catch (Exception e) {
            throw new RuntimeException("Unable parse config at " + path, e);
        }
    }

    private String getSimpleClassName(String className) {
        int dotIndex = className.lastIndexOf(".");
        return className.substring(dotIndex + 1);
    }

}
