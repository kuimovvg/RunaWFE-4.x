package ru.runa.gpd.ui.dialog;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.wfe.DataImporter;
import ru.runa.gpd.wfe.WFEServerExecutorsImporter;
import ru.runa.gpd.wfe.WFEServerRelationsImporter;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

import com.google.common.collect.Lists;

public class EditableSyncDialog extends Dialog {

    private Text variableNameField;

    private String value;

    private final String variableTypeFilter;

    private final List<String> values;

    private final String rawValue;

    public boolean isVariableNameEditable() {
        return true;
    }

    public EditableSyncDialog(String variableTypeFilter, List<String> values, String rawValue) {
        super(Display.getCurrent().getActiveShell());
        this.variableTypeFilter = variableTypeFilter;
        this.values = values;
        this.rawValue = rawValue;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("button.choose"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

        area.setLayout(new GridLayout(2, false));
        if (!getType().equals(Type.LONG) && !getType().equals(Type.OBJECT)) {
            SyncUIHelper.createHeader(area, getDataImporter(), WFEConnectionPreferencePage.class, null);
        }
        SWTUtils.createLink(area, Localization.getString("button.choose"), getHyperlinkAdapter());

        variableNameField = new Text(area, SWT.BORDER);
        variableNameField.setEditable(isVariableNameEditable());
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 200;
        variableNameField.setLayoutData(gridData);
        variableNameField.setText(rawValue == null ? "" : rawValue);
        variableNameField.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                String selected = ((Text) e.getSource()).getText();
                value = selected;
            }
        });
        return area;
    }

    public Object openDialog() {
        if (open() == IDialogConstants.OK_ID) {
            return value;
        }
        return null;
    }

    private static boolean isGroup(String className) {
        return className != null && VariableFormatRegistry.isAssignableFrom(Group.class, className);
    }

    private DataImporter getDataImporter() {
        DataImporter importer = null;
        Type type = getType();
        switch (type) {
        case EXECUTOR:
            importer = WFEServerExecutorsImporter.getInstance();
            break;
        case GROUP:
            importer = WFEServerExecutorsImporter.getInstance();
            break;
        case RELATION:
            importer = WFEServerRelationsImporter.getInstance();
            break;
        default:
            break;
        }
        return importer;
    }

    public LoggingHyperlinkAdapter getHyperlinkAdapter() {
        LoggingHyperlinkAdapter adapter = null;
        switch (getType()) {
        case EXECUTOR:
        case GROUP:
            adapter = getGroupHyperlinkAdapter(isGroup(variableTypeFilter));
            break;
        case RELATION:
            adapter = getRelationHyperlinkAdapter();
            break;
        case LONG:
        case OBJECT:
            adapter = getLongHyperlinkAdapter();
            break;
        default:
            break;
        }
        return adapter;
    }

    private LoggingHyperlinkAdapter getRelationHyperlinkAdapter() {
        return new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                ChooseItemDialog dialog = new ChooseItemDialog(Localization.getString("Relations"), null, true);
                List<String> relations = WFEServerRelationsImporter.getInstance().loadCachedData();
                if (relations != null) {
                    relations.addAll(values);
                    dialog.setItems(relations);
                    if (dialog.open() == IDialogConstants.OK_ID) {
                        variableNameField.setText((String) dialog.getSelectedItem());
                    }
                }
            }
        };
    }

    private LoggingHyperlinkAdapter getGroupHyperlinkAdapter(final boolean isGroup) {
        return new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                String title = isGroup ? Localization.getString("EditableSyncDialog.title.group") : Localization
                        .getString("EditableSyncDialog.title.executors");
                ChooseItemDialog dialog = new ChooseItemDialog(title, null, true);
                Map<String, Boolean> executors = WFEServerExecutorsImporter.getInstance().loadCachedData();
                List<String> groups = Lists.newArrayList();
                for (Map.Entry<String, Boolean> entry : executors.entrySet()) {
                    if (isGroup) {
                        if (entry.getValue().equals(isGroup)) {
                            groups.add(entry.getKey());
                        }
                    } else {
                        groups.add(entry.getKey());
                    }
                }
                if (values != null) {
                    for (String val : values) {
                        if (!groups.contains(val)) {
                            groups.add(val);
                        }
                    }
                }
                dialog.setItems(groups);
                if (dialog.open() == IDialogConstants.OK_ID) {
                    variableNameField.setText((String) dialog.getSelectedItem());
                }
            }
        };
    }

    private LoggingHyperlinkAdapter getLongHyperlinkAdapter() {
        return new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                ChooseItemDialog dialog = new ChooseItemDialog(Localization.getString("Positions"), null, true);
                if (values != null) {
                    dialog.setItems(values);
                    if (dialog.open() == IDialogConstants.OK_ID) {
                        variableNameField.setText((String) dialog.getSelectedItem());
                    }
                }
            }
        };
    }

    private Type getType() {
        if (variableTypeFilter == null || Object.class.getName().equals(variableTypeFilter)) {
            return Type.OBJECT;
        } else if (VariableFormatRegistry.isAssignableFrom(Group.class.getName(), variableTypeFilter)) {
            return Type.GROUP;
        } else if (VariableFormatRegistry.isAssignableFrom(Executor.class.getName(), variableTypeFilter)) {
            return Type.EXECUTOR;
        } else if (VariableFormatRegistry.isAssignableFrom(String.class.getName(), variableTypeFilter)) {
            return Type.RELATION;
        } else if (VariableFormatRegistry.isAssignableFrom(Long.class.getName(), variableTypeFilter)) {
            return Type.LONG;
        } else {
            throw new IllegalArgumentException();
        }
    }

    enum Type {
        EXECUTOR, GROUP, RELATION, LONG, OBJECT
    }
}
