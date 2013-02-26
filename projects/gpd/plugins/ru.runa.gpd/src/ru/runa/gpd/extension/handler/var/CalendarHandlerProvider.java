package ru.runa.gpd.extension.handler.var;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.ui.dialog.ChooseVariableDialog;
import ru.runa.gpd.util.Delay;
import ru.runa.wfe.var.format.LongFormat;

public class CalendarHandlerProvider extends XmlBasedConstructorProvider<CalendarConfig> {
    private static List<String> dateFormats = new ArrayList<String>();
    private static List<String> setFormats = new ArrayList<String>();
    static {
        dateFormats.add(Date.class.getName());
        setFormats.addAll(dateFormats);
        setFormats.add(LongFormat.class.getName());
    }

    @Override
    protected CalendarConfig createDefault() {
        return new CalendarConfig();
    }

    @Override
    protected CalendarConfig fromXml(String xml) throws Exception {
        return CalendarConfig.fromXml(xml);
    }

    @Override
    protected Composite createConstructorView(Composite parent) {
        return new ConstructorView(parent, SWT.NONE);
    }

    @Override
    protected String getTitle() {
        return Localization.getString("ru.runa.wfe.extension.handler.var.CreateCalendarHandler");
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
                label.setText(Localization.getString("property.duration.baseDate"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                combo.add(Delay.CURRENT_DATE_MESSAGE);
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    if (dateFormats.contains(entry.getValue())) {
                        combo.add(entry.getKey());
                    }
                }
                combo.setLayoutData(get2GridData());
                if (model.getBaseVariableName() != null) {
                    combo.setText(model.getBaseVariableName());
                } else {
                    combo.setText(Delay.CURRENT_DATE_MESSAGE);
                }
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        model.setBaseVariableName(combo.getText());
                        if (Delay.CURRENT_DATE_MESSAGE.equals(model.getBaseVariableName())) {
                            model.setBaseVariableName(null);
                        }
                    }
                });
            }
            {
                Label label = new Label(this, SWT.NONE);
                label.setText(Localization.getString("ParamBasedProvider.result"));
                final Combo combo = new Combo(this, SWT.READ_ONLY);
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    if (dateFormats.contains(entry.getValue())) {
                        combo.add(entry.getKey());
                    }
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
            composite.setLayout(new GridLayout(4, false));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
            composite.setLayoutData(data);
            Composite strokeComposite = new Composite(composite, SWT.NONE);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 4;
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
                text.addMenuDetectListener(new MenuDetectListener() {
                    @Override
                    public void menuDetected(MenuDetectEvent e) {
                        if (text.getMenu() == null) {
                            MenuManager menuManager = new MenuManager();
                            Menu menu = menuManager.createContextMenu(getShell());
                            menuManager.add(new Action(Localization.getString("button.insert_variable")) {
                                @Override
                                public void run() {
                                    List<String> variableNames = new ArrayList<String>();
                                    for (Map.Entry<String, String> entry : variables.entrySet()) {
                                        if (setFormats.contains(entry.getValue())) {
                                            variableNames.add(entry.getKey());
                                        }
                                    }
                                    ChooseVariableDialog dialog = new ChooseVariableDialog(variableNames);
                                    String variableName = dialog.openDialog();
                                    if (variableName != null) {
                                        String r = "${" + variableName + "}";
                                        text.setText(r);
                                    }
                                }
                            });
                            text.setMenu(menu);
                        }
                    }
                });
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
