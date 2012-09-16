package ru.runa.bpm.ui.custom;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.dialog.ChooseItemDialog;
import ru.runa.bpm.ui.dialog.ChooseVariableDialog;
import ru.runa.bpm.ui.dialog.DelegableConfigurationDialog;
import ru.runa.bpm.ui.dialog.HighlightTextStyling;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.IOUtils;

public class FormulaCellEditorProvider extends DelegableProvider {

    @Override
    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        return new ConfigurationDialog(
                delegable.getDelegationConfiguration(), 
                definition.getVariableNames(true));
    }

    public static class ConfigurationDialog extends DelegableConfigurationDialog {
        private final List<String> variableNames;
        private HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());

        public ConfigurationDialog(String initialValue, List<String> variableNames) {
            super(initialValue);
            this.variableNames = variableNames;
        }
        
        @Override
        protected void createDialogHeader(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(3, false));
            composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Hyperlink hl1 = new Hyperlink(composite, SWT.NONE);
            hl1.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
            hl1.setText(Messages.getString("help"));
            hl1.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    try {
                        String lang = Locale.getDefault().getLanguage();
                        InputStream is = FormulaCellEditorProvider.class.getResourceAsStream("FormulaHelp_" + lang);
                        if (is == null) {
                            is = FormulaCellEditorProvider.class.getResourceAsStream("FormulaHelp");
                        }
                        String help = IOUtils.readStream(is);
                        HelpDialog dialog = new HelpDialog(help);
                        dialog.open();
                    } catch (IOException ex) {
                        DesignerLogger.logError("Unable to find help file", ex);
                    }
                }
            });
            hyperlinkGroup.add(hl1);

            Hyperlink hl2 = new Hyperlink(composite, SWT.NONE);
            hl2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            hl2.setText(Messages.getString("button.insert_function"));
            hl2.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    ChooseFunctionDialog dialog = new ChooseFunctionDialog();
                    String function = dialog.openDialog();
                    if (function != null) {
                        styledText.insert(function);
                        styledText.setFocus();
                        styledText.setCaretOffset(styledText.getCaretOffset() + function.length());
                    }
                }
            });
            hyperlinkGroup.add(hl2);

            Hyperlink hl3 = new Hyperlink(composite, SWT.NONE);
            hl3.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            hl3.setText(Messages.getString("button.insert_variable"));
            hl3.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    ChooseVariableDialog dialog = new ChooseVariableDialog(variableNames);
                    String variableName = dialog.openDialog();
                    if (variableName != null) {
                        if (variableName.indexOf(" ") > 0)
                            variableName = "'" + variableName + "'";
                        styledText.insert(variableName);
                        styledText.setFocus();
                        styledText.setCaretOffset(styledText.getCaretOffset() + variableName.length());
                    }
                }
            });
            hyperlinkGroup.add(hl3);
        }
        
        @Override
        protected void createDialogFooter(Composite composite) {
            styledText.addLineStyleListener(new FormulaConfigurationStyling(variableNames));
        }
    }
    
    public static class FormulaConfigurationStyling extends HighlightTextStyling {
        private static final Color VARIABLE_COLOR = new Color(null, 155, 155, 255);

        public FormulaConfigurationStyling(List<String> variables) {
            super(new ArrayList<RegexpHighlight>());
            for (String variableName : variables) {
                StyleRange styleRange = new StyleRange(0, 0, VARIABLE_COLOR, null, SWT.NORMAL);
                addHighlightDefinition(new RegexpHighlight(variableName, variableName, styleRange));
            }
        }

    }
    
    private static class ChooseFunctionDialog extends ChooseItemDialog {
        private final static List<String> functions = new ArrayList<String>();
        static {
            functions.add("get_instance_id()");
            functions.add("current_date()");
            functions.add("current_time()");
            functions.add("current_date_time()");
            functions.add("date(d1)");
            functions.add("time(d1)");
            functions.add("hours_round_up(n1)");
            functions.add("round(n1)");
            functions.add("round(n1, n2)");
            functions.add("round_down(n1)");
            functions.add("round_down(n1, n2)");
            functions.add("round_up(n1)");
            functions.add("round_up(n1, n2)");
            functions.add("number_to_string_ru(n1)");
            functions.add("number_to_string_ru(n1, s2, s3, s4, s5)");
            functions.add("number_to_short_string_ru(n1, s2, s3, s4, s5)");
            functions.add("FIO_case_ru(fio, caseNumber, mode)");
        }

        public ChooseFunctionDialog() {
            super(Messages.getString("ChooseFunction.title"), Messages.getString("ChooseFunction.message"), false);
        }

        public String openDialog() {
            setItems(functions);
            if (open() == IDialogConstants.OK_ID) {
                return (String) getSelectedItem();
            }
            return null;

        }
    }
    
    private static class HelpDialog extends Dialog {
        private Text text;
        private String initValue;
        
        public HelpDialog(String initValue){
            super(Display.getCurrent().getActiveShell());
            this.initValue = initValue;
            setShellStyle(getShellStyle()|SWT.RESIZE);
        }
        
        @Override
        protected Point getInitialSize() {
            return new Point(700, 500);
        }
        
        @Override
        protected Control createDialogArea(Composite parent) {
            getShell().setText("ExecuteFormulaActionHandler help");
            
            Composite composite = new Composite(parent, SWT.NULL);
            composite.setLayout(new GridLayout());
            composite.setLayoutData(new GridData(GridData.FILL_BOTH));
            
            text = new Text(composite, SWT.MULTI|SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
            text.setLayoutData(new GridData(GridData.FILL_BOTH));
            text.setText(this.initValue);
            text.setEditable(false);
            
            return composite;
        }
        
    }
}
