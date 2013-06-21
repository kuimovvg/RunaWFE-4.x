package ru.runa.gpd.office.excel;

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
import org.eclipse.swt.layout.FillLayout;
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

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputComposite;
import ru.runa.gpd.office.resource.Messages;

public abstract class BaseExcelHandlerCellEditorProvider extends XmlBasedConstructorProvider<ExcelModel> {
    protected abstract FilesSupplierMode getMode();

    @Override
    protected Composite createConstructorView(Composite parent) {
        return new ConstructorView(parent);
    }

    @Override
    protected ExcelModel createDefault() {
        return new ExcelModel(getMode());
    }

    @Override
    protected ExcelModel fromXml(String xml) throws Exception {
        return ExcelModel.fromXml(xml, getMode());
    }

    private class ConstructorView extends Composite implements Observer {
        private final HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());

        public ConstructorView(Composite parent) {
            super(parent, SWT.None);
            setLayout(new GridLayout(3, false));
            buildFromModel();
        }

        @Override
        public void update(Observable arg0, Object arg1) {
            buildFromModel();
        }

        public void buildFromModel() {
            try {
                for (Control control : getChildren()) {
                    control.dispose();
                }
                Hyperlink addCellLink = new Hyperlink(this, SWT.NONE);
                addCellLink.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                addCellLink.setText(Messages.getString("label.AddCell"));
                addCellLink.addHyperlinkListener(new HyperlinkAdapter() {
                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        model.constraintses.add(new ConstraintsModel(ConstraintsModel.CELL));
                        buildFromModel();
                    }
                });
                hyperlinkGroup.add(addCellLink);
                Hyperlink addRowLink = new Hyperlink(this, SWT.NONE);
                addRowLink.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                addRowLink.setText(Messages.getString("label.AddRow"));
                addRowLink.addHyperlinkListener(new HyperlinkAdapter() {
                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        model.constraintses.add(new ConstraintsModel(ConstraintsModel.ROW));
                        buildFromModel();
                    }
                });
                hyperlinkGroup.add(addRowLink);
                Hyperlink addColumnLink = new Hyperlink(this, SWT.NONE);
                addColumnLink.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
                addColumnLink.setText(Messages.getString("label.AddColumn"));
                addColumnLink.addHyperlinkListener(new HyperlinkAdapter() {
                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        model.constraintses.add(new ConstraintsModel(ConstraintsModel.COLUMN));
                        buildFromModel();
                    }
                });
                hyperlinkGroup.add(addColumnLink);
                new InputOutputComposite(this, model.getInOutModel(), variables, getMode());
                for (ConstraintsModel c : model.constraintses) {
                    switch (c.type) {
                    case ConstraintsModel.CELL:
                        new CellComposite(c);
                        break;
                    case ConstraintsModel.ROW:
                        new RowComposite(c);
                        break;
                    case ConstraintsModel.COLUMN:
                        new ColumnComposite(c);
                        break;
                    }
                }
                ((ScrolledComposite) getParent()).setMinSize(computeSize(getSize().x, SWT.DEFAULT));
                ((ScrolledComposite) getParent()).setMinHeight(2000);
                this.layout(true, true);
                this.redraw();
            } catch (Throwable e) {
                PluginLogger.logErrorWithoutDialog("Cannot build model", e);
            }
        }

        public abstract class ConstraintsComposite extends Composite {
            public final ConstraintsModel cmodel;

            ConstraintsComposite(ConstraintsModel m) {
                super(ConstructorView.this, SWT.NONE);
                cmodel = m;
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                data.horizontalSpan = 3;
                setLayoutData(data);
                setLayout(new FillLayout(SWT.VERTICAL));
                Group group = new Group(this, SWT.None);
                group.setLayout(new GridLayout(3, false));
                group.setText(getTitle());
                Label l = new Label(group, SWT.NONE);
                l.setText(Messages.getString("label.variable"));
                final Combo combo = new Combo(group, SWT.READ_ONLY);
                for (String varName : variables.keySet()) {
                    if (isImportant(variables.get(varName))) {
                        combo.add(varName);
                    }
                }
                try {
                    combo.setText(cmodel.variable);
                } catch (Exception e) {
                }
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        cmodel.variable = combo.getText();
                    }
                });
                Hyperlink hl1 = new Hyperlink(group, SWT.NONE);
                hl1.setText("[X]");
                hl1.addHyperlinkListener(new HyperlinkAdapter() {
                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        model.constraintses.remove(cmodel);
                        buildFromModel();
                    }
                });
                hyperlinkGroup.add(hl1);
                final Combo sheetCombo = new Combo(group, SWT.READ_ONLY);
                sheetCombo.add(Messages.getString("label.sheetByTitle"));
                sheetCombo.add(Messages.getString("label.sheetByIndex"));
                final Text sheetText = new Text(group, SWT.BORDER);
                sheetText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                if (cmodel.sheetName != null && cmodel.sheetName.length() > 0) {
                    sheetCombo.select(0);
                    sheetText.setText(cmodel.sheetName);
                } else {
                    sheetCombo.select(1);
                    sheetText.setText("" + cmodel.sheetIndex);
                }
                sheetText.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent arg0) {
                        updateSheet(sheetCombo, sheetText);
                    }
                });
                sheetCombo.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetDefaultSelected(SelectionEvent arg0) {
                    }

                    @Override
                    public void widgetSelected(SelectionEvent arg0) {
                        updateSheet(sheetCombo, sheetText);
                    }
                });
                new Label(group, SWT.NONE);
                l = new Label(group, SWT.None);
                l.setText(getXcoordMessage());
                final Text tx = new Text(group, SWT.BORDER);
                tx.setText("" + cmodel.getColumn());
                tx.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                new Label(group, SWT.NONE);
                tx.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent arg0) {
                        try {
                            int x = Integer.parseInt(tx.getText());
                            if (x >= 0 && x < 65535) {
                                cmodel.setColumn(x);
                            } else {
                                tx.setText("0");
                            }
                        } catch (Exception e) {
                            tx.setText("0");
                        }
                    }
                });
                l = new Label(group, SWT.None);
                l.setText(getYcoordMessage());
                final Text ty = new Text(group, SWT.BORDER);
                ty.setText("" + cmodel.getRow());
                ty.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                new Label(group, SWT.NONE);
                ty.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent arg0) {
                        try {
                            int y = Integer.parseInt(ty.getText());
                            if (y >= 0 && y < 65535) {
                                cmodel.setRow(y);
                            } else {
                                ty.setText("0");
                            }
                        } catch (Exception e) {
                            ty.setText("0");
                        }
                    }
                });
                layout(true, true);
            }

            private void updateSheet(Combo combo, Text text) {
                if (combo.getSelectionIndex() == 0) {
                    cmodel.sheetName = text.getText();
                    cmodel.sheetIndex = 0;
                } else {
                    cmodel.sheetName = "";
                    try {
                        cmodel.sheetIndex = Integer.parseInt(text.getText());
                    } catch (Exception e) {
                        cmodel.sheetIndex = 0;
                        text.setText("0");
                    }
                }
            }

            protected boolean isImportant(String javaType) {
                return VariableFormatRegistry.isAssignableFrom(List.class, javaType);
            }

            public abstract String getTitle();

            public String getXcoordMessage() {
                return Messages.getString("label.xcoord");
            }

            public String getYcoordMessage() {
                return Messages.getString("label.ycoord");
            }
        }

        public class CellComposite extends ConstraintsComposite {
            CellComposite(ConstraintsModel m) {
                super(m);
            }

            @Override
            public boolean isImportant(String javaType) {
                return !VariableFormatRegistry.isAssignableFrom(List.class, javaType);
            }

            @Override
            public String getTitle() {
                return Messages.getString("label.Cell");
            }
        }

        public class RowComposite extends ConstraintsComposite {
            RowComposite(ConstraintsModel m) {
                super(m);
            }

            @Override
            public String getTitle() {
                return Messages.getString("label.Row");
            }

            @Override
            public String getXcoordMessage() {
                return Messages.getString("label.xcoord_start");
            }
        }

        public class ColumnComposite extends ConstraintsComposite {
            ColumnComposite(ConstraintsModel m) {
                super(m);
            }

            @Override
            public String getTitle() {
                return Messages.getString("label.Column");
            }

            @Override
            public String getYcoordMessage() {
                return Messages.getString("label.ycoord_start");
            }
        }
    }
}
