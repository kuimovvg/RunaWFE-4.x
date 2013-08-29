package ru.runa.gpd.swimlane;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public class BotSwimlaneElement extends OrgFunctionSwimlaneElement {
    private Combo combo;

    public BotSwimlaneElement() {
        super(BotTask.SWIMLANE_DEFINITION_NAME);
    }

    @Override
    public void createGUI(Composite clientArea) {
        createComposite(clientArea, 2);
        Label label = new Label(getClientArea(), SWT.NONE);
        label.setText(Localization.getString("ExecutorByNameFunction"));
        label.setLayoutData(createLayoutData(1, false));
        combo = new Combo(getClientArea(), SWT.READ_ONLY);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        combo.setVisibleItemCount(10);
        List<String> botNames = BotCache.getAllBotNames();
        combo.setItems(botNames.toArray(new String[botNames.size()]));
        combo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                setOrgFunctionParameterValue(0, combo.getText());
                fireCompletedEvent();
            }
        });
    }

    private GridData createLayoutData(int numColumns, boolean fillGrab) {
        GridData td = new GridData(fillGrab ? GridData.FILL_HORIZONTAL : GridData.CENTER);
        td.horizontalSpan = numColumns;
        return td;
    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionSwimlaneInitializer swimlaneInitializer) {
        super.open(path, swimlaneName, swimlaneInitializer);
        combo.setText(getOrgFunctionParameterValue(0));
    }
}
