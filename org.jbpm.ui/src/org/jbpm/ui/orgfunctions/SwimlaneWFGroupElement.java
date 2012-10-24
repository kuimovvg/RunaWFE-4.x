package org.jbpm.ui.orgfunctions;

import org.eclipse.swt.widgets.Composite;
import org.jbpm.ui.pref.WFEConnectionPreferencePage;
import org.jbpm.ui.sync.SyncUIHelper;

public class SwimlaneWFGroupElement extends SwimlaneGroupElement {

    @Override
    public void createGUI(Composite clientArea) {
        super.createGUI(clientArea);
        SyncUIHelper.createHeader(getClientArea(), WFEServerExecutorsImporter.getInstance(), WFEConnectionPreferencePage.class);
    }

}
