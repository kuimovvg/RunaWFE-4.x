package ru.runa.gpd.extension.orgfunction;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.BotTask;

public class SwimlaneBotElement extends SwimlaneElement {
    private Combo combo;

    public SwimlaneBotElement() {
        setOrgFunctionDefinitionName(BotTask.SWIMLANE_DEFINITION_NAME);
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
        combo.add(OrgFunctionDefinition.DEFAULT.getName());
        List<String> botNames = BotCache.getAllBotNames();
        combo.setItems(botNames.toArray(new String[botNames.size()]));
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                OrgFunctionDefinition definition = createNew();
                definition.getParameters().get(0).setValue(combo.getText());
                fireCompletedEvent(definition);
            }
        });
    }

    private GridData createLayoutData(int numColumns, boolean fillGrab) {
        GridData td = new GridData(fillGrab ? GridData.FILL_HORIZONTAL : GridData.CENTER);
        td.horizontalSpan = numColumns;
        return td;
    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionDefinition definition) {
        super.open(path, swimlaneName, definition);
        String value = "";
        if (currentDefinition != null && currentDefinition.getParameters().size() > 0) {
            value = currentDefinition.getParameters().get(0).getValue();
        }
        combo.setText(value);
    }
    //    private static class MappedNameComparator implements Comparator<String> {
    //        public int compare(String o1, String o2) {
    //            String m1 = TypeNameMapping.getTypeName(o1);
    //            String m2 = TypeNameMapping.getTypeName(o2);
    //            return m1.compareTo(m2);
    //        }
    //    }
}
