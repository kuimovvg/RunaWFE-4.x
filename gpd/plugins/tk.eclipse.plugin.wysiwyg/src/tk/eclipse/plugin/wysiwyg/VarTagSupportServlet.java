package tk.eclipse.plugin.wysiwyg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.bpm.ui.common.model.Variable;
import ru.runa.bpm.ui.util.IOUtils;

import tk.eclipse.plugin.vartag.VarTagInfo;
import tk.eclipse.plugin.vartag.VarTagUtil;

public class VarTagSupportServlet extends HttpServlet {
	private static final long serialVersionUID = -4457636711561815927L;
	
	private Map<String, VarTagInfo> varTags = null;

	@Override
	public void init() throws ServletException {
		super.init();
		varTags = VarTagUtil.getVarTagsInfo();
	}

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuffer resultHtml = new StringBuffer();

        String commandStr = request.getParameter("method");
        String javaType = request.getParameter("type");
       
        if("GetVarTagDialog".equals(commandStr)){
        	response.setContentType("text/html; charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
			IOUtils.writeToStream(response.getOutputStream(), CKEditorDialogCreatorHelper.createVarTagDialog());
			return;
        }
        if ("GetTagImage".equals(commandStr)) {
            response.setContentType("image/png;");
            InputStream imageStream = null;
            VarTagInfo varTagInfo = getVarTagSafely(javaType);
            if (varTagInfo.hasImage()) {
                imageStream = varTagInfo.openImageStream();
            } else {
                imageStream = WYSIWYGPlugin.loadTagImage(
                        WYSIWYGPlugin.getDefault().getBundle(), "metadata/var_icons/DefaultTag.png");
            }
            IOUtils.copyStream(imageStream, response.getOutputStream());
            return;
        }
        
		if ("GetVarTags".equals(commandStr)) {
			response.setContentType("text/html; charset=UTF-8");
		} else {
			response.setContentType("text/xml; charset=UTF-8");
		}
		response.setHeader("Cache-Control", "no-cache");

		if ("GetTagVisibleName".equals(commandStr)) {
            resultHtml.append(getVarTagSafely(javaType).displayName);
		} else if ("GetVarTags".equals(commandStr)) {
			resultHtml.append(IOUtils.readStream(VarTagUtil.class.getResourceAsStream("dialog.start")));
            for (Variable variable : WYSIWYGHTMLEditor.getCurrent().getVariablesList(true)) {
                resultHtml.append("<option value=\"").append(variable.getName()).append("\">").append(variable.getName()).append("</option>");
            }
            resultHtml.append("</select></td></tr><tr><td><span fckLang=\"VarTagDlgType\">Type</span><br><select id=\"tagType\">");
			for (VarTagInfo varTag : varTags.values()) {
                resultHtml.append("<option value=\"");
                resultHtml.append(varTag.javaType).append("\">");
                resultHtml.append(varTag.displayName);
                resultHtml.append("</option>");
			}
			resultHtml.append(IOUtils.readStream(VarTagUtil.class.getResourceAsStream("dialog.end")));
		} else if ("IsTagHaveImage".equals(commandStr)) {
            resultHtml.append(getVarTagSafely(javaType).hasImage() ? "true" : "false");
		} else if ("GetVarTagWidth".equals(commandStr)) {
            resultHtml.append(getVarTagSafely(javaType).width);
		} else if ("GetVarTagHeight".equals(commandStr)) {
            resultHtml.append(getVarTagSafely(javaType).height);
        } else if ("IsAvailable".equals(commandStr)) {
            resultHtml.append(!WYSIWYGHTMLEditor.getCurrent().isFtlFormat());
		} else {
		    WYSIWYGPlugin.logInfo("Unknown cmd: " + commandStr);
		}
        IOUtils.writeToStream(response.getOutputStream(), resultHtml.toString());
	}
    
	private VarTagInfo getVarTagSafely(String javaType) {
		VarTagInfo varTagInfo = varTags.get(javaType);
		if (varTagInfo == null) {
			WYSIWYGPlugin.logInfo("VarTag not found: " + javaType);
			varTagInfo = new VarTagInfo(null, javaType, "Tag not found ("+javaType+")", 250, 30, null, false);
		}
		return varTagInfo;
	}
	
}
