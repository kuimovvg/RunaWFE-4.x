package ru.runa.gpd.extension.handler;

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
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.DelegableConfigurationDialog;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.ui.custom.HighlightTextStyling;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.dialog.ChooseItemDialog;
import ru.runa.gpd.ui.dialog.ChooseVariableDialog;
import ru.runa.gpd.util.IOUtils;

public class FormulaCellEditorProvider extends DelegableProvider {

    @Override
    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        return new ConfigurationDialog(delegable.getDelegationConfiguration(), delegable.getVariableNames(true));
    }

    private static class ConfigurationDialog extends DelegableConfigurationDialog {
        private final List<String> variableNames;

        public ConfigurationDialog(String initialValue, List<String> variableNames) {
            super(initialValue);
            this.variableNames = variableNames;
        }

        @Override
        protected void createDialogHeader(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(3, false));
            composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Hyperlink hl1 = SWTUtils.createLink(composite, Localization.getString("help"), new LoggingHyperlinkAdapter() {
                @Override
                public void onLinkActivated(HyperlinkEvent e) throws IOException {
                    String lang = Locale.getDefault().getLanguage();
                    InputStream is = FormulaCellEditorProvider.class.getResourceAsStream("FormulaHelp_" + lang);
                    if (is == null) {
                        is = FormulaCellEditorProvider.class.getResourceAsStream("FormulaHelp");
                    }
                    String help = IOUtils.readStream(is);
                    HelpDialog dialog = new HelpDialog(help);
                    dialog.open();
                }
            });
            hl1.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));

            Hyperlink hl2 = SWTUtils.createLink(composite, Localization.getString("button.insert_function"), new LoggingHyperlinkAdapter() {
                @Override
                public void onLinkActivated(HyperlinkEvent e) {
                    ChooseFunctionDialog dialog = new ChooseFunctionDialog();
                    String function = dialog.openDialog();
                    if (function != null) {
                        styledText.insert(function);
                        styledText.setFocus();
                        styledText.setCaretOffset(styledText.getCaretOffset() + function.length());
                    }
                }
            });
            hl2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            Hyperlink hl3 = SWTUtils.createLink(composite, Localization.getString("button.insert_variable"), new LoggingHyperlinkAdapter() {
                @Override
                public void onLinkActivated(HyperlinkEvent e) {
                    ChooseVariableDialog dialog = new ChooseVariableDialog(variableNames);
                    String variableName = dialog.openDialog();
                    if (variableName != null) {
                        if (variableName.indexOf(" ") > 0) {
                            variableName = "'" + variableName + "'";
                        }
                        styledText.insert(variableName);
                        styledText.setFocus();
                        styledText.setCaretOffset(styledText.getCaretOffset() + variableName.length());
                    }
                }
            });
            hl3.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
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
            functions.add("get_process_id()");
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
            functions.add("isExecutorInGroup(group, executor)");
            functions.add("ListToString(list)");
            functions.add("GetListMatchedIndexes(list, argument)");
            functions.add("GetListMismatchedIndexes(list1, list2)");
            functions.add("CreateSubListByIndexes(list, indexes)");
            functions.add("DeleteListElementsByIndexes(list, indexes)");
            functions.add("ToList(argument1[, argumentN])");
            functions.add("GetSize(container)");
        }

        public ChooseFunctionDialog() {
            super(Localization.getString("ChooseFunction.title"), Localization.getString("ChooseFunction.message"), false);
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

        public HelpDialog(String initValue) {
            super(Display.getCurrent().getActiveShell());
            this.initValue = initValue;
            setShellStyle(getShellStyle() | SWT.RESIZE);
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

            text = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
            text.setLayoutData(new GridData(GridData.FILL_BOTH));
            text.setText(this.initValue);
            text.setEditable(false);

            return composite;
        }

    }
}