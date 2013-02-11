package ru.runa.gpd.office.word;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.office.resource.Messages;

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
    protected Composite createConstructorView(Composite parent) {
        return new ConstructorView(parent, SWT.NONE);
    }

    @Override
    protected String getTitle() {
        return Messages.getString("MSWordConfig.title");
    }

    private class ConstructorView extends Composite implements Observer {
        private final HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());

        public ConstructorView(Composite parent, int style) {
            super(parent, style);
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

        private GridData get2GridData() {
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            return data;
        }

        private void addRootSection() {
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Messages.getString("MSWordConfig.label.templatePath"));
                final Text text = new Text(this, SWT.BORDER);
                text.setLayoutData(get2GridData());
                text.setText(model.getTemplatePath());
                text.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent event) {
                        model.setTemplatePath(text.getText());
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Messages.getString("MSWordConfig.label.resultFileName"));
                final Text text = new Text(this, SWT.BORDER);
                text.setLayoutData(get2GridData());
                text.setText(model.getResultFileName());
                text.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent event) {
                        model.setResultFileName(text.getText());
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Messages.getString("MSWordConfig.label.resultVariableName"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    if ("ru.runa.wf.web.forms.format.FileFormat".equals(entry.getValue()) || "file".equals(entry.getValue())) {
                        combo.add(entry.getKey());
                    }
                }
                combo.setLayoutData(get2GridData());
                combo.setText(model.getResultVariableName());
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
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
            Composite strokeComposite = new Composite(composite, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            strokeComposite.setLayoutData(data);
            strokeComposite.setLayout(new GridLayout(4, false));
            Label strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            data = new GridData();
            data.widthHint = 50;
            strokeLabel.setLayoutData(data);
            Label headerLabel = new Label(strokeComposite, SWT.NONE);
            headerLabel.setText(Messages.getString("MSWordConfig.label.mappings"));
            strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            strokeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            Hyperlink hl2 = new Hyperlink(strokeComposite, SWT.NONE);
            hl2.setText(Localization.getString("button.add"));
            hl2.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.addMapping();
                }
            });
            hyperlinkGroup.add(hl2);
            return composite;
        }

        private void addParamSection(Composite parent, final MSWordVariableMapping mapping, final int index) {
            final Combo combo = new Combo(parent, SWT.READ_ONLY);
            for (String variableName : variables.keySet()) {
                combo.add(variableName);
            }
            combo.setText(mapping.getVariableName());
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String variableName = combo.getText();
                    mapping.setVariableName(variableName, variables.get(variableName));
                }
            });
            final Text text = new Text(parent, SWT.BORDER);
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.setText(mapping.getBookmarkName());
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent event) {
                    mapping.setBookmarkName(text.getText());
                }
            });
            Hyperlink hl1 = new Hyperlink(parent, SWT.NONE);
            hl1.setText("[X]");
            hl1.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.deleteMapping(index);
                }
            });
            hyperlinkGroup.add(hl1);
        }
    }
}
