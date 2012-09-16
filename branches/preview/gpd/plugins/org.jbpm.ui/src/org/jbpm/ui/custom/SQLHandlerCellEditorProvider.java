package ru.runa.bpm.ui.custom;

import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.custom.SQLTasksModel.SQLQueryModel;
import ru.runa.bpm.ui.custom.SQLTasksModel.SQLQueryParameterModel;
import ru.runa.bpm.ui.custom.SQLTasksModel.SQLTaskModel;
import ru.runa.bpm.ui.resource.Messages;

public class SQLHandlerCellEditorProvider extends XmlBasedConstructorProvider<SQLTasksModel> {

    private static final List<String> PREDEFINED_VARIABLES = new ArrayList<String>();
    static {
        PREDEFINED_VARIABLES.add("instanceId");
        PREDEFINED_VARIABLES.add("currentDate");
    }

    @Override
    protected SQLTasksModel createDefault() {
        return SQLTasksModel.createDefault();
    }

    @Override
    protected SQLTasksModel fromXml(String xml) throws Exception {
        return SQLTasksModel.fromXml(xml);
    }

    @Override
    protected Composite createConstructorView(Composite parent) {
        return new ConstructorView(parent, SWT.NONE);
    }

    @Override
    protected String getTitle() {
        return Messages.getString("SQLActionHandlerConfig.title");
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
                if (model.tasks.size() > 0) {
                    addTaskSection(model.getFirstTask());
                }
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
            } catch (Throwable e) {
                DesignerLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        private void addTaskSection(SQLTaskModel taskModel) {
            Label label = new Label(this, SWT.NONE);
            label.setText(Messages.getString("label.DataSourceName"));
            final Text text = new Text(this, SWT.BORDER);
            text.setText(taskModel.dsName);
            text.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent event) {
                    model.getFirstTask().dsName = text.getText();
                }
            });
            GridData data = new GridData();
            data.widthHint = 200;
            text.setLayoutData(data);
            Hyperlink hl = new Hyperlink(this, SWT.NONE);
            hl.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
            hl.setText(Messages.getString("button.add") + " " + Messages.getString("label.SQLQuery"));
            hl.addHyperlinkListener(new HyperlinkAdapter() {

                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.getFirstTask().addQuery();
                }
            });
            hyperlinkGroup.add(hl);
            for (SQLQueryModel queryModel : taskModel.queries) {
                addQuerySection(queryModel, taskModel.queries.indexOf(queryModel));
            }
        }

        private void addQuerySection(SQLQueryModel queryModel, final int queryIndex) {
            Group group = new Group(this, SWT.NONE);
            group.setData(queryIndex);
            group.setText(Messages.getString("label.SQLQuery"));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            group.setLayoutData(data);
            group.setLayout(new GridLayout(2, false));

            final Text text = new Text(group, SWT.BORDER);
            text.setText(queryModel.query);
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent event) {
                    model.getFirstTask().queries.get(queryIndex).query = text.getText();
                }
            });

            Hyperlink hl1 = new Hyperlink(group, SWT.NONE);
            hl1.setText("[X]");
            hl1.addHyperlinkListener(new HyperlinkAdapter() {

                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.getFirstTask().deleteQuery(queryIndex);
                }
            });
            hyperlinkGroup.add(hl1);

            Composite paramsComposite = createParametersComposite(group, "label.SQLParams", queryIndex, false);
            for (SQLQueryParameterModel parameterModel : queryModel.params) {
                addParamSection(paramsComposite, parameterModel, queryIndex, queryModel.params.indexOf(parameterModel), true);
            }
            Composite resultsComposite = createParametersComposite(group, "label.SQLResults", queryIndex, true);
            for (SQLQueryParameterModel parameterModel : queryModel.results) {
                addParamSection(resultsComposite, parameterModel, queryIndex, queryModel.results.indexOf(parameterModel), false);
            }
        }

        private Composite createParametersComposite(Composite parent, String labelKey, final int queryIndex, final boolean result) {
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
            headerLabel.setText(Messages.getString(labelKey));

            strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            strokeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Hyperlink hl2 = new Hyperlink(strokeComposite, SWT.NONE);
            hl2.setText(Messages.getString("button.add"));
            hl2.addHyperlinkListener(new HyperlinkAdapter() {

                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.getFirstTask().addQueryParameter(queryIndex, result);
                }
            });
            hyperlinkGroup.add(hl2);
            return composite;
        }

        private void addParamSection(Composite parent, final SQLQueryParameterModel parameterModel, final int queryIndex, final int paramIndex,
                boolean input) {
            final Combo combo = new Combo(parent, SWT.READ_ONLY);
            List<String> vars = definition.getVariableNames(true);
            for (String variableName : vars) {
                combo.add(variableName);
            }
            if (input) {
                for (String variableName : PREDEFINED_VARIABLES) {
                    combo.add(variableName);
                }
            }
            combo.setText(parameterModel.varName);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            combo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    parameterModel.varName = combo.getText();
                    parameterModel.swimlaneVar = definition.getSwimlaneByName(parameterModel.varName) != null;
                }
            });

            if (paramIndex != 0) {
                Hyperlink hl0 = new Hyperlink(parent, SWT.NONE);
                hl0.setText(Messages.getString("button.up"));
                hl0.addHyperlinkListener(new HyperlinkAdapter() {

                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        model.getFirstTask().moveUpQueryParameter(queryIndex, parameterModel.result, paramIndex);
                    }
                });
                hyperlinkGroup.add(hl0);
            } else {
                new Label(parent, SWT.NONE);
            }

            Hyperlink hl1 = new Hyperlink(parent, SWT.NONE);
            hl1.setText("[X]");
            hl1.addHyperlinkListener(new HyperlinkAdapter() {

                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.getFirstTask().deleteQueryParameter(queryIndex, parameterModel.result, paramIndex);
                }
            });
            hyperlinkGroup.add(hl1);
        }

    }

}
