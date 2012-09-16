package ru.runa.office;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
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
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.Variable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.runa.office.resource.Messages;

public class InputOutputComposite extends Composite {

    public final InputOutputModel model;
    private final ProcessDefinition definition;

    public InputOutputComposite(Composite parent, InputOutputModel m, ProcessDefinition definition, FilesSupplierMode mode) {
        super(parent, SWT.NONE);
        this.model = m;
        this.definition = definition;
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        setLayoutData(data);
        setLayout(new FillLayout(SWT.VERTICAL));

        if (mode.isInSupported()) {
            Group inputGroup = new Group(this, SWT.NONE);
            inputGroup.setText(Messages.getString("label.input"));
            inputGroup.setLayout(new GridLayout(2, false));

            new ChooseFileName(inputGroup, model.inputPath, model.inputVariable) {

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

            Label dirLabel = new Label(outputGroup, SWT.NONE);
            dirLabel.setText(Messages.getString("label.outputDir"));
            final Text dirText = new Text(outputGroup, SWT.BORDER);
            dirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (m.outputDir != null) {
                dirText.setText(m.outputDir);
            }
            dirText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent arg0) {
                    model.outputDir = dirText.getText();
                }
            });

            new ChooseFileName(outputGroup, model.outputFilename, model.outputVariable) {

                @Override
                public void setFileName(String fileName) {
                    model.outputFilename = fileName;
                    model.outputVariable = "";
                }

                @Override
                public void setVariable(String variable) {
                    model.outputFilename = "";
                    model.outputVariable = variable;
                }
            };
        }
        layout(true, true);
    }

    private abstract class ChooseFileName {

        public abstract void setFileName(String fileName);

        public abstract void setVariable(String variable);

        private Control control = null;
        private final Composite composite;

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
            text.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent arg0) {
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
            List<Variable> vars = definition.getVariablesList();
            for (Variable var : vars) {
                if ("ru.runa.wf.web.forms.format.FileFormat".equals(var.getFormat())) {
                    combo.add(var.getName());
                }
            }
            if (variable != null) {
                combo.setText(variable);
            }
            combo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    setVariable(combo.getText());
                }
            });
            combo.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent arg0) {
                    setVariable(combo.getText());
                }
            });
            control = combo;
            composite.layout(true, true);
        }

        public ChooseFileName(Composite composite, String fileName, String variable) {
            this.composite = composite;
            final Combo combo = new Combo(composite, SWT.READ_ONLY);
            combo.add(Messages.getString("label.fileNameConstantValue"));
            combo.add(Messages.getString("label.fileNameVariableValue"));

            if (variable != null && variable.length() > 0) {
                combo.select(1);
                showVariable(variable);
            } else {
                combo.select(0);
                showFileName(fileName);
            }

            combo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    if (combo.getSelectionIndex() == 0) {
                        showFileName(null);
                    } else {
                        showVariable(null);
                    }
                }
            });
        }
    }

    public static class InputOutputModel {

        public String inputPath;
        public String inputVariable;
        public String outputDir;
        public String outputVariable;
        public String outputFilename;

        public void serialize(Document document, Element parent, FilesSupplierMode mode) throws Exception {
            if (mode.isInSupported()) {
                Element input = document.createElement("input");
                parent.appendChild(input);
                if (inputPath != null && inputPath.length() > 0) {
                    input.setAttribute("path", inputPath);
                }
                if (inputVariable != null && inputVariable.length() > 0) {
                    input.setAttribute("variable", inputVariable);
                }
            }
            if (mode.isOutSupported()) {
                Element output = document.createElement("output");
                parent.appendChild(output);
                if (outputDir != null && outputDir.length() > 0) {
                    output.setAttribute("dir", outputDir);
                }
                if (outputVariable != null && outputVariable.length() > 0) {
                    output.setAttribute("variable", outputVariable);
                }
                if (outputFilename != null && outputFilename.length() > 0) {
                    output.setAttribute("fileName", outputFilename);
                }
            }
        }

        public static InputOutputModel deserialize(Element input, Element output) throws Exception {
            InputOutputModel model = new InputOutputModel();
            if (input != null) {
                model.inputPath = input.getAttribute("path");
                model.inputVariable = input.getAttribute("variable");
            }
            if (output != null) {
                model.outputFilename = output.getAttribute("fileName");
                model.outputDir = output.getAttribute("dir");
                model.outputVariable = output.getAttribute("varaible");
            }
            return model;
        }
    }

}
