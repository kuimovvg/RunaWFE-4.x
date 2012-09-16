/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.logic.bot;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.logic.bot.assigner.AssignerSettings;
import ru.runa.wf.logic.bot.assigner.AssignerSettingsXmlParser;
import ru.runa.wf.logic.bot.assigner.IEvaluationFunction;
import ru.runa.wf.service.ExecutionService;

public class AdvancedSwimlaneAssignerTaskHandler extends AbstractOrgFunctionTaskHandler {

    private AssignerSettings settings;

    public void configure(String configurationName) throws TaskHandlerException {
        settings = AssignerSettingsXmlParser.read(getClass().getResourceAsStream(configurationName));
    }

    public void configure(byte[] configuration) throws TaskHandlerException {
        settings = AssignerSettingsXmlParser.read(new ByteArrayInputStream(configuration));
    }

    public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Map variablesMap = executionService.getVariables(subject, taskStub.getId());

            List conditions = settings.getAssignerConditions();
            for (Iterator iter = conditions.iterator(); iter.hasNext();) {
                AssignerSettings.Condition condition = (AssignerSettings.Condition) iter.next();
                if (isAppliedCondition(condition.getFunctionClassName(), variablesMap)) {
                    applyCondition(condition, variablesMap);
                    break;
                }
            }
            executionService.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), variablesMap);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

    private void applyCondition(AssignerSettings.Condition condition, Map variablesMap) {
        String actor = (String) variablesMap.get(condition.getVariableName());
        variablesMap.put(condition.getSwimlaneName(), actor);
    }

    private boolean isAppliedCondition(String functionClassName, Map variablesMap) throws TaskHandlerException {
        if ("true".equalsIgnoreCase(functionClassName)) {
            return true;
        }
        try {
            Class clazz = Class.forName(functionClassName);
            IEvaluationFunction evaluationFunction = (IEvaluationFunction) clazz.newInstance();
            return evaluationFunction.evaluate(variablesMap);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }
}
