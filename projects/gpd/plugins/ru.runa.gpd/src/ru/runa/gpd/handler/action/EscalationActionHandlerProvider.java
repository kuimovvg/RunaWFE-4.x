package ru.runa.gpd.handler.action;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.handler.DelegableConfigurationDialog;
import ru.runa.gpd.handler.DelegableProvider;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.orgfunction.OrgFunctionsRegistry;
import ru.runa.gpd.ui.dialog.XmlHighlightTextStyling;

public class EscalationActionHandlerProvider extends DelegableProvider {
    @Override
    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        return new EscalationConfigurationDialog(delegable.getDelegationConfiguration());
    }

    @Override
    public boolean validateValue(Delegable delegable) {
        try {
            OrgFunctionsRegistry.getInstance().getArtifact(delegable.getDelegationConfiguration());
            return true;
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("EscalationActionHandler; invalid configuration", e);
            return false;
        }
    }

    public class EscalationConfigurationDialog extends DelegableConfigurationDialog {
        private Combo combo;

        public EscalationConfigurationDialog(String initialValue) {
            super(initialValue);
        }

        @Override
        protected Point getInitialSize() {
            return new Point(500, 300);
        }

        @Override
        protected void createDialogHeader(Composite composite) {
            Composite gui = new Composite(composite, SWT.NONE);
            gui.setLayout(new GridLayout(2, false));
            gui.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            String orgFunctionLabel = "";
            try {
                orgFunctionLabel = OrgFunctionsRegistry.getInstance().getArtifact(initialValue).getLabel();
            } catch (Exception e) {
            }
            {
                Label label = new Label(gui, SWT.NONE);
                label.setText(Localization.getString("swimlane.initializer"));
            }
            combo = new Combo(gui, SWT.READ_ONLY);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            List<OrgFunctionDefinition> definitions = OrgFunctionsRegistry.getInstance().getAll();
            for (OrgFunctionDefinition definition : definitions) {
                combo.add(definition.getLabel());
            }
            combo.setText(orgFunctionLabel);
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateText();
                }
            });
            super.createDialogHeader(composite);
        }

        public void updateText() {
            styledText.setText(OrgFunctionsRegistry.getInstance().getArtifactNotNullByLabel(combo.getText()).getName());
        }

        @Override
        protected void createDialogFooter(Composite composite) {
            styledText.addLineStyleListener(new XmlHighlightTextStyling());
        }
    }
}
