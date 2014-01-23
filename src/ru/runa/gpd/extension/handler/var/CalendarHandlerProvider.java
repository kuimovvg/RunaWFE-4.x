package ru.runa.gpd.extension.handler.var;

import java.util.Date;
import java.util.List;
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
import org.eclipse.swt.widgets.Button;
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
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.ui.custom.InsertVariableTextMenuDetectListener;
import ru.runa.gpd.util.Duration;
import ru.runa.wfe.var.format.LongFormat;

public class CalendarHandlerProvider extends XmlBasedConstructorProvider<CalendarConfig> {
    private static String[] dateFormats = new String[] { Date.class.getName() };
    private static String[] setFormats = new String[] { Date.class.getName(), LongFormat.class.getName() };

    @Override
    protected CalendarConfig createDefault() {
        return new CalendarConfig();
    }

    @Override
    protected CalendarConfig fromXml(String xml) throws Exception {
        return CalendarConfig.fromXml(xml);
    }

    @Override
    protected Composite createConstructorView(Composite parent, Delegable delegable) {
        return new ConstructorView(parent, delegable);
    }

    @Override
    protected String getTitle() {
        return Localization.getString("ru.runa.wfe.extension.handler.var.CreateCalendarHandler");
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

        private GridData get2GridData() {
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            return data;
        }

        private void addRootSection() {
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Localization.getString("property.duration.baseDate"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                combo.add(Duration.CURRENT_DATE_MESSAGE);
                for (String variableName : delegable.getVariableNames(false, dateFormats)) {
                    combo.add(variableName);
                }
                combo.setLayoutData(get2GridData());
                if (model.getBaseVariableName() != null) {
                    combo.setText(model.getBaseVariableName());
                } else {
                    combo.setText(Duration.CURRENT_DATE_MESSAGE);
                }
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        model.setBaseVariableName(combo.getText());
                        if (Duration.CURRENT_DATE_MESSAGE.equals(model.getBaseVariableName())) {
                            model.setBaseVariableName(null);
                        }
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Localization.getString("ParamBasedProvider.result"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                for (String variableName : delegable.getVariableNames(false, dateFormats)) {
                    combo.add(variableName);
                }
                combo.setLayoutData(get2GridData());
                combo.setText(model.getOutVariableName());
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        model.setOutVariableName(combo.getText());
                    }
                });
            }
            Composite paramsComposite = createParametersComposite(this);
            int index = 0;
            for (CalendarOperation operation : model.getMappings()) {
                addOperationSection(paramsComposite, operation, index);
                index++;
            }
        }

        private Composite createParametersComposite(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(5, false));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 5;
            composite.setLayoutData(data);
            Composite strokeComposite = new Composite(composite, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 5;
            strokeComposite.setLayoutData(data);
            strokeComposite.setLayout(new GridLayout(5, false));
            Label strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            data = new GridData();
            data.widthHint = 50;
            strokeLabel.setLayoutData(data);
            Label headerLabel = new Label(strokeComposite, SWT.NONE);
            headerLabel.setText(Localization.getString("label.operations"));
            strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            strokeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            Hyperlink hl1 = new Hyperlink(strokeComposite, SWT.NONE);
            hl1.setText(Localization.getString("button.add"));
            hl1.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.addOperation(CalendarOperation.ADD);
                }
            });
            hyperlinkGroup.add(hl1);
            Hyperlink hl2 = new Hyperlink(strokeComposite, SWT.NONE);
            hl2.setText(Localization.getString("button.set"));
            hl2.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.addOperation(CalendarOperation.SET);
                }
            });
            hyperlinkGroup.add(hl2);
            return composite;
        }

        private void addOperationSection(Composite parent, final CalendarOperation operation, final int index) {
            {
                final Button checkBusinessTimeButton = new Button(parent, SWT.CHECK);
                checkBusinessTimeButton.setText(Localization.getString("label.businessTime"));
                checkBusinessTimeButton.setEnabled(CalendarOperation.ADD.equals(operation.getType()));
                checkBusinessTimeButton.setSelection(operation.isBusinessTime());
                checkBusinessTimeButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        operation.setBusinessTime(checkBusinessTimeButton.getSelection());
                    }
                });
            }
            {
                final Combo combo = new Combo(parent, SWT.READ_ONLY);
                for (String fieldName : CalendarConfig.FIELD_NAMES) {
                    combo.add(fieldName);
                }
                combo.setText(operation.getFieldName());
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        operation.setFieldName(combo.getText());
                    }
                });
            }
            final Label label = new Label(parent, SWT.NONE);
            label.setText(operation.getType());
            {
                final Text text = new Text(parent, SWT.BORDER);
                text.setText(operation.getExpression());
                text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                text.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent e) {
                        operation.setExpression(text.getText());
                    }
                });
                List<String> variableNames = delegable.getVariableNames(false, setFormats);
                new InsertVariableTextMenuDetectListener(text, variableNames);
            }
            Hyperlink hl1 = new Hyperlink(parent, SWT.NONE);
            hl1.setText("[X]");
            hl1.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.deleteOperation(index);
                }
            });
            hyperlinkGroup.add(hl1);
        }
    }
}
