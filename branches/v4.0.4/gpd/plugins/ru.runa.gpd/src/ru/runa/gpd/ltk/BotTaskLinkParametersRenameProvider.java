package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.lang.model.BotTaskLink;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.base.Objects;

@SuppressWarnings({ "unchecked" })
public class BotTaskLinkParametersRenameProvider extends VariableRenameProvider<BotTaskLink> {
    private static final String PARAM = "param";
    private static final String VARIABLE = "variable";

    public BotTaskLinkParametersRenameProvider(BotTaskLink botTaskLink) {
        setElement(botTaskLink);
    }

    @Override
    public List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        Map<String, String> parameters = ParamDefConfig.getAllParameters(element.getDelegationConfiguration());
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (Objects.equal(oldVariable.getName(), entry.getValue())) {
                changes.add(new ParamChange(element, oldVariable.getName(), newVariable.getName()));
            }
        }
        return changes;
    }

    private class ParamChange extends TextCompareChange {
        public ParamChange(BotTaskLink element, String currentVariableName, String previewVariableName) {
            super(element, currentVariableName, previewVariableName);
        }

        @Override
        protected String toPreviewContent(String variableName) {
            return variableName;
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            try {
                Document document = XmlUtil.parseWithoutValidation(element.getDelegationConfiguration());
                List<Element> groupElements = document.getRootElement().elements();
                for (Element groupElement : groupElements) {
                    List<Element> paramElements = groupElement.elements(PARAM);
                    for (Element element : paramElements) {
                        if (Objects.equal(currentVariableName, element.attributeValue(VARIABLE))) {
                            element.addAttribute(VARIABLE, replacementVariableName);
                        }
                    }
                }
                String configuration = XmlUtil.toString(document);
                element.setDelegationConfiguration(configuration);
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
            return new NullChange("BotTaskLink");
        }
    }
}
