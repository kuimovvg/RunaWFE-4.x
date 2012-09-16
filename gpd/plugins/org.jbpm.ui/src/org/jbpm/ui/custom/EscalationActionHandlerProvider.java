package ru.runa.bpm.ui.custom;

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
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.dialog.DelegableConfigurationDialog;
import ru.runa.bpm.ui.dialog.XmlHighlightTextStyling;
import ru.runa.bpm.ui.orgfunctions.OrgFunctionDefinition;
import ru.runa.bpm.ui.orgfunctions.OrgFunctionsRegistry;
import ru.runa.bpm.ui.resource.Messages;

public class EscalationActionHandlerProvider extends DelegableProvider {

    @Override
    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        return new EscalationConfigurationDialog(delegable.getDelegationConfiguration());
    }

    @Override
    public boolean validateValue(Delegable delegable) {
        try {
            OrgFunctionsRegistry.getDefinitionByClassName(delegable.getDelegationConfiguration());
            return true;
        } catch (Exception e) {
            DesignerLogger.logErrorWithoutDialog("EscalationActionHandler; invalid configuration", e);
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

            String orgFunctionDisplayName = "";
            try {
                orgFunctionDisplayName = OrgFunctionsRegistry.getDefinitionByClassName(initialValue).getName();
            } catch (Exception e) {
            }

            {
                Label label = new Label(gui, SWT.NONE);
                label.setText(Messages.getString("swimlane.initializer"));
            }

            combo = new Combo(gui, SWT.READ_ONLY);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            List<OrgFunctionDefinition> definitions = OrgFunctionsRegistry.getAllOrgFunctionDefinitions();
            for (OrgFunctionDefinition definition : definitions) {
                combo.add(definition.getName());
            }
            combo.setText(orgFunctionDisplayName);
            combo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateText();
                }
            });
            super.createDialogHeader(composite);
        }

        public void updateText() {
            styledText.setText(OrgFunctionsRegistry.getDefinitionByName(combo.getText()).getClassName());
        }

        @Override
        protected void createDialogFooter(Composite composite) {
            styledText.addLineStyleListener(new XmlHighlightTextStyling());
        }

    }
}
