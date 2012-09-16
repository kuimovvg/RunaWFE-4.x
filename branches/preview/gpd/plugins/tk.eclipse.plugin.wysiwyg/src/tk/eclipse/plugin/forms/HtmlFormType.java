package tk.eclipse.plugin.forms;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import ru.runa.bpm.ui.common.model.FormNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tk.eclipse.plugin.vartag.VarTagInfo;
import tk.eclipse.plugin.vartag.VarTagUtil;


public class HtmlFormType extends BaseHtmlFormType {

    @Override
    protected Map<String, Integer> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception {
        Map<String, Integer> variableNames = new HashMap<String, Integer>();
        Document document = getDocument(new ByteArrayInputStream(formBytes));
        NodeList vartagElements = document.getElementsByTagName("customtag");
        for (int i = 0; i < vartagElements.getLength(); i++) {
            Node varNode = vartagElements.item(i).getAttributes().getNamedItem("var");
            Node delegationNode = vartagElements.item(i).getAttributes().getNamedItem("delegation");
            VarTagInfo varTagInfo = VarTagUtil.getVarTagInfo(delegationNode.getNodeValue());
            Integer status;
            if (varTagInfo != null) {
                status = varTagInfo.inputTag ? WRITE_ACCESS : READ_ACCESS;
            } else {
                status = DOUBTFUL;
            }
            variableNames.put(varNode.getNodeValue(), status);
        }
        return variableNames;
    }

}
