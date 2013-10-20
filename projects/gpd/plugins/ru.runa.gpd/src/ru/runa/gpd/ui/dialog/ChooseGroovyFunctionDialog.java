package ru.runa.gpd.ui.dialog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;

import ru.cg.runaex.shared.bean.project.xml.GroovyFunction;
import ru.runa.gpd.Localization;
import ru.runa.gpd.data.DaoFactory;
import ru.runa.gpd.data.provider.GroovyFunctionsDao;
import ru.runa.gpd.util.ProjectFinder;

public class ChooseGroovyFunctionDialog extends ChooseItemDialog {

    private class GroovyFunctionlabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            GroovyFunction function = (GroovyFunction) element;
            return getFunctionSignature(function);
        }
    }

    private String getFunctionSignature(GroovyFunction function) {
        Matcher functionSignatureMatcher = FUNCTION_SIGNATURE_PATTERN.matcher(function.getCode());
        if (functionSignatureMatcher.find()) {
            return functionSignatureMatcher.group(1).concat(functionSignatureMatcher.group(2));
        }
        return "";
    }

    private static final GroovyFunctionsDao GROOVY_FUNCTIONS_DAO = DaoFactory.getGroovyFunctionsDao();
    private static final Pattern FUNCTION_SIGNATURE_PATTERN = Pattern.compile("def\\s+(.{0,}?)\\s{0,}(\\(.{0,}?\\))");

    private final List<GroovyFunction> groovyFunctions;

    public ChooseGroovyFunctionDialog() {
        super(Localization.getString("ChooseGroovyFunction.title"), Localization.getString("ChooseGroovyFunction.message"), true);

        IProject currentProject = ProjectFinder.getCurrentProject();
        this.groovyFunctions = GROOVY_FUNCTIONS_DAO.get(currentProject).getGroovyFunctionList();

        setLabelProvider(new GroovyFunctionlabelProvider());
    }

    public String openDialog() {
        try {
            Collections.sort(groovyFunctions, new Comparator<GroovyFunction>() {

                @Override
                public int compare(GroovyFunction arg0, GroovyFunction arg1) {
                    return arg0.getCode().compareTo(arg1.getCode());
                }

            });
            setItems(groovyFunctions);
            if (open() != IDialogConstants.CANCEL_ID) {
                GroovyFunction selectedFunction = (GroovyFunction) getSelectedItem();
                return getFunctionSignature(selectedFunction);

            }
        } catch (Exception e) {
            // ignore this and return null;
        }
        return null;

    }

}
