package ru.runa.gpd.office;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.office.resource.Messages;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.ProcessFileUtils;
import ru.runa.wfe.var.FileVariable;

import com.google.common.base.Strings;

public class InputOutputComposite extends Composite {
    public final InputOutputModel model;
    private final Delegable delegable;
    private final String fileExtension;

    public InputOutputComposite(Composite parent, Delegable delegable, final InputOutputModel model, FilesSupplierMode mode, String fileExtension) {
        super(parent, SWT.NONE);
        this.model = model;
        this.delegable = delegable;
        this.fileExtension = fileExtension;
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        setLayoutData(data);
        setLayout(new FillLayout(SWT.VERTICAL));
        if (mode.isInSupported()) {
            Group inputGroup = new Group(this, SWT.NONE);
            inputGroup.setText(Messages.getString("label.input"));
            inputGroup.setLayout(new GridLayout(2, false));
            new ChooseStringOrFile(inputGroup, model.inputPath, model.inputVariable, Messages.getString("label.filePath"), FilesSupplierMode.IN) {
                @Override
                public void setFileName(String fileName) {
                    model.inputPath = fileName;
                    model.inputVariable = "";
                }

                @Override
                public void setVariable(String variable) {
                    model.inputPath = "";
                    model.inputVariable = variable;
                }
            };
        }
        if (mode.isOutSupported()) {
            Group outputGroup = new Group(this, SWT.NONE);
            outputGroup.setText(Messages.getString("label.output"));
            outputGroup.setLayout(new GridLayout(2, false));
            Label fileNameLabel = new Label(outputGroup, SWT.NONE);
            fileNameLabel.setText(Messages.getString("label.fileName"));
            final Text fileNameText = new Text(outputGroup, SWT.BORDER);
            fileNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (model.outputFilename != null) {
                fileNameText.setText(model.outputFilename);
            }
            fileNameText.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    model.outputFilename = fileNameText.getText();
                }
            });
            new ChooseStringOrFile(outputGroup, model.outputDir, model.outputVariable, Messages.getString("label.fileDir"), FilesSupplierMode.OUT) {
                @Override
                public void setFileName(String fileName) {
                    model.outputDir = fileName;
                    model.outputVariable = "";
                }

                @Override
                public void setVariable(String variable) {
                    model.outputDir = "";
                    model.outputVariable = variable;
                }
            };
        }
        layout(true, true);
    }

    private abstract class ChooseStringOrFile implements PropertyChangeListener {
        public abstract void setFileName(String fileName);

        public abstract void setVariable(String variable);

        private Control control = null;
        private final Composite composite;

        public ChooseStringOrFile(Composite composite, String fileName, String variableName, String stringLabel, FilesSupplierMode mode) {
            this.composite = composite;
            final Combo combo = new Combo(composite, SWT.READ_ONLY);
            combo.add(stringLabel);
            combo.add(Messages.getString("label.fileVariable"));
            if (mode == FilesSupplierMode.IN) {
                combo.add(Messages.getString("label.processDefinitionFile"));
            }
            if (!Strings.isNullOrEmpty(variableName)) {
                combo.select(1);
                showVariable(variableName);
            } else if (ProcessFileUtils.isProcessFile(fileName)) {
                combo.select(2);
                showEmbeddedFile(fileName);
            } else {
                combo.select(0);
                showFileName(fileName);
            }
            combo.addSelectionListener(new LoggingSelectionAdapter() {
                
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    if (combo.getSelectionIndex() == 0) {
                        showFileName(null);
                    } else if (combo.getSelectionIndex() == 2) {
                        showEmbeddedFile(null);
                    } else {
                        showVariable(null);
                    }
                }
            });
        }

        private void showFileName(String filename) {
            if (control != null) {
                if (!Text.class.isInstance(control)) {
                    control.dispose();
                } else {
                    return;
                }
            }
            final Text text = new Text(composite, SWT.BORDER);
            if (filename != null) {
                text.setText(filename);
            }
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.addModifyListener(new LoggingModifyTextAdapter() {
                
                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    setFileName(text.getText());
                }
            });
            control = text;
            composite.layout(true, true);
        }

        private void showVariable(String variable) {
            if (control != null) {
                if (!Combo.class.isInstance(control)) {
                    control.dispose();
                } else {
                    return;
                }
            }
            final Combo combo = new Combo(composite, SWT.READ_ONLY);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            for (String variableName : delegable.getVariableNames(false, FileVariable.class.getName())) {
                combo.add(variableName);
            }
            if (variable != null) {
                combo.setText(variable);
            }
            combo.addSelectionListener(new LoggingSelectionAdapter() {
                
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    setVariable(combo.getText());
                }
            });
            combo.addModifyListener(new LoggingModifyTextAdapter() {
                
                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    setVariable(combo.getText());
                }
            });
            control = combo;
            composite.layout(true, true);
        }

        private void showEmbeddedFile(String path) {
            if (control != null) {
                if (TemplateFileComposite.class != control.getClass()) {
                    control.dispose();
                } else {
                    return;
                }
            }
            String fileName;
            if (ProcessFileUtils.isProcessFile(path)) {
                fileName = ProcessFileUtils.getProcessFileName(path);
            } else {
                String id = ((GraphElement) delegable).getId();
                fileName = id + ".template." + fileExtension;
                while (ProcessFileUtils.getProcessFile(fileName).exists()) {
                    id += "d";
                    fileName = id + ".template." + fileExtension;
                }
            }
            control = new TemplateFileComposite(composite, fileName, fileExtension);
            ((TemplateFileComposite) control).addPropertyChangeListener(this);
            composite.layout(true, true);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            setFileName(ProcessFileUtils.getProcessFilePath((String) event.getNewValue()));
        }

    }

}
