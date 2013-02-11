package ru.runa.gpd.extension.handler;

import java.util.List;
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

import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.LocalizationRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;

public abstract class ParamBasedProvider extends DelegableProvider {
    protected abstract ParamDefConfig getParamConfig(Delegable delegable);

    protected ImageDescriptor getLogo() {
        return SharedImages.getImageDescriptor("/icons/logo.gif");
    }

    @Override
    public String showConfigurationDialog(Delegable delegable) {
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        ParamDefConfig config = getParamConfig(delegable);
        ConfigurationWizardPage page = new ConfigurationWizardPage(definition.getVariablesWithSwimlanes(), config.parseConfiguration(delegable.getDelegationConfiguration()),
                config, LocalizationRegistry.getLabel(delegable.getDelegationClassName()));
        final ConfigurationWizard wizard = new ConfigurationWizard(page);
        WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard) {
            @Override
            protected void createButtonsForButtonBar(Composite parent) {
                Button copyButton = createButton(parent, 197, Localization.getString("button.copy"), false);
                copyButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Clipboard clipboard = new Clipboard(Display.getCurrent());
                        clipboard.setContents(new String[] { wizard.getWizardPage().getConfiguration() }, new Transfer[] { TextTransfer.getInstance() });
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
            setWindowTitle(Localization.getString("property.delegation.configuration"));
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
        private final List<Variable> variables;
        private final Map<String, String> properties;

        protected ConfigurationWizardPage(List<Variable> variables, Map<String, String> properties, ParamDefConfig config, String headerText) {
            super("config", headerText, getLogo());
            this.variables = variables;
            this.properties = properties;
            this.config = config;
        }

        @Override
        public void createControl(Composite parent) {
            paramDefComposite = new ParamDefComposite(parent, config, properties, variables);
            setControl(paramDefComposite);
            paramDefComposite.createUI();
        }

        public String getConfiguration() {
            Map<String, String> properties = paramDefComposite.readUserInput();
            return config.toConfiguration(properties);
        }
    }
}
