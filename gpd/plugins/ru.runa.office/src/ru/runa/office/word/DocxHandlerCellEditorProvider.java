package ru.runa.office.word;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import ru.runa.bpm.ui.common.model.Variable;
import ru.runa.bpm.ui.custom.XmlBasedConstructorProvider;

import ru.runa.office.FilesSupplierMode;
import ru.runa.office.InputOutputComposite;
import ru.runa.office.resource.Messages;

public class DocxHandlerCellEditorProvider extends XmlBasedConstructorProvider<DocxModel> {

    @Override
    protected Composite createConstructorView(Composite parent) {
        return new ConstructorView(parent);
    }

    @Override
    protected boolean isValidateByXSD() {
        return false;
    }

    @Override
    protected String getTitle() {
        return Messages.getString("DocxActionHandlerConfig.title");
    }

    private class ConstructorView extends Composite implements Observer {

        private final HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());

        public ConstructorView(Composite parent) {
            super(parent, SWT.None);
            setLayout(new GridLayout(2, false));
            buildFromModel();
        }

        public void update(Observable arg0, Object arg1) {
            buildFromModel();
        }

        public void buildFromModel() {
            try {
                for (Control control : getChildren()) {
                    control.dispose();
                }
                final Button strict = new Button(this, SWT.CHECK);
                strict.setText(Messages.getString("label.strict"));
                strict.setSelection(model.isStrict());
                strict.addSelectionListener(new SelectionListener() {

                    public void widgetDefaultSelected(SelectionEvent arg0) {
                        model.setStrict(strict.getSelection());
                    }

                    public void widgetSelected(SelectionEvent arg0) {
                        model.setStrict(strict.getSelection());
                    }
                });
                Hyperlink addTableLink = new Hyperlink(this, SWT.NONE);
                addTableLink.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                addTableLink.setText(Messages.getString("label.AddTable"));
                addTableLink.addHyperlinkListener(new HyperlinkAdapter() {

                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        model.addNewTable(DocxTableModel.createTable());
                        buildFromModel();
                    }
                });
                hyperlinkGroup.add(addTableLink);
                new InputOutputComposite(this, model.getInOutModel(), definition, FilesSupplierMode.BOTH);
                int i = 0;
                for (DocxTableModel table : model.tables) {
                    addTableSection(table, i++);
                }
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                this.layout(true, true);
            } catch (Throwable e) {
                DesignerLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        private void addTableSection(DocxTableModel tableModel, final int tableIndex) {

            Group group = new Group(this, SWT.NONE);
            group.setData(tableIndex);
            group.setText(Messages.getString("label.Table"));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            group.setLayoutData(data);
            group.setLayout(new GridLayout(2, false));

            final Text text = new Text(group, SWT.BORDER);
            text.setText(tableModel.name);

            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent event) {
                    model.tables.get(tableIndex).name = text.getText();
                }
            });

            Hyperlink hl1 = new Hyperlink(group, SWT.NONE);
            hl1.setText("[X]");
            hl1.addHyperlinkListener(new HyperlinkAdapter() {

                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.tables.remove(tableIndex);
                    buildFromModel();
                }
            });
            hyperlinkGroup.add(hl1);

            Group pgroup = new Group(group, SWT.None);
            GridData pdata = new GridData(GridData.FILL_HORIZONTAL);
            pdata.horizontalSpan = 3;
            pgroup.setLayoutData(pdata);
            pgroup.setLayout(new GridLayout(2, false));

            Label l = new Label(pgroup, SWT.None);
            l.setText(Messages.getString("label.tableStyle"));
            final Text styleText = new Text(pgroup, SWT.BORDER);
            styleText.setText(tableModel.getStyleName());

            styleText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            styleText.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent event) {
                    model.tables.get(tableIndex).styleName = styleText.getText();
                }
            });

            final Button addBreak = new Button(pgroup, SWT.CHECK);
            addBreak.setText(Messages.getString("label.tableAddBreak"));
            addBreak.setSelection(model.tables.get(tableIndex).addBreak);
            addBreak.addSelectionListener(new SelectionListener() {

                public void widgetDefaultSelected(SelectionEvent arg0) {
                    model.tables.get(tableIndex).addBreak = addBreak.getSelection();
                }

                public void widgetSelected(SelectionEvent arg0) {
                    model.tables.get(tableIndex).addBreak = addBreak.getSelection();
                }
            });

            Composite columnsComposite = createParametersComposite(group, "label.DocxTableColumns", tableIndex);
            for (DocxColumnModel columnModel : tableModel.columns) {
                addColumnSection(columnsComposite, columnModel, tableIndex, tableModel.columns.indexOf(columnModel));
            }
        }

        private Composite createParametersComposite(Composite parent, String labelKey, final int tableIndex) {
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
            hl2.setText(ru.runa.bpm.ui.resource.Messages.getString("button.add"));
            hl2.addHyperlinkListener(new HyperlinkAdapter() {

                @Override
                public void linkActivated(HyperlinkEvent e) {
                    model.tables.get(tableIndex).addColumn();
                    buildFromModel();
                }
            });
            hyperlinkGroup.add(hl2);
            return composite;
        }

        private void addColumnSection(Composite parent, final DocxColumnModel columnModel, final int tableIndex, final int columnIndex) {
            final Combo combo = new Combo(parent, SWT.READ_ONLY);
            List<Variable> vars = definition.getVariablesList();
            for (Variable variable : vars) {
                if (variable.getFormat().equals("ru.runa.wf.web.forms.format.ArrayListFormat")) {
                    combo.add(variable.getName());
                }
            }
            try {
                combo.setText(columnModel.variable);
            } catch (Exception e) {
            }
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            combo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    columnModel.variable = combo.getText();
                }
            });

            if (columnIndex != 0) {
                Hyperlink hl0 = new Hyperlink(parent, SWT.NONE);
                hl0.setText(ru.runa.bpm.ui.resource.Messages.getString("button.up"));
                hl0.addHyperlinkListener(new HyperlinkAdapter() {

                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        model.tables.get(tableIndex).moveUpColumn(columnIndex);
                        buildFromModel();
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
                    model.tables.get(tableIndex).columns.remove(columnIndex);
                    buildFromModel();
                }
            });
            hyperlinkGroup.add(hl1);
        }

    }

    @Override
    protected DocxModel createDefault() {
        DocxModel model = new DocxModel();
        return model;
    }

    @Override
    protected DocxModel fromXml(String xml) throws Exception {
        return DocxModel.fromXml(xml);
    }

}
