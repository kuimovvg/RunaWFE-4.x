package ru.runa.gpd.extension.handler;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.State;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;

public class SendEmailActionHandlerProvider extends DelegableProvider {
    @Override
    public String showConfigurationDialog(Delegable delegable) {
        final EmailConfigWizardPage wizardPage = new EmailConfigWizardPage(bundle, delegable);
        final ConfigurationWizard wizard = new ConfigurationWizard(wizardPage);
        WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard) {
            @Override
            protected void createButtonsForButtonBar(Composite parent) {
                Button testButton = createButton(parent, 101, Localization.getString("EmailDialog.test.button"), false);
                testButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        final String cfg = wizardPage.generateCode();
                        final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
                        monitorDialog.setCancelable(true);
                        final IRunnableWithProgress runnable = new IRunnableWithProgress() {
                            @Override
                            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                try {
                                    monitor.beginTask(Localization.getString("EmailDialog.test.button"), 1);
                                    EmailConfig config = EmailConfigParser.parse(cfg);
                                    EmailUtils.sendMessage(config, null);
                                } catch (Exception e) {
                                    throw new InvocationTargetException(e);
                                } finally {
                                    monitor.done();
                                }
                            }
                        };
                        try {
                            monitorDialog.run(true, false, runnable);
                            setMessage(Localization.getString("EmailDialog.test.success"));
                        } catch (InvocationTargetException ex) {
                            PluginLogger.logError(Localization.getString("EmailDialog.test.error"), ex.getTargetException());
                        } catch (InterruptedException ex) {
                        }
                    }
                });
                Button copyButton = createButton(parent, 197, Localization.getString("button.copy"), false);
                copyButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Clipboard clipboard = new Clipboard(Display.getCurrent());
                        clipboard.setContents(new String[] { wizardPage.generateCode() }, new Transfer[] { TextTransfer.getInstance() });
                        clipboard.dispose();
                    }
                });
                super.createButtonsForButtonBar(parent);
            }

            @Override
            protected Point getInitialSize() {
                return new Point(700, 500);
            }
        };
        if (wizardDialog.open() == IDialogConstants.OK_ID) {
            return wizardPage.getResult();
        }
        return null;
    }

    @Override
    public boolean validateValue(Delegable delegable) {
        String configuration = delegable.getDelegationConfiguration();
        try {
            if (configuration.trim().length() > 0) {
                EmailConfig config = EmailConfigParser.parse(configuration, false);
                GraphElement parent = ((GraphElement) delegable).getParent();
                if (config.isUseMessageFromTaskForm() && (!(parent instanceof State))) {
                    return false;
                }
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("invalid Email config " + configuration, e);
            return false;
        }
        return true;
    }

    public class ConfigurationWizard extends Wizard {
        private final EmailConfigWizardPage wizardPage;

        public ConfigurationWizard(EmailConfigWizardPage wizardPage) {
            this.wizardPage = wizardPage;
            setNeedsProgressMonitor(true);
            setWindowTitle(Localization.getString("property.delegation.configuration"));
        }

        @Override
        public void addPages() {
            addPage(wizardPage);
        }

        @Override
        public boolean performFinish() {
            wizardPage.generateCode();
            return true;
        }
    }
}
