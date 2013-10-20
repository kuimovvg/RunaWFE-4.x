package ru.cg.runaex.components_plugin.property_editor.require_rule;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import ru.cg.runaex.components.bean.component.part.RequireRuleComponentPart;
import ru.cg.runaex.components_plugin.Localization;
import ru.cg.runaex.components_plugin.component_parameter.descriptor.DescriptorLocalizationFactory;
import ru.cg.runaex.components_plugin.util.VariableUtils;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.GroovyActionHandlerProvider;

public class RequireRuleEditDialog extends Dialog {

    private Localization localization;

    private Button requiredBtn;
    private Button notRequiredBtn;
    private Button groovyScriptBtn;
    private Button openGroovyScriptDialogBtn;
    private Button selectBtn;

    private RequireRuleComponentPart requireRule;

    public RequireRuleEditDialog(RequireRuleComponentPart requireRule) {
        super(Display.getCurrent().getActiveShell());
        localization = DescriptorLocalizationFactory.getRequireRuleLocalization();

        try {
            this.requireRule = requireRule.clone();
        } catch (CloneNotSupportedException ex) {
            PluginLogger.logError("Error cloning require rule", ex);
        }
    }

    @Override
    protected Point getInitialSize() {
        return new Point(365, 120);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout areaLayout = new GridLayout(4, false);
        area.setLayout(areaLayout);
        area.setLayoutData(new GridData(GridData.FILL_BOTH));

        notRequiredBtn = new Button(area, SWT.RADIO);
        notRequiredBtn.setText(localization.get("btn.notRequired"));
        notRequiredBtn.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                onItemSelected(event.widget);
            }

        });

        requiredBtn = new Button(area, SWT.RADIO);
        requiredBtn.setText(localization.get("btn.required"));
        requiredBtn.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                onItemSelected(event.widget);
            }

        });
        if (requireRule.isUnconditionallyRequired()) {
            requiredBtn.setSelection(true);
        }

        groovyScriptBtn = new Button(area, SWT.RADIO);
        groovyScriptBtn.setText(localization.get("btn.groovyScript"));
        groovyScriptBtn.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                onItemSelected(event.widget);
            }

        });
        if (requireRule.getGroovyScript() != null) {
            groovyScriptBtn.setSelection(true);
        }

        openGroovyScriptDialogBtn = new Button(area, SWT.PUSH);
        openGroovyScriptDialogBtn.setText("...");
        openGroovyScriptDialogBtn.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                openGroovyScriptEditDialog();
            }

        });

        return area;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        selectBtn = createButton(parent, IDialogConstants.OK_ID, localization.get("dialog.ok"), true);
        createButton(parent, IDialogConstants.CANCEL_ID, localization.get("dialog.cancel"), false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(localization.get("dialog.title"));
    }

    public RequireRuleComponentPart getRequireRule() {
        return requireRule;
    }

    private void onItemSelected(Widget selectedButton) {
        openGroovyScriptDialogBtn.setEnabled(selectedButton.equals(groovyScriptBtn));
        selectBtn.setEnabled(!selectedButton.equals(groovyScriptBtn));

        if (notRequiredBtn.equals(selectedButton)) {
            requireRule.setUnconditionallyRequired(false);
            requireRule.setGroovyScript(null);
        } else if (requiredBtn.equals(selectedButton)) {
            requireRule.setUnconditionallyRequired(true);
            requireRule.setGroovyScript(null);
        } else {
            requireRule.setUnconditionallyRequired(false);
            if (requireRule.getGroovyScript() == null) {
                requireRule.setGroovyScript("");
            }
        }
    }

    private void openGroovyScriptEditDialog() {
        GroovyActionHandlerProvider.ConfigurationDialog dialog = new GroovyActionHandlerProvider.ConfigurationDialog(requireRule.getGroovyScript(), VariableUtils.getVariables());
        dialog.open();

        switch (dialog.getReturnCode()) {
        case Window.OK: {
            selectBtn.setEnabled(true);
            requireRule.setGroovyScript(dialog.getResult());
            break;
        }
        case Window.CANCEL: {
            // Do nothing
            break;
        }
        }
    }

}
