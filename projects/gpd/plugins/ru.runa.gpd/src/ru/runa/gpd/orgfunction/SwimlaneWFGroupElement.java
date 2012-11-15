package ru.runa.gpd.orgfunction;

import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.wfe.SyncUIHelper;

public class SwimlaneWFGroupElement extends SwimlaneGroupElement {

    @Override
    public void createGUI(Composite clientArea) {
        super.createGUI(clientArea);
        SyncUIHelper.createHeader(getClientArea(), WFEServerExecutorsImporter.getInstance(), WFEConnectionPreferencePage.class);
    }

}
