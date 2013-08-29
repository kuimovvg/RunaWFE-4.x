package ru.runa.gpd.extension.handler;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableConfigurationDialog;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.extension.orgfunction.OrgFunctionsRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.XmlHighlightTextStyling;

public class EscalationActionHandlerProvider extends DelegableProvider {
    @Override
    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        return new EscalationConfigurationDialog(delegable.getDelegationConfiguration());
    }

    @Override
    public boolean validateValue(Delegable delegable) {
        String configuration = delegable.getDelegationConfiguration();
        try {
            OrgFunctionsRegistry.getInstance().getArtifact(configuration);
            return true;
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("EscalationActionHandler invalid configuration: " + configuration, e);
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
            combo.addSelectionListener(new LoggingSelectionAdapter() {
                
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
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
