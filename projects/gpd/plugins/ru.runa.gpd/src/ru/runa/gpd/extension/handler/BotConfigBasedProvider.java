package ru.runa.gpd.extension.handler;

import java.util.HashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.BotTaskConfigHelper;
import ru.runa.gpd.extension.LocalizationRegistry;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class BotConfigBasedProvider extends ConfigBasedProvider {
    public String getConfiguration(Delegable delegable) {
        ParamDefConfig config = ((BotTask) delegable).getParamDefConfig();
        if (config == null || BotTaskConfigHelper.isParamDefConfigEmpty(config)) {
            config = getParamConfig(delegable);
        }
        return config.toConfiguration(new HashMap<String, String>());
    }

    @Override
    public final String showConfigurationDialog(Delegable delegable) {
        ProcessDefinition definition = ((BotTask) delegable).getProcessDefinition();
        ParamDefConfig config = ((BotTask) delegable).getParamDefConfig();
        if (config == null || BotTaskConfigHelper.isParamDefConfigEmpty(config)) {
            config = getParamConfig(delegable);
        }
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
}
