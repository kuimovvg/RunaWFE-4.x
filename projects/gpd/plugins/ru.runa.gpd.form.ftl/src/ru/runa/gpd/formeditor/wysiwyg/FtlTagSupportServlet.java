package ru.runa.gpd.formeditor.wysiwyg;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import ru.runa.gpd.formeditor.WYSIWYGPlugin;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil;
import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.formeditor.ftl.MethodTag.OptionalValue;
import ru.runa.gpd.formeditor.ftl.MethodTag.Param;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.IOUtils;

public class FtlTagSupportServlet extends HttpServlet {
    private static final long serialVersionUID = -4457636711561815927L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            StringBuffer resultHtml = new StringBuffer();
            String commandStr = request.getParameter("method");
            if (commandStr == null) {
                resultHtml.append("invalid request ...");
            }
            String tagName = request.getParameter("tagName");
            if ("CustomizeCommonDialog".equals(commandStr)) {
                String dialogPath = request.getParameter("dialog");
                InputStream stream = FileLocator.openStream(WYSIWYGPlugin.getDefault().getBundle(),
                        new Path(dialogPath.replace("http://localhost:" + WYSIWYGPlugin.SERVER_PORT, "CKEditor")), false);
                String fileContent = IOUtils.readStream(stream);
                {
                    int idx = fileContent.indexOf("type:'text'");
                    StringBuilder newContent = new StringBuilder(fileContent.substring(0, idx));
                    newContent.append("type:'select'");
                    fileContent = fileContent.substring(idx + 11);
                    idx = fileContent.indexOf("'default':''");
                    newContent.append(fileContent.substring(0, idx));
                    String filterClassName = Object.class.getName();
                    if (dialogPath.contains("checkbox.js")) {
                        filterClassName = Boolean.class.getName();
                    }
                    newContent.append("items:[");
                    boolean needComma = false;
                    String defaultChoise = "";
                    List<Variable> variables = new ArrayList<Variable>(WYSIWYGHTMLEditor.getCurrent().getVariables(false, filterClassName).values());
                    Collections.sort(variables);
                    for (Variable variable : variables) {
                        newContent.append(needComma ? "," : "").append("['").append(variable.getName()).append("','").append(variable.getName()).append("']");
                        if (!needComma) {
                            defaultChoise = variable.getName();
                        }
                        needComma = true;
                    }
                    newContent.append("],'default':'").append(defaultChoise).append("',style : 'width : 100%;'");
                    newContent.append(fileContent.substring(idx + 12));
                    fileContent = newContent.toString();
                }
                response.getOutputStream().write(fileContent.getBytes("UTF-8"));
                return;
            }
            if ("GetMethodDialog".equals(commandStr)) {
                response.setContentType("text/html; charset=UTF-8");
                response.setHeader("Cache-Control", "no-cache");
                response.getOutputStream().write(CKEditorDialogCreatorHelper.createFtlMethodDialog().getBytes("UTF-8"));
                response.getOutputStream().flush();
                return;
            }
            if ("GetTagImage".equals(commandStr)) {
                response.setContentType("image/png;");
                InputStream imageStream = null;
                String tagImageName = null;
                if (MethodTag.hasTag(tagName)) {
                    MethodTag tag = MethodTag.getTag(tagName);
                    try {
                        if (tag.hasImage()) {
                            imageStream = tag.openImageStream();
                        }
                    } catch (IOException e) {
                        // Unable to load tag image, using default
                    }
                    tagImageName = "DefaultTag.png";
                } else {
                    if (WYSIWYGHTMLEditor.getCurrent().getVariables(true, null).containsKey(tagName)) {
                        tagImageName = "VariableValueDisplay.png";
                    } else {
                        tagImageName = "TagNotFound.png";
                    }
                }
                if (imageStream == null) {
                    imageStream = WYSIWYGPlugin.loadTagImage(WYSIWYGPlugin.getDefault().getBundle(), "metadata/icons/" + tagImageName);
                }
                IOUtils.copyStream(imageStream, response.getOutputStream());
                return;
            }
            if (commandStr.startsWith("GetAll")) {
                response.setContentType("text/html; charset=UTF-8");
            } else {
                response.setContentType("text/xml; charset=UTF-8");
            }
            response.setHeader("Cache-Control", "no-cache");
            if ("GetAllMethods".equals(commandStr)) {
                resultHtml.append(IOUtils.readStream(FreemarkerUtil.class.getResourceAsStream("ftl.method.dialog.start")));
                for (MethodTag tagInfo : MethodTag.getEnabled()) {
                    resultHtml.append("<option value=\"" + tagInfo.id + "\">" + tagInfo.name + "</option>");
                }
                resultHtml.append(IOUtils.readStream(FreemarkerUtil.class.getResourceAsStream("ftl.method.dialog.end")));
            } else if ("GetAllVariables".equals(commandStr)) {
                resultHtml.append(IOUtils.readStream(FreemarkerUtil.class.getResourceAsStream("ftl.format.dialog.start")));
                List<Variable> variables = new ArrayList<Variable>(WYSIWYGHTMLEditor.getCurrent().getVariables(true, null).values());
                Collections.sort(variables);
                for (Variable variable : variables) {
                    resultHtml.append("<option value=\"").append(variable.getName()).append("\">").append(variable.getName()).append("</option>");
                }
                resultHtml.append(IOUtils.readStream(FreemarkerUtil.class.getResourceAsStream("ftl.format.dialog.end")));
            } else if ("GetParameters".equals(commandStr)) {
                resultHtml.append("<table style=\"width: 100%;\" style=\"vertical-align: top;\">");
                int paramCounter = 0;
                for (Param param : MethodTag.getTag(tagName).params) {
                    resultHtml.append("<tr><td class='leftParam' style=\"vertical-align: top;\">");
                    resultHtml.append(param.label);
                    resultHtml.append("</td><td class='rightParam'>");
                    if (param.isCombo() || param.isVarCombo()) {
                        resultHtml.append("<select id=\"pc_").append(paramCounter).append("\">");
                        for (OptionalValue option : param.optionalValues) {
                            if (option.container) {
                                List<Variable> variables;
                                if (option.useFilter) {
                                    variables = new ArrayList<Variable>(WYSIWYGHTMLEditor.getCurrent().getVariables(param.isVarCombo(), option.filterType).values());
                                } else {
                                    variables = new ArrayList<Variable>(WYSIWYGHTMLEditor.getCurrent().getVariables(param.isVarCombo(), null).values());
                                }
                                Collections.sort(variables);
                                for (Variable variable : variables) {
                                    resultHtml.append("<option value=\"").append(variable.getName()).append("\"");
                                    if (option.filterType != null && !option.filterType.equals(variable.getJavaClassName())) {
                                        resultHtml.append(" style=\"color: #aaa;\"");
                                    }
                                    resultHtml.append(">").append(variable.getName()).append("</option>");
                                }
                            } else {
                                resultHtml.append("<option value=\"").append(option.name).append("\">").append(option.value).append("</option>");
                            }
                        }
                        resultHtml.append("</select>");
                    } else if (param.isRichCombo()) {
                        resultHtml.append("<select id=\"pc_").append(paramCounter).append("\">");
                        resultHtml.append("<option value=\"\">").append(WYSIWYGPlugin.getResourceString("message.choose")).append("</option>");
                        for (OptionalValue option : param.optionalValues) {
                            if (option.container) {
                                List<Variable> variables;
                                if (option.useFilter) {
                                    variables = new ArrayList<Variable>(WYSIWYGHTMLEditor.getCurrent().getVariables(true, option.filterType).values());
                                } else {
                                    variables = new ArrayList<Variable>(WYSIWYGHTMLEditor.getCurrent().getVariables(true, null).values());
                                }
                                Collections.sort(variables);
                                for (Variable variable : variables) {
                                    resultHtml.append("<option value=\"").append(variable.getName()).append("\"");
                                    if (option.filterType != null && !option.filterType.equals(variable.getJavaClassName())) {
                                        resultHtml.append(" style=\"color: #aaa;\"");
                                    }
                                    resultHtml.append(">").append(variable.getName()).append("</option>");
                                }
                            } else {
                                resultHtml.append("<option value=\"").append(option.name).append("\">").append(option.value).append("</option>");
                            }
                        }
                        resultHtml.append("</select>");
                        resultHtml.append(WYSIWYGPlugin.getResourceString("message.richcombo.or")).append("<br />");
                        resultHtml.append("<input id=\"pt_").append(paramCounter).append("\"/>");
                    } else {
                        resultHtml.append("<input id=\"pt_").append(paramCounter).append("\"/>");
                    }
                    resultHtml.append("</td></tr>");
                    paramCounter++;
                }
                resultHtml.append("</table>");
            } else if ("GetTagImage".equals(commandStr)) {
                // resultHtml.append(MethodTag.getTag(tagName).image);
            } else if ("GetVarTagWidth".equals(commandStr)) {
                resultHtml.append(MethodTag.getTag(tagName).width);
            } else if ("GetVarTagHeight".equals(commandStr)) {
                resultHtml.append(MethodTag.getTag(tagName).height);
            } else if ("IsAvailable".equals(commandStr)) {
                resultHtml.append(WYSIWYGHTMLEditor.getCurrent().isFtlFormat());
                WYSIWYGHTMLEditor.getCurrent().setBrowserLoaded(true);
            } else if ("GetVariableNames".equals(commandStr)) {
                String filterClassName = Object.class.getName();
                if ("checkbox".equals(request.getParameter("elementType"))) {
                    filterClassName = Boolean.class.getName();
                }
                if ("file".equals(request.getParameter("elementType"))) {
                    filterClassName = "ru.runa.wfe.var.FileVariable";
                }
                List<Variable> variables = new ArrayList<Variable>(WYSIWYGHTMLEditor.getCurrent().getVariables(false, filterClassName).values());
                Collections.sort(variables);
                for (Variable variable : variables) {
                    if (resultHtml.length() > 0) {
                        resultHtml.append("|");
                    }
                    resultHtml.append(variable.getName());
                }
            } else {
                WYSIWYGPlugin.logInfo("Unknown cmd: " + commandStr);
            }
            response.getOutputStream().write(resultHtml.toString().getBytes("UTF-8"));
            response.getOutputStream().flush();
        } catch (Throwable th) {
            WYSIWYGPlugin.logError("-- JS command error", th);
            response.setStatus(500);
        }
    }
}
