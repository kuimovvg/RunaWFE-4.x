package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

@SuppressWarnings("unchecked")
public class BotTaskParamRenameProvider implements VariableRenameProvider {
    private static final String PARAM = "param";
    private static final String VARIABLE = "variable";
    private TaskState taskState;

    public BotTaskParamRenameProvider(TaskState taskState) {
        setElement(taskState);
    }

    @Override
    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        BotTask botTask = taskState.getBotTask();
        if (botTask != null) {
            String conf = botTask.getDelegationConfiguration();
            if (!Strings.isNullOrEmpty(conf)) {
                Document document = XmlUtil.parseWithoutValidation(conf);
                // FIXME recursive
                List<Element> elements = document.getRootElement().elements(PARAM);
                for (Element element : elements) {
                    if (Objects.equal(variableName, element.attributeValue(VARIABLE))) {
                        changes.add(new ParamChange(taskState, variableName, replacement));
                    }
                }
            }
        }
        return changes;
    }

    @Override
    public void setElement(GraphElement element) {
        taskState = (TaskState) element;
    }

    private class ParamChange extends TextCompareChange {
        public ParamChange(NamedGraphElement element, String currentVariableName, String previewVariableName) {
            super(element, currentVariableName, previewVariableName);
        }

        @Override
        protected String toPreviewContent(String variableName) {
            return variableName;
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            BotTask botTask = taskState.getBotTask();
            if (botTask != null) {
                String conf = botTask.getDelegationConfiguration();
                if (!Strings.isNullOrEmpty(conf)) {
                    try {
                        Document document = XmlUtil.parseWithoutValidation(conf);
                        // FIXME recursive
                        List<Element> elements = document.getRootElement().elements(PARAM);
                        for (Element element : elements) {
                            if (Objects.equal(currentVariableName, element.attributeValue(VARIABLE))) {
                                element.addAttribute(VARIABLE, replacementVariableName);
                            }
                        }
                        String newConf = XmlUtil.toString(document);
                        taskState.getBotTask().setDelegationConfiguration(newConf);
                    } catch (Exception e) {
                        PluginLogger.logError(e);
                    }
                }
            }
            return new NullChange("TaskState");
        }
    }
}
