package ru.runa.gpd.swimlane;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class RelationSwimlaneInitializer extends SwimlaneInitializer {
    private static final char RELATION_INVERSED = '!';
    public static final String RELATION_BEGIN = "@";
    private String relationName = "";
    private String relationParameterVariableName = "";
    private boolean inversed;

    public RelationSwimlaneInitializer() {
    }

    public RelationSwimlaneInitializer(String swimlaneConfiguration) {
        Preconditions.checkArgument(swimlaneConfiguration.startsWith(RELATION_BEGIN), "Invalid configuration");
        int relationNameBegin = RELATION_BEGIN.length();
        if (swimlaneConfiguration.charAt(relationNameBegin) == RELATION_INVERSED) {
            relationNameBegin += 1;
            inversed = true;
        }
        int leftBracketIndex = swimlaneConfiguration.indexOf(LEFT_BRACKET);
        relationName = swimlaneConfiguration.substring(relationNameBegin, leftBracketIndex);
        int startIndex = relationName.length() + relationNameBegin + 1;
        relationParameterVariableName = swimlaneConfiguration.substring(startIndex, swimlaneConfiguration.length() - 1);
        if (relationParameterVariableName.contains(LEFT_BRACKET) && relationParameterVariableName.endsWith(RIGHT_BRACKET)) {
            // back compatibility
            leftBracketIndex = relationParameterVariableName.indexOf(LEFT_BRACKET);
            relationParameterVariableName = relationParameterVariableName.substring(leftBracketIndex + 3, relationParameterVariableName.length() - 2);
        }
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getRelationParameterVariableName() {
        return relationParameterVariableName;
    }

    public void setRelationParameterVariableName(String relationParameterVariableName) {
        this.relationParameterVariableName = relationParameterVariableName;
    }

    public boolean isInversed() {
        return inversed;
    }

    public void setInversed(boolean reversed) {
        this.inversed = reversed;
    }

    @Override
    public boolean hasReference(Variable variable) {
        return Objects.equal(relationParameterVariableName, variable.getName());
    }

    @Override
    public void onVariableRename(String variableName, String newVariableName) {
        if (Objects.equal(relationParameterVariableName, variableName)) {
            relationParameterVariableName = newVariableName;
        }
    }

    @Override
    public List<String> getErrors(ProcessDefinition processDefinition) {
        List<String> errors = new ArrayList<String>();
        if (Strings.isNullOrEmpty(relationName)) {
            errors.add("relation.emptyName");
        }
        List<String> variableNames = processDefinition.getVariableNames(true, Executor.class.getName());
        if (!variableNames.contains(relationParameterVariableName)) {
            errors.add("relation.variableDoesNotExist");
        }
        return errors;
    }

    @Override
    public String toString() {
        if (relationName == null) {
            // special case without initializer
            return "";
        }
        StringBuffer result = new StringBuffer();
        result.append(RELATION_BEGIN);
        if (inversed) {
            result.append(RELATION_INVERSED);
        }
        result.append(relationName).append("(");
        result.append(relationParameterVariableName);
        result.append(")");
        return result.toString();
    }
}
