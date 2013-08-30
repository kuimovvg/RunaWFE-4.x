package ru.runa.gpd.office.word;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.office.resource.Messages;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.wfe.var.FileVariable;

public class MSWordTaskHandlerConfigurator extends XmlBasedConstructorProvider<MSWordConfig> {
    @Override
    protected MSWordConfig createDefault() {
        return new MSWordConfig();
    }

    @Override
    protected MSWordConfig fromXml(String xml) throws Exception {
        return MSWordConfig.fromXml(xml);
    }

    @Override
    protected Composite createConstructorView(Composite parent, Delegable delegable) {
        return new ConstructorView(parent, delegable);
    }

    @Override
    protected String getTitle() {
        return Messages.getString("MSWordConfig.title");
    }

    private class ConstructorView extends Composite implements Observer {
        private final HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());
        private final Delegable delegable;

        public ConstructorView(Composite parent, Delegable delegable) {
            super(parent, SWT.NONE);
            this.delegable = delegable;
            setLayout(new GridLayout(3, false));
            buildFromModel();
        }

        @Override
        public void update(Observable o, Object arg) {
            buildFromModel();
        }

        public void buildFromModel() {
            try {
                for (Control control : getChildren()) {
                    control.dispose();
                }
                addRootSection();
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
            } catch (Throwable e) {
                PluginLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        private GridData getGridData(int horizontalSpan) {
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = horizontalSpan;
            return data;
        }

        private void addRootSection() {
            {
                final Button strict = new Button(this, SWT.CHECK);
                strict.setLayoutData(getGridData(3));
                strict.setText(Messages.getString("label.strict"));
                strict.setSelection(model.isStrictMode());
                strict.addSelectionListener(new LoggingSelectionAdapter() {
                    
                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        model.setStrictMode(strict.getSelection());
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Messages.getString("MSWordConfig.label.templatePath"));
                final Text text = new Text(this, SWT.BORDER);
                text.setLayoutData(getGridData(2));
                text.setText(model.getTemplatePath());
                text.addModifyListener(new LoggingModifyTextAdapter() {
                    
                    @Override
                    protected void onTextChanged(ModifyEvent e) throws Exception {
                        model.setTemplatePath(text.getText());
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Messages.getString("MSWordConfig.label.resultFileName"));
                final Text text = new Text(this, SWT.BORDER);
                text.setLayoutData(getGridData(2));
                text.setText(model.getResultFileName());
                text.addModifyListener(new LoggingModifyTextAdapter() {
                    
                    @Override
                    protected void onTextChanged(ModifyEvent e) throws Exception {
                        model.setResultFileName(text.getText());
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Messages.getString("MSWordConfig.label.resultVariableName"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                for (String variableName : delegable.getVariableNames(true, FileVariable.class.getName())) {
                    combo.add(variableName);
                }
                combo.setLayoutData(getGridData(2));
                combo.setText(model.getResultVariableName());
                combo.addSelectionListener(new LoggingSelectionAdapter() {
                    
                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        model.setResultVariableName(combo.getText());
                    }
                });
            }
            Composite paramsComposite = createParametersComposite(this);
            int index = 0;
            for (MSWordVariableMapping mapping : model.getMappings()) {
                addParamSection(paramsComposite, mapping, index);
                index++;
            }
        }

        private Composite createParametersComposite(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(3, false));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            composite.setLayoutData(data);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            Composite strokeComposite = SWTUtils.createStrokeComposite(composite, data, Messages.getString("MSWordConfig.label.mappings"), 4);
            Hyperlink hl2 = new Hyperlink(strokeComposite, SWT.NONE);
            hl2.setText(Localization.getString("button.add"));
            hl2.addHyperlinkListener(new LoggingHyperlinkAdapter() {
                
                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.addMapping();
                }
            });
            hyperlinkGroup.add(hl2);
            return composite;
        }

        private void addParamSection(Composite parent, final MSWordVariableMapping mapping, final int index) {
            final Combo combo = new Combo(parent, SWT.READ_ONLY);
            for (String variableName : delegable.getVariableNames(true)) {
                combo.add(variableName);
            }
            combo.setText(mapping.getVariableName());
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            combo.addSelectionListener(new LoggingSelectionAdapter() {
                
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    String variableName = combo.getText();
                    mapping.setVariableName(variableName);
                }
            });
            final Text text = new Text(parent, SWT.BORDER);
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.setText(mapping.getBookmarkName());
            text.addModifyListener(new LoggingModifyTextAdapter() {
                
                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    mapping.setBookmarkName(text.getText());
                }
            });
            Hyperlink hl1 = new Hyperlink(parent, SWT.NONE);
            hl1.setText("[X]");
            hl1.addHyperlinkListener(new LoggingHyperlinkAdapter() {
                
                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    model.deleteMapping(index);
                }
            });
            hyperlinkGroup.add(hl1);
        }
    }
}
