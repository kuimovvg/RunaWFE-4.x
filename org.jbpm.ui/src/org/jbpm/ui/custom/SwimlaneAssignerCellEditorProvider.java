package org.jbpm.ui.custom;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.common.model.Delegable;
import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.Swimlane;
import org.jbpm.ui.dialog.DelegableConfigurationDialog;
import org.jbpm.ui.dialog.SwimlaneConfigDialog;
import org.jbpm.ui.dialog.XmlHighlightTextStyling;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SwimlaneAssignerCellEditorProvider extends DelegableProvider {
    private ProcessDefinition definition;

    @Override
    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        definition = ((GraphElement) delegable).getProcessDefinition();
        return new SwimlaneAssignerConfigurationDialog(
                delegable.getDelegationConfiguration(), 
                definition.getSwimlaneNames());
    }

    @Override
    public boolean validateValue(Delegable delegable) {
        try {
            XmlUtil.parseDocument(new ByteArrayInputStream(delegable.getDelegationConfiguration().getBytes(PluginConstants.UTF_ENCODING)));
            return true;
        } catch (Exception e) {
            DesignerLogger.logErrorWithoutDialog("SwimlaneAssignerActionHandler; invalid configuration", e);
            return false;
        }
    }

    public class SwimlaneAssignerConfigurationDialog extends DelegableConfigurationDialog {
        private final List<String> swimlaneNames;
        private Combo swimlaneNameCombo;
        private Text swimlaneInitializerText;
        private Button swimlaneInitializerButton;
        
        public SwimlaneAssignerConfigurationDialog(String initialValue, List<String> swimlaneNames) {
            super(initialValue);
            this.swimlaneNames = swimlaneNames;
        }

        @Override
        protected Point getInitialSize() {
            return new Point(500, 300);
        }

        @Override
        protected void createDialogHeader(Composite composite) {
            Composite gui = new Composite(composite, SWT.NONE);
            gui.setLayout(new GridLayout(3, false));
            gui.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            
            String initialSwimlaneName = "";
            String initialSwimlaneInitializer = "";
            try {
                Document document = XmlUtil.parseDocument(new ByteArrayInputStream(initialValue.getBytes(PluginConstants.UTF_ENCODING)));
                Element root = document.getDocumentElement();
                initialSwimlaneName = root.getAttribute("swimlaneName");
                initialSwimlaneInitializer = root.getAttribute("swimlaneInititalizer");
            } catch (Exception e) {
            }
            
            {
                Label label = new Label(gui, SWT.NONE);
                label.setText(Messages.getString("swimlane.name"));
            }
            swimlaneNameCombo = new Combo(gui, SWT.READ_ONLY);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.horizontalSpan = 2;
            swimlaneNameCombo.setLayoutData(gridData);
            swimlaneNameCombo.setItems(swimlaneNames.toArray(new String[swimlaneNames.size()]));
            swimlaneNameCombo.setText(initialSwimlaneName);
            swimlaneNameCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateText();
                }
            });
            
            {
                Label label = new Label(gui, SWT.NONE);
                label.setText(Messages.getString("swimlane.initializer"));
            }

            swimlaneInitializerText = new Text(gui, SWT.BORDER);
            swimlaneInitializerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            swimlaneInitializerText.setText(initialSwimlaneInitializer);
            swimlaneInitializerText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    updateText();
                }
            });
            
            swimlaneInitializerButton = new Button(gui, SWT.NONE);
            swimlaneInitializerButton.setText("...");
            swimlaneInitializerButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Swimlane swimlane = new Swimlane();
                    swimlane.setName("TEST");
                    swimlane.setDelegationConfiguration(swimlaneInitializerText.getText());
                    SwimlaneConfigDialog dialog = new SwimlaneConfigDialog(definition, swimlane, "");
                    if (dialog.open() == IDialogConstants.OK_ID) {
                        swimlaneInitializerText.setText(dialog.getConfiguration());
                    }
                }
            });
            
            super.createDialogHeader(composite);
        }
        
        public void updateText() {
            String xml = "<Assign swimlaneName=\"" + swimlaneNameCombo.getText() + "\" ";
            xml += "swimlaneInititalizer=\"" + swimlaneInitializerText.getText() + "\"/>";
            styledText.setText(xml);
        }

        @Override
        protected void createDialogFooter(Composite composite) {
            styledText.addLineStyleListener(new XmlHighlightTextStyling());
        }

    }
}
