package ru.runa.bpm.ui.custom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.dialog.ChooseVariableDialog;
import ru.runa.bpm.ui.dialog.DelegableConfigurationDialog;
import ru.runa.bpm.ui.dialog.JavaHighlightTextStyling;
import ru.runa.bpm.ui.resource.Messages;

public class BSHActionHandlerProvider extends DelegableProvider {

    @Override
    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        return new ConfigurationDialog(
                delegable.getDelegationConfiguration(), 
                definition.getVariableNames(true));
    }

    public static class ConfigurationDialog extends DelegableConfigurationDialog {
        private final List<String> variableNames;
        private HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());

        public ConfigurationDialog(String initialValue, List<String> variableNames) {
            super(initialValue);
            this.variableNames = new ArrayList<String>();
            for (String string : variableNames) {
                if (!string.contains(" ")) {
                    this.variableNames.add(string);
                }
            }
        }
        
        @Override
        protected void createDialogHeader(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(2, false));
            composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Hyperlink hl3 = new Hyperlink(composite, SWT.NONE);
            hl3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
            hl3.setText(Messages.getString("button.insert_variable"));
            hl3.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    ChooseVariableDialog dialog = new ChooseVariableDialog(variableNames);
                    String variableName = dialog.openDialog();
                    if (variableName != null) {
                        styledText.insert(variableName);
                        styledText.setFocus();
                        styledText.setCaretOffset(styledText.getCaretOffset() + variableName.length());
                    }
                }
            });
            hyperlinkGroup.add(hl3);
        }
        
        @Override
        protected void createDialogFooter(Composite composite) {
            styledText.addLineStyleListener(new JavaHighlightTextStyling(variableNames));
        }
    }
    
}
