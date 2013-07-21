package ru.runa.wfe.execution.logic;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.dao.RelationDAO;
import ru.runa.wfe.relation.dao.RelationPairDAO;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RelationSwimlaneInitializer extends SwimlaneInitializer {
    private static final char RELATION_INVERSED = '!';
    public static final String RELATION_BEGIN = "@";
    private String relationName;
    private String relationParameterVariableName;
    private boolean inversed;

    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private RelationDAO relationDAO;
    @Autowired
    private RelationPairDAO relationPairDAO;

    @Override
    public void parse(String swimlaneConfiguration) {
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

    @Override
    public List<? extends Executor> evaluate(IVariableProvider variableProvider) {
        Executor parameter = variableProvider.getValueNotNull(Executor.class, relationParameterVariableName);
        Set<Executor> parameters = Sets.newHashSet();
        parameters.add(parameter);
        parameters.addAll(executorDAO.getExecutorParentsAll(parameter));
        Set<Executor> result = Sets.newHashSet();
        Relation relation = relationDAO.getNotNull(relationName);
        if (inversed) {
            List<RelationPair> pairs = relationPairDAO.getExecutorsRelationPairsLeft(relation, parameters);
            for (RelationPair pair : pairs) {
                result.add(pair.getRight());
            }
        } else {
            List<RelationPair> pairs = relationPairDAO.getExecutorsRelationPairsRight(relation, parameters);
            for (RelationPair pair : pairs) {
                result.add(pair.getLeft());
            }
        }
        return Lists.newArrayList(result);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("relationName", relationName).add("relationParameterVariableName", relationParameterVariableName)
                .add("inversed", inversed).toString();
    }

}
