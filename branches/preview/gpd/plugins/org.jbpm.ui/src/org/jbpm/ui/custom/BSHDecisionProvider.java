package ru.runa.bpm.ui.custom;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.window.Window;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.bsh.BSHDecisionModel;
import ru.runa.bpm.ui.common.model.Decision;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.Transition;
import ru.runa.bpm.ui.dialog.BSHEditorDialog;

import bsh.ParseException;
import bsh.Parser;

public class BSHDecisionProvider extends DelegableProvider implements IDecisionProvider {

    @Override
    public String showConfigurationDialog(Delegable delegable) {
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        List<Transition> transitions = ((Decision) delegable).getLeavingTransitions();
        List<String> transitionNames = new ArrayList<String>();
        for (Transition transition : transitions) {
            transitionNames.add(transition.getName());
        }

        BSHEditorDialog dialog = new BSHEditorDialog(delegable.getDelegationConfiguration(), transitionNames, definition.getVariablesList());

        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    @Override
    public boolean validateValue(Delegable delegable) {
        try {
            String configuration = delegable.getDelegationConfiguration();
            Parser parser = new Parser(new ByteArrayInputStream(configuration.getBytes()));
            while (!parser.Line()) {
                parser.popNode();
            }
            return configuration.trim().length() > 0;
        } catch (ParseException e) {
            DesignerLogger.logErrorWithoutDialog("BSH parser exception", e);
            return false;
        }
    }

    public Set<String> getTransitionNames(Decision decision) {
        try {
            BSHDecisionModel model = new BSHDecisionModel(decision.getDelegationConfiguration(), decision.getProcessDefinition().getVariablesList());
            return new HashSet<String>(model.getTransitionNames());
        } catch (Exception e) {
        }
        return new HashSet<String>();
    }

    public String getDefaultTransitionName(Decision decision) {
        try {
            BSHDecisionModel model = new BSHDecisionModel(decision.getDelegationConfiguration(), decision.getProcessDefinition().getVariablesList());
            return model.getDefaultTransitionName();
        } catch (Exception e) {
        }
        return null;
    }

    public void transitionRenamed(Decision decision, String oldName, String newName) {
        String conf = decision.getDelegationConfiguration();
        conf = conf.replaceAll(Pattern.quote("\"" + oldName + "\""), Matcher.quoteReplacement("\"" + newName + "\""));
        decision.setDelegationConfiguration(conf);
    }

}
