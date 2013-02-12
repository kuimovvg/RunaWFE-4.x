package ru.runa.gpd.extension.handler;

import org.eclipse.jface.resource.ImageDescriptor;

import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.util.XmlUtil;

public class ConfigBasedProvider extends ParamBasedProvider {
    @Override
    protected ImageDescriptor getLogo() {
        return SharedImages.getImageDescriptor(bundle, "/icons/logo.gif", false);
    }

    @Override
    protected ParamDefConfig getParamConfig(Delegable delegable) {
        String xml = XmlUtil.getParamDefConfig(bundle, delegable.getDelegationClassName());
        return ParamDefConfig.parse(xml);
    }
}
