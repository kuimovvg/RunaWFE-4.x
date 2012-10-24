package org.jbpm.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.SharedImages;
import org.jbpm.ui.bsh.BSHDecisionModel;
import org.jbpm.ui.bsh.BSHTypeSupport;
import org.jbpm.ui.bsh.Operation;
import org.jbpm.ui.bsh.BSHDecisionModel.IfExpr;
import org.jbpm.ui.common.model.Variable;
import org.jbpm.ui.resource.Messages;

public class BSHEditorDialog extends Dialog {

    private static final Image upImage = SharedImages.getImage("icons/up.gif");

    private TabFolder tabFolder;

    private StyledText styledText;

    private Composite constructor;

    private final String initValue;
    private BSHDecisionModel initModel;
    private String initErrorMessage;

    private final List<String> transitionNames;

    private final List<Variable> variables = new ArrayList<Variable>();
    private final List<String> variableNames = new ArrayList<String>();

    private ErrorHeaderComposite constructorHeader;
    private ErrorHeaderComposite sourceHeader;

    private Label[] labels;

    private Combo[][] comboBoxes;

    private Combo defaultTransitionCombo;

    private String result;

    public BSHEditorDialog(String initValue, List<String> transitionNames, List<Variable> variables) {
        super(Display.getCurrent().getActiveShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.initValue = initValue;
        this.transitionNames = transitionNames;
        for (Variable variable : variables) {
            if (variable.getName().indexOf(" ") < 0) {
                this.variables.add(variable);
            }
        }
        for (Variable variable : variables) {
            variableNames.add(variable.getName());
        }
        if (this.initValue.length() > 0) {
            try {
                initModel = new BSHDecisionModel(initValue, variables);
            } catch (Throwable e) {
                initErrorMessage = e.getMessage();
                DesignerLogger.logErrorWithoutDialog("", e);
            }
        }
    }

    @Override
    protected Point getInitialSize() {
        return new Point(700, 400);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.getString("BSHEditor.title"));

        tabFolder = new TabFolder(parent, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        tabFolder.addSelectionListener(new TabSelectionHandler());

        Composite constructorView = new Composite(tabFolder, SWT.NONE);
        constructorView.setLayout(new GridLayout());

        TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
        tabItem1.setText(Messages.getString("BSHEditor.title.constructor"));
        tabItem1.setControl(constructorView);

        constructorHeader = new ErrorHeaderComposite(constructorView);

        ScrolledComposite scrolledComposite = new ScrolledComposite(constructorView, SWT.V_SCROLL | SWT.BORDER);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinHeight(200);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        constructor = new Composite(scrolledComposite, SWT.NONE);

        scrolledComposite.setContent(constructor);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 5;
        constructor.setLayout(gridLayout);
        constructor.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite sourceView = new Composite(tabFolder, SWT.NONE);
        sourceView.setLayout(new GridLayout());

        sourceHeader = new ErrorHeaderComposite(sourceView);

        styledText = new StyledText(sourceView, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        styledText.addLineStyleListener(new JavaHighlightTextStyling(variableNames));
        styledText.setText(this.initValue);
        styledText.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
        tabItem2.setText(Messages.getString("BSHEditor.title.bsh"));
        tabItem2.setControl(sourceView);

        createConstructorView();

        try {
            if (initModel != null && initValue.equals(initModel.generateCode())) {
                initConstructorView();
            } else {
                if (this.initValue.length() > 0) {
                    tabFolder.setSelection(1);
                }
                if (initErrorMessage != null) {
                    setErrorLabelText(initErrorMessage);
                }
            }
        } catch (RuntimeException e) {
            // Activate source view if custom code found
            tabFolder.setSelection(1);
        }

        return tabFolder;
    }

    private void setErrorLabelText(String text) {
        constructorHeader.setErrorText(text);
        sourceHeader.setErrorText(text);
    }

    private void clearErrorLabelText() {
        constructorHeader.clearErrorText();
        sourceHeader.clearErrorText();
    }

    private void createConstructorView() {
        if (initModel != null) {
            // reorder transitions;
            List<String> tmp = new ArrayList<String>(transitionNames);
            transitionNames.clear();
            for (IfExpr expr : initModel.getIfExprs()) {
                if (tmp.remove(expr.getTransition())) {
                    transitionNames.add(expr.getTransition());
                }
            }
            for (String newTransition : tmp) {
                transitionNames.add(newTransition);
            }
        }
        comboBoxes = new Combo[transitionNames.size()][3];
        labels = new Label[transitionNames.size()];
        for (int i = 0; i < transitionNames.size(); i++) {
            labels[i] = new Label(constructor, SWT.NONE);
            labels[i].setText(transitionNames.get(i));
            labels[i].setLayoutData(getGridData());

            comboBoxes[i][0] = new Combo(constructor, SWT.READ_ONLY);
            for (Variable variable : variables) {
                comboBoxes[i][0].add(variable.getName());
            }
            comboBoxes[i][0].setData(DATA_INDEX_KEY, new int[] { i, 0 });
            comboBoxes[i][0].addSelectionListener(new ComboSelectionHandler());
            comboBoxes[i][0].setLayoutData(getGridData());

            comboBoxes[i][1] = new Combo(constructor, SWT.READ_ONLY);
            comboBoxes[i][1].setData(DATA_INDEX_KEY, new int[] { i, 1 });
            comboBoxes[i][1].addSelectionListener(new ComboSelectionHandler());
            comboBoxes[i][1].setLayoutData(getGridData());

            comboBoxes[i][2] = new Combo(constructor, SWT.READ_ONLY);
            comboBoxes[i][2].setData(DATA_INDEX_KEY, new int[] { i, 2 });
            comboBoxes[i][2].addSelectionListener(new ComboSelectionHandler());
            comboBoxes[i][2].setLayoutData(getGridData());

            if (i != 0) {
                Button upButton = new Button(constructor, SWT.PUSH);
                upButton.setImage(upImage);
                upButton.setData(i);
                upButton.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        upRecord((Integer) e.widget.getData());
                    }

                });
            } else {
                new Label(constructor, SWT.NONE);
            }
        }
        for (int i = 0; i < comboBoxes.length; i++) {
            for (int j = 0; j < 3; j++) {
                comboBoxes[i][j].setSize(100, 20);
            }
        }
        if (transitionNames.size() > 0) {
            Composite bottomComposite = new Composite(constructor, SWT.NONE);
            bottomComposite.setLayout(new GridLayout(2, true));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 5;
            bottomComposite.setLayoutData(data);
            Label defaultLabel = new Label(bottomComposite, SWT.NONE);
            defaultLabel.setText(Messages.getString("BSHEditor.byDefault") + ":");
            defaultTransitionCombo = new Combo(bottomComposite, SWT.READ_ONLY);
            for (String trName : transitionNames) {
                defaultTransitionCombo.add(trName);
            }
            defaultTransitionCombo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (int j = 0; j < labels.length; j++) {
                        boolean enabled = !labels[j].getText().equals(defaultTransitionCombo.getText());
                        comboBoxes[j][0].setEnabled(enabled);
                        comboBoxes[j][1].setEnabled(enabled);
                        comboBoxes[j][2].setEnabled(enabled);
                    }
                }
            });
            bottomComposite.pack();
        }
    }

    private void initConstructorView() {
        for (int i = 0; i < transitionNames.size(); i++) {
            IfExpr ifExpr = initModel.getIfExpr(transitionNames.get(i));

            if (ifExpr != null) {
                labels[i].setText(ifExpr.getTransition());
                if (ifExpr.isByDefault()) {
                    comboBoxes[i][0].setEnabled(false);
                    comboBoxes[i][1].setEnabled(false);
                    comboBoxes[i][2].setEnabled(false);
                    defaultTransitionCombo.setText(ifExpr.getTransition());
                } else {
                    Variable variable = ifExpr.getVariable1();
                    int index = variables.indexOf(variable);
                    if (index == -1) {
                        // required variable was deleted in process
                        // definition
                        continue;
                    }
                    comboBoxes[i][0].select(index);
                    refreshComboItems(comboBoxes[i][0]);

                    BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(variable.getFormat());

                    index = Operation.getAll(typeSupport).indexOf(ifExpr.getOperation());
                    if (index == -1) {
                        // required operation was deleted !!!
                        continue;
                    }
                    comboBoxes[i][1].select(index);
                    refreshComboItems(comboBoxes[i][1]);

                    String lexem2Text = ifExpr.getLexem2TextValue();
                    int combo3index = 0;
                    if (getVariableByName(lexem2Text) != null) {
                        combo3index = getCombo3VariableNames(variable).indexOf(lexem2Text);
                    } else {
                        int predefinedIndex = typeSupport.getPredefinedValues(ifExpr.getOperation()).indexOf(lexem2Text);
                        if (predefinedIndex >= 0) {
                            combo3index = getCombo3VariableNames(variable).size() + predefinedIndex;
                        } else {
                            comboBoxes[i][2].add(lexem2Text, 0);
                            comboBoxes[i][2].setData(DATA_USER_INPUT_KEY, lexem2Text);
                        }
                    }
                    comboBoxes[i][2].select(combo3index);
                }
            }
        }
    }

    static final String DATA_INDEX_KEY = "indexes";

    static final String DATA_VARIABLE_KEY = "variable";

    static final String DATA_USER_INPUT_KEY = "userInput";

    static final String DATA_OPERATION_KEY = "operation";

    static final String INPUT_VALUE = Messages.getString("BSH.InputValue");

    private GridData getGridData() {
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.minimumWidth = 100;
        return data;
    }

    private void upRecord(Integer recordIndex) {
        String recordText = labels[recordIndex].getText();
        labels[recordIndex].setText(labels[recordIndex - 1].getText());
        labels[recordIndex - 1].setText(recordText);

        boolean enabledIndex = comboBoxes[recordIndex][0].getEnabled();
        boolean enabledIndexPrev = comboBoxes[recordIndex - 1][0].getEnabled();
        int combo1Index = comboBoxes[recordIndex][0].getSelectionIndex();
        int combo2Index = comboBoxes[recordIndex][1].getSelectionIndex();
        int combo3Index = comboBoxes[recordIndex][2].getSelectionIndex();
        String combo3UserInput = (String) comboBoxes[recordIndex][2].getData(DATA_USER_INPUT_KEY);

        comboBoxes[recordIndex][0].select(comboBoxes[recordIndex - 1][0].getSelectionIndex());
        comboBoxes[recordIndex][0].setEnabled(enabledIndexPrev);
        refreshComboItems(comboBoxes[recordIndex][0]);
        comboBoxes[recordIndex][1].select(comboBoxes[recordIndex - 1][1].getSelectionIndex());
        comboBoxes[recordIndex][1].setEnabled(enabledIndexPrev);
        refreshComboItems(comboBoxes[recordIndex][1]);
        String combo3UserInput2 = (String) comboBoxes[recordIndex - 1][2].getData(DATA_USER_INPUT_KEY);
        if (combo3UserInput2 != null) {
            comboBoxes[recordIndex][2].add(combo3UserInput2, 0);
            comboBoxes[recordIndex][2].setData(DATA_USER_INPUT_KEY, combo3UserInput2);
        }
        comboBoxes[recordIndex][2].select(comboBoxes[recordIndex - 1][2].getSelectionIndex());
        comboBoxes[recordIndex][2].setEnabled(enabledIndexPrev);

        comboBoxes[recordIndex - 1][0].select(combo1Index);
        comboBoxes[recordIndex - 1][0].setEnabled(enabledIndex);
        refreshComboItems(comboBoxes[recordIndex - 1][0]);
        comboBoxes[recordIndex - 1][1].select(combo2Index);
        comboBoxes[recordIndex - 1][1].setEnabled(enabledIndex);
        refreshComboItems(comboBoxes[recordIndex - 1][1]);
        if (combo3UserInput != null) {
            comboBoxes[recordIndex - 1][2].add(combo3UserInput, 0);
            comboBoxes[recordIndex - 1][2].setData(DATA_USER_INPUT_KEY, combo3UserInput);
        }
        comboBoxes[recordIndex - 1][2].select(combo3Index);
        comboBoxes[recordIndex - 1][2].setEnabled(enabledIndex);
    }

    private Variable getVariableByName(String variableName) {
        for (Variable variable : variables) {
            if (variable.getName().equals(variableName)) {
                return variable;
            }
        }
        return null;
    }

    private void refreshComboItems(Combo combo) {
        try {
            int[] indexes = (int[]) combo.getData(DATA_INDEX_KEY);

            if (indexes[1] == 2) {
                if (INPUT_VALUE.equals(combo.getText())) {
                    String oldUserInput = (String) combo.getData(DATA_USER_INPUT_KEY);
                    Variable variable1 = (Variable) comboBoxes[indexes[0]][0].getData(DATA_VARIABLE_KEY);
                    BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(variable1.getFormat());
                    UserInputDialog inputDialog = typeSupport.createUserInputDialog(INPUT_VALUE, oldUserInput);
                    if (OK == inputDialog.open()) {
                        String userInput = inputDialog.getUserInput();
                        if (oldUserInput != null) {
                            combo.remove(0);
                        }
                        combo.setData(DATA_USER_INPUT_KEY, userInput);
                        combo.add(userInput, 0);
                        combo.select(0);
                    } else {
                        combo.deselectAll();
                    }
                } else {
                    Variable variable = getVariableByName(combo.getText());
                    if (variable != null) {
                        combo.setData(DATA_VARIABLE_KEY, variable);
                    }
                }
                return;
            }

            Combo targetCombo = comboBoxes[indexes[0]][indexes[1] + 1];
            targetCombo.setItems(new String[0]);
            if (indexes[1] == 0) {
                // there was changed value in first (variable) combo in 'i' row
                Variable variable = getVariableByName(combo.getText());
                combo.setData(DATA_VARIABLE_KEY, variable);
                if (variable != null) {
                    BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(variable.getFormat());
                    for (Operation operation : Operation.getAll(typeSupport)) {
                        targetCombo.add(operation.getVisibleName());
                    }
                }
            } else if (indexes[1] == 1) {
                // there was changed value in second (operation) combo in 'i'
                // row
                Variable variable1 = (Variable) comboBoxes[indexes[0]][0].getData(DATA_VARIABLE_KEY);
                if (variable1 != null) {
                    BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(variable1.getFormat());
                    Operation operation = Operation.getByName(combo.getText(), typeSupport);
                    combo.setData(DATA_OPERATION_KEY, operation);
                    for (String variableName : getCombo3VariableNames(variable1)) {
                        targetCombo.add(variableName);
                    }
                    for (String pv : typeSupport.getPredefinedValues(operation)) {
                        targetCombo.add(pv);
                    }
                    if (typeSupport.hasUserInputEditor()) {
                        targetCombo.add(INPUT_VALUE);
                    }
                }
            }
        } catch (RuntimeException e) {
            DesignerLogger.logError(e);
        }
    }

    private List<String> getCombo3VariableNames(Variable variable1) {
        List<String> vars = new ArrayList<String>();
        BSHTypeSupport typeSupport1 = BSHTypeSupport.getByFormat(variable1.getFormat());
        for (Variable variable : variables) {
            BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(variable.getFormat());
            // formats are equals, variable not selected in the first combo
            if (typeSupport1.getClass() == typeSupport.getClass() && variable1 != variable) {
                vars.add(variable.getName());
            }
        }
        return vars;
    }

    private class TabSelectionHandler extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (tabFolder.getSelectionIndex() == 1) {
                toBSHCode();
            }
        }

    }

    private class ErrorHeaderComposite extends Composite {

        private final Label errorLabel;

        public ErrorHeaderComposite(Composite parent) {
            super(parent, SWT.NONE);
            setLayout(new GridLayout(2, false));
            setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            errorLabel = new Label(this, SWT.NONE);
            errorLabel.setForeground(ColorConstants.red);
            errorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        public void setErrorText(String text) {
            errorLabel.setText(text);
            pack();
        }

        public void clearErrorText() {
            setErrorText("");
        }
    }

    private class ComboSelectionHandler extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            refreshComboItems((Combo) e.widget);
        }

    }

    private void toBSHCode() {
        for (int i = 0; i < comboBoxes.length; i++) {
            for (int j = 0; j < 3; j++) {
                if (comboBoxes[i][j].getText().length() == 0 && !labels[i].getText().equals(defaultTransitionCombo.getText())) {
                    setErrorLabelText(Messages.getString("BSHEditor.fillAll"));
                    // we cannot construct while all data not filled
                    return;
                }
            }
        }
        clearErrorLabelText();

        try {
            BSHDecisionModel decisionModel = new BSHDecisionModel(variables);
            for (int i = 0; i < transitionNames.size(); i++) {
                IfExpr ifExpr;
                if (labels[i].getText().equals(defaultTransitionCombo.getText())) {
                    ifExpr = new IfExpr(labels[i].getText());
                } else {
                    Variable var1 = (Variable) comboBoxes[i][0].getData(DATA_VARIABLE_KEY);
                    String operationName = comboBoxes[i][1].getItem(comboBoxes[i][1].getSelectionIndex());
                    String lexem2Text = comboBoxes[i][2].getText();

                    Object lexem2;
                    Variable var2 = getVariableByName(lexem2Text);
                    if (var2 != null) {
                        lexem2 = var2;
                    } else {
                        lexem2 = lexem2Text;
                    }
                    BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(var1.getFormat());

                    ifExpr = new IfExpr(labels[i].getText(), var1, lexem2, Operation.getByName(operationName, typeSupport));
                }
                decisionModel.addIfExpr(ifExpr);
            }
            styledText.setText(decisionModel.generateCode());
        } catch (RuntimeException e1) {
            DesignerLogger.logError(e1);
            setErrorLabelText(Messages.getString("BSHEditor.error.construct"));
        }
    }

    @Override
    protected void okPressed() {
        if (tabFolder.getSelectionIndex() == 0) {
            toBSHCode();
        }
        this.result = styledText.getText();
        super.okPressed();
    }

    public String getResult() {
        return result;
    }
}
