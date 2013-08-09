package ru.runa.gpd.extension.decision;

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
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;

public class GroovyDecisionProvider extends DelegableProvider implements IDecisionProvider {
    @Override
    public String showConfigurationDialog(Delegable delegable) {
        if (!HandlerArtifact.DECISION.equals(delegable.getDelegationType())) {
            throw new IllegalArgumentException("For decision handler only");
        }
        ProcessDefinition definition = ((GraphElement) delegable).getProcessDefinition();
        List<Transition> transitions = ((Decision) delegable).getLeavingTransitions();
        List<String> transitionNames = new ArrayList<String>();
        for (Transition transition : transitions) {
            transitionNames.add(transition.getName());
        }
        GroovyEditorDialog dialog = new GroovyEditorDialog(definition, transitionNames, delegable.getDelegationConfiguration());
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    @Override
    public boolean validateValue(Delegable delegable) {
        String configuration = delegable.getDelegationConfiguration();
        try {
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);
            shell.parse(configuration);
            return configuration.trim().length() > 0;
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Script parse error: " + configuration, e);
            return false;
        }
    }

    @Override
    public Set<String> getTransitionNames(Decision decision) {
        try {
            List<Variable> variables = decision.getProcessDefinition().getVariables(true);
            GroovyDecisionModel model = new GroovyDecisionModel(decision.getDelegationConfiguration(), variables);
            return new HashSet<String>(model.getTransitionNames());
        } catch (Exception e) {
        }
        return new HashSet<String>();
    }

    @Override
    public String getDefaultTransitionName(Decision decision) {
        try {
            List<Variable> variables = decision.getProcessDefinition().getVariables(true);
            GroovyDecisionModel model = new GroovyDecisionModel(decision.getDelegationConfiguration(), variables);
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
