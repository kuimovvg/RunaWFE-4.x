package ru.runa.bpm.ui.custom;

import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import ru.runa.bpm.ui.SharedImages;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.TypeNameMapping;

public abstract class ParamBasedProvider extends DelegableProvider {

    protected abstract ParamDefConfig getParamConfig(Delegable delegable);
    
    protected ImageDescriptor getLogo() {
        return SharedImages.getImageDescriptor("/icons/logo.gif");
    }
    
    @Override
    public final String showConfigurationDialog(Delegable delegable) {
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        ParamDefConfig config = getParamConfig(delegable);
        
        ConfigurationWizardPage page = new ConfigurationWizardPage(
                definition.getVariableFormats(true), 
                config.parseConfiguration(delegable.getDelegationConfiguration()), 
                config, 
                TypeNameMapping.getTypeName(delegable.getDelegationClassName()));
        final ConfigurationWizard wizard = new ConfigurationWizard(page);
        WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard) {

            @Override
            protected void createButtonsForButtonBar(Composite parent) {
                Button copyButton = createButton(parent, 197, Messages.getString("button.copy"), false);
                copyButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Clipboard clipboard = new Clipboard(Display.getCurrent());
                        clipboard.setContents(
                                new String[]{ wizard.getWizardPage().getConfiguration() }, 
                                new Transfer[]{ TextTransfer.getInstance() });
                        clipboard.dispose();
                    }
                });
                super.createButtonsForButtonBar(parent);
            }
            
        };
        if (dialog.open() == IDialogConstants.OK_ID) {
            return wizard.getConfiguration();
        }
        return null;
    }
    
    @Override
    public boolean validateValue(Delegable delegable) {
        return getParamConfig(delegable).validate(delegable.getDelegationConfiguration());
    }
    
    public class ConfigurationWizard extends Wizard {
        private final ConfigurationWizardPage wizardPage;
        private String configuration;

        public ConfigurationWizard(ConfigurationWizardPage wizardPage) {
            this.wizardPage = wizardPage;
            setNeedsProgressMonitor(true);
            setWindowTitle(Messages.getString("property.delegation.configuration"));
        }

        @Override
        public void addPages() {
            addPage(wizardPage);
        }

        public String getConfiguration() {
            return configuration;
        }
        
        public ConfigurationWizardPage getWizardPage() {
            return wizardPage;
        }
        
        @Override
        public boolean performFinish() {
            configuration = wizardPage.getConfiguration();
            return true;
        }
    }
    
    public class ConfigurationWizardPage extends WizardPage {
        private ParamDefComposite paramDefComposite;

        private final ParamDefConfig config;
        private final Map<String, String> variableNames;
        private final Map<String, String> properties;

        protected ConfigurationWizardPage(Map<String, String> variableNames, Map<String, String> properties, ParamDefConfig config, String headerText) {
            super("config", headerText, getLogo());
            this.variableNames = variableNames;
            this.properties = properties;
            this.config = config;
        }

        public void createControl(Composite parent) {
            paramDefComposite = new ParamDefComposite(parent, config, properties, variableNames);
            setControl(paramDefComposite);
            paramDefComposite.createUI();
        }

        public String getConfiguration() {
            Map<String, String> properties = paramDefComposite.readUserInput();
            return config.toConfiguration(properties);
        }
    }
    
}
