package ru.runa.gpd.handler.decision;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.window.Window;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.handler.DelegableProvider;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;

public class BSHDecisionProvider extends DelegableProvider implements IDecisionProvider {
    @Override
    public String showConfigurationDialog(Delegable delegable) {
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        List<Transition> transitions = ((Decision) delegable).getLeavingTransitions();
        List<String> transitionNames = new ArrayList<String>();
        for (Transition transition : transitions) {
            transitionNames.add(transition.getName());
        }
        BSHEditorDialog dialog = new BSHEditorDialog(delegable.getDelegationConfiguration(), transitionNames, definition.getVariables());
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    @Override
    public boolean validateValue(Delegable delegable) {
        try {
            String configuration = delegable.getDelegationConfiguration();
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);
            shell.parse(configuration);
            return configuration.trim().length() > 0;
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Script parse error", e);
            return false;
        }
    }

    @Override
    public Set<String> getTransitionNames(Decision decision) {
        try {
            BSHDecisionModel model = new BSHDecisionModel(decision.getDelegationConfiguration(), decision.getProcessDefinition().getVariables());
            return new HashSet<String>(model.getTransitionNames());
        } catch (Exception e) {
        }
        return new HashSet<String>();
    }

    @Override
    public String getDefaultTransitionName(Decision decision) {
        try {
            BSHDecisionModel model = new BSHDecisionModel(decision.getDelegationConfiguration(), decision.getProcessDefinition().getVariables());
            return model.getDefaultTransitionName();
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void transitionRenamed(Decision decision, String oldName, String newName) {
        String conf = decision.getDelegationConfiguration();
        conf = conf.replaceAll(Pattern.quote("\"" + oldName + "\""), Matcher.quoteReplacement("\"" + newName + "\""));
        decision.setDelegationConfiguration(conf);
    }
}
