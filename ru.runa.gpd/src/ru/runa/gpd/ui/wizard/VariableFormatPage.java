package ru.runa.gpd.ui.wizard;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.DynaContentWizardPage;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.StringFormat;

import com.google.common.collect.Maps;

public class VariableFormatPage extends DynaContentWizardPage {
    private VariableFormatArtifact type;
    private String[] componentClassNames;
    private final boolean editFormat;
    private static Map<String, String[]> containerFormats = Maps.newHashMap();
    static {
        containerFormats.put(ListFormat.class.getName(), new String[] { Localization.getString("VariableFormatPage.components.list.value") });
        containerFormats.put(MapFormat.class.getName(),
                new String[] { Localization.getString("VariableFormatPage.components.map.key"), Localization.getString("VariableFormatPage.components.map.value") });
    }

    public VariableFormatPage(Variable variable, boolean editFormat) {
        if (variable != null) {
            setTypeByFormatClassName(variable.getFormatClassName());
            componentClassNames = variable.getFormatComponentClassNames();
            if (containerFormats.containsKey(type.getName()) && componentClassNames.length != containerFormats.get(type.getName()).length) {
                createDefaultComponentClassNames();
            }
        } else {
            setTypeByFormatClassName(StringFormat.class.getName());
            componentClassNames = new String[0];
        }
        this.editFormat = editFormat;
    }

    public void setTypeByFormatClassName(String formatClassName) {
        this.type = VariableFormatRegistry.getInstance().getArtifactNotNull(formatClassName);
    }

    @Override
    protected int getGridLayoutColumns() {
        return 1;
    }

    @Override
    protected void createContent(Composite composite) {
        final Combo combo = createFormatCombo(composite);
        combo.setEnabled(editFormat);
        combo.setText(type.getLabel());
        combo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                type = VariableFormatRegistry.getInstance().getArtifactNotNullByLabel(combo.getText());
                createDefaultComponentClassNames();
                updateContent();
            }
        });
        dynaComposite = new Composite(composite, SWT.NONE);
        dynaComposite.setLayout(new GridLayout(2, false));
        dynaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private void createDefaultComponentClassNames() {
        String[] labels = containerFormats.get(type.getName());
        componentClassNames = new String[labels != null ? labels.length : 0];
        for (int i = 0; i < componentClassNames.length; i++) {
            componentClassNames[i] = StringFormat.class.getName();
        }
    }

    private Combo createFormatCombo(Composite composite) {
        final Combo combo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        for (VariableFormatArtifact artifact : VariableFormatRegistry.getInstance().getAll()) {
            if (artifact.isEnabled()) {
                combo.add(artifact.getLabel());
            }
        }
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return combo;
    }

    @Override
    protected void createDynaContent() {
        String[] labels = containerFormats.get(type.getName());
        if (labels != null) {
            GridData strokeData = new GridData(GridData.FILL_HORIZONTAL);
            strokeData.horizontalSpan = 2;
            SWTUtils.createStrokeComposite(dynaComposite, strokeData, Localization.getString("VariableFormatPage.components.label"), 3);
            for (int i = 0; i < labels.length; i++) {
                Label label = new Label(dynaComposite, SWT.NONE);
                label.setText(labels[i]);
                final Combo combo = createFormatCombo(dynaComposite);
                combo.setData(i);
                combo.setText(VariableFormatRegistry.getInstance().getArtifact(componentClassNames[i]).getLabel());
                combo.addSelectionListener(new LoggingSelectionAdapter() {
                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        int index = (Integer) combo.getData();
                        componentClassNames[index] = VariableFormatRegistry.getInstance().getArtifactNotNullByLabel(combo.getText()).getName();
                    }
                });
            }
        }
    }

    @Override
    protected void verifyContentIsValid() {
    }

    public VariableFormatArtifact getType() {
        return type;
    }

    public String[] getComponentClassNames() {
        return componentClassNames;
    }
}
