package org.jbpm.ui.custom;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.eclipse.jface.resource.ImageDescriptor;
import org.jbpm.ui.SharedImages;
import org.jbpm.ui.common.model.Delegable;
import org.jbpm.ui.util.IOUtils;

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
