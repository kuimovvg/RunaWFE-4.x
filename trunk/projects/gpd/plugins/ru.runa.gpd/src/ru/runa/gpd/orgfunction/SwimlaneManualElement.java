package ru.runa.gpd.orgfunction;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;

public class SwimlaneManualElement extends SwimlaneGroupElement {
    private Composite paramsComposite;
    private Text classNameText;
    private Combo combo;

    @Override
    public void createGUI(Composite clientArea) {
        createComposite(clientArea, 2);
        Label label = new Label(getClientArea(), SWT.NONE);
        label.setText(Localization.getString("OrgFunction.Type"));
        label.setLayoutData(createLayoutData(1, false));
        combo = new Combo(getClientArea(), SWT.READ_ONLY);
        combo.setVisibleItemCount(10);
        combo.add(OrgFunctionDefinition.DEFAULT.getName());
        List<OrgFunctionDefinition> definitions = OrgFunctionsRegistry.getInstance().getAll();
        for (OrgFunctionDefinition definition : definitions) {
            combo.add(definition.getName());
        }
        if (currentDefinition != null) {
            combo.setText(currentDefinition.getName());
        }
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    String displayName = combo.getItem(combo.getSelectionIndex());
                    if (OrgFunctionDefinition.DEFAULT.getName().equals(displayName)) {
                        currentDefinition = OrgFunctionDefinition.DEFAULT;
                    } else {
                        if (currentDefinition != null && displayName.equals(currentDefinition.getName())) {
                            return;
                        }
                        currentDefinition = OrgFunctionsRegistry.getInstance().getArtifactNotNullByDisplayName(displayName);
                    }
                    if (classNameText != null) {
                        String className = currentDefinition != null ? currentDefinition.getName() : "";
                        classNameText.setText(className);
                    }
                    setOrgFunctionDefinitionName(currentDefinition.getName());
                    updateSwimlane();
                    reloadParametersUI();
                } catch (Exception ex) {
                    PluginLogger.logError(ex);
                }
            }
        });
        combo.setLayoutData(createLayoutData(1, true));
        classNameText = new Text(getClientArea(), SWT.BORDER);
        classNameText.setEditable(false);
        classNameText.setLayoutData(createLayoutData(2, true));
        paramsComposite = new Composite(getClientArea(), SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        gridData.heightHint = 150;
        paramsComposite.setLayoutData(gridData);
        GridLayout layout = new GridLayout(3, false);
        layout.marginLeft = 0;
        layout.marginRight = 1;
        layout.marginWidth = 0;
        paramsComposite.setLayout(layout);
    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionDefinition currentDefinition) {
        super.open(path, swimlaneName, currentDefinition);
        this.currentDefinition = currentDefinition;
        if (currentDefinition != null) {
            combo.setText(currentDefinition.getName());
            classNameText.setText(currentDefinition.getName());
            reloadParametersUI();
        } else {
            combo.select(-1);
        }
    }

    private void reloadParametersUI() {
        for (Control control : paramsComposite.getChildren()) {
            control.dispose();
        }
        for (final OrgFunctionParameter parameter : currentDefinition.getParameters()) {
            String message = Localization.getString(parameter.getName()) + " *:";
            Control control;
            if (parameter.isMultiple()) {
                control = createLink(paramsComposite, message);
                ((Hyperlink) control).addHyperlinkListener(new HyperlinkAdapter() {
                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        currentDefinition.propagateParameter(parameter, 1);
                        updateSwimlane();
                        reloadParametersUI();
                    }
                });
                control.setToolTipText("[+]");
            } else {
                control = new Label(paramsComposite, SWT.NONE);
                ((Label) control).setText(message);
            }
            control.setLayoutData(createLayoutData(1, false));
            if (parameter.isTransientParam()) {
                Hyperlink linkDelete = createLink(paramsComposite, "[-]");
                linkDelete.addHyperlinkListener(new HyperlinkAdapter() {
                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        currentDefinition.removeParameter(parameter);
                        updateSwimlane();
                        reloadParametersUI();
                    }
                });
                GridData td = createLayoutData(1, false);
                td.widthHint = 20;
                linkDelete.setLayoutData(td);
            }
            final Text text = new Text(paramsComposite, SWT.BORDER);
            text.setText(parameter.getValue());
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    try {
                        parameter.setValue(text.getText());
                        updateSwimlane();
                        text.setBackground(ColorConstants.white);
                    } catch (NumberFormatException e1) {
                        text.setBackground(ColorConstants.cyan);
                    }
                }
            });
            text.setLayoutData(createLayoutData(parameter.isTransientParam() ? 1 : 2, true));
        }
        paramsComposite.redraw();
        paramsComposite.layout(true, true);
    }

    private void updateSwimlane() {
        if (currentDefinition != null) {
            boolean fireEvent = true;
            for (OrgFunctionParameter parameter : currentDefinition.getParameters()) {
                if (parameter.getValue().length() == 0) {
                    fireEvent = false;
                }
            }
            if (fireEvent) {
                fireCompletedEvent(currentDefinition);
            }
        }
    }

    private GridData createLayoutData(int numColumns, boolean fillGrab) {
        GridData td = new GridData(fillGrab ? GridData.FILL_HORIZONTAL : GridData.CENTER);
        td.horizontalSpan = numColumns;
        return td;
    }
}
