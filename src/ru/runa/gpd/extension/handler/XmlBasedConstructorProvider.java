package ru.runa.gpd.extension.handler;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.XmlHighlightTextStyling;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class XmlBasedConstructorProvider<T extends Observable> extends DelegableProvider {
    protected T model;
    protected boolean formalVariable = false;
    protected Map<String, String> variables = Maps.newHashMap();
    protected List<String> swimlaneNames = Lists.newArrayList();

    @Override
    public String showConfigurationDialog(Delegable delegable) {
        this.variables.clear();
        this.swimlaneNames.clear();
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        for (Variable variable : definition.getVariables()) {
            VariableFormatArtifact artifact = VariableFormatRegistry.getInstance().getArtifactNotNull(variable.getFormat());
            variables.put(variable.getName(), artifact.getVariableClassName());
        }
        for (Swimlane swimlane : definition.getSwimlanes()) {
            swimlaneNames.add(swimlane.getName());
        }
        XmlBasedConstructorDialog dialog = new XmlBasedConstructorDialog(delegable.getDelegationConfiguration());
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    @Override
    public String showConfigurationDialog(String delegationConfiguration, Map<String, String> variables) {
        XmlBasedConstructorDialog dialog = new XmlBasedConstructorDialog(delegationConfiguration);
        formalVariable = true;
        this.variables.clear();
        this.swimlaneNames.clear();
        this.variables.putAll(variables);
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    @Override
    public boolean validateValue(Delegable delegable) {
        try {
            fromXml(delegable.getDelegationConfiguration());
            return true;
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog(getClass() + ": invalid configuration", e);
            return false;
        }
    }

    protected abstract String getTitle();

    protected abstract Composite createConstructorView(Composite parent);

    protected abstract T createDefault();

    protected abstract T fromXml(String xml) throws Exception;

    protected int getSelectedTabIndex() {
        return 0;
    }

    private class XmlBasedConstructorDialog extends Dialog {
        private TabFolder tabFolder;
        private XmlContentView xmlContentView;
        private Composite constructorView;
        private final String initialValue;
        private String result;

        public XmlBasedConstructorDialog(String initialValue) {
            super(Display.getCurrent().getActiveShell());
            setShellStyle(getShellStyle() | SWT.RESIZE);
            this.initialValue = initialValue;
        }

        @Override
        protected Point getInitialSize() {
            return new Point(600, 400);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            getShell().setText(getTitle());
            tabFolder = new TabFolder(parent, SWT.BORDER);
            tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
            Composite composite = new Composite(tabFolder, SWT.NONE);
            composite.setLayout(new GridLayout());
            TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
            tabItem1.setText(Localization.getString("SQLActionHandlerConfig.title.configuration"));
            tabItem1.setControl(composite);
            ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.BORDER);
            scrolledComposite.setExpandHorizontal(true);
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            try {
                if (initialValue.trim().length() != 0) {
                    model = fromXml(initialValue);
                } else {
                    model = createDefault();
                }
            } catch (Exception ex) {
                PluginLogger.logError(Localization.getString("config.error.parse"), ex);
                model = createDefault();
            }
            constructorView = createConstructorView(scrolledComposite);
            constructorView.setLayoutData(new GridData(GridData.FILL_BOTH));
            if (constructorView instanceof Observer) {
                model.addObserver((Observer) constructorView);
            }
            scrolledComposite.setContent(constructorView);
            xmlContentView = new XmlContentView(tabFolder, SWT.NONE);
            xmlContentView.setLayoutData(new GridData(GridData.FILL_BOTH));
            xmlContentView.setValue(initialValue);
            TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
            tabItem2.setText(" XML ");
            tabItem2.setControl(xmlContentView);
            tabFolder.setSelection(getSelectedTabIndex());
            tabFolder.addSelectionListener(new TabSelectionHandler());
            return tabFolder;
        }

        @Override
        protected void okPressed() {
            if (tabFolder.getSelectionIndex() == 0) {
                populateToSourceView();
            }
            if (xmlContentView.validate()) {
                this.result = xmlContentView.getValue();
                super.okPressed();
            }
        }

        private void populateToConstructorView() {
            try {
                String xml = xmlContentView.getValue();
                model = fromXml(xml);
                if (constructorView instanceof Observer) {
                    Observer observer = (Observer) constructorView;
                    model.addObserver(observer);
                    // constructorView.buildFromModel();
                    observer.update(null, null);
                    constructorView.layout(true, true);
                }
            } catch (Exception ex) {
                PluginLogger.logError("Unable to parse model from XML", ex);
            }
        }

        private void populateToSourceView() {
            xmlContentView.setValue(model.toString());
        }

        public String getResult() {
            return result;
        }

        private class TabSelectionHandler extends SelectionAdapter {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (tabFolder.getSelectionIndex() == 0) {
                    if (xmlContentView.validate()) {
                        populateToConstructorView();
                    } else {
                        // return to source view due to invalid xml
                        tabFolder.setSelection(1);
                    }
                } else {
                    populateToSourceView();
                }
            }
        }
    }

    public static class XmlContentView extends Composite {
        private final StyledText styledText;
        private final Label errorLabel;

        public XmlContentView(Composite parent, int style) {
            super(parent, style);
            setLayout(new GridLayout());
            errorLabel = new Label(this, SWT.NONE);
            errorLabel.setForeground(ColorConstants.red);
            errorLabel.setText("");
            errorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            styledText = new StyledText(this, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
            styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
            XmlHighlightTextStyling lineStyleListener = new XmlHighlightTextStyling();
            styledText.addLineStyleListener(lineStyleListener);
            styledText.setLineSpacing(2);
        }

        private void setErrorLabelText(String text) {
            errorLabel.setText(text);
            errorLabel.pack(true);
        }

        public boolean validate() {
            try {
                String xml = styledText.getText();
                XmlUtil.parseWithoutValidation(xml);
                return true;
            } catch (Exception e) {
                setErrorLabelText(e.getMessage());
            }
            return false;
        }

        public void setValue(String value) {
            styledText.setText(value);
        }

        public String getValue() {
            return styledText.getText();
        }
    }
}
