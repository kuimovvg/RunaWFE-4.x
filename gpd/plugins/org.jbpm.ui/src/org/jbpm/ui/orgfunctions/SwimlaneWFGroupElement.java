package ru.runa.bpm.ui.orgfunctions;

import org.eclipse.swt.widgets.Composite;
import ru.runa.bpm.ui.pref.WFEConnectionPreferencePage;
import ru.runa.bpm.ui.sync.SyncUIHelper;

public class SwimlaneWFGroupElement extends SwimlaneGroupElement {

    @Override
    public void createGUI(Composite clientArea) {
        super.createGUI(clientArea);
        SyncUIHelper.createHeader(getClientArea(), WFEServerExecutorsImporter.getInstance(), WFEConnectionPreferencePage.class);
    }

}
