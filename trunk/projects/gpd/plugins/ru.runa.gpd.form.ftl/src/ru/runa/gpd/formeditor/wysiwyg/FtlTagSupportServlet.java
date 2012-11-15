package ru.runa.gpd.formeditor.wysiwyg;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.formeditor.WYSIWYGPlugin;
import ru.runa.gpd.formeditor.ftl.FormatTag;
import ru.runa.gpd.formeditor.ftl.FormatTag.FtlFormat;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil.TagParser;
import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.formeditor.ftl.MethodTag.MethodTagComparator;
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
                    String elementType = "hidden";
                    if (dialogPath.contains("select.js")) {
                        elementType = "select";
                    }
                    if (dialogPath.contains("textfield.js")) {
                        elementType = "text";
                    }
                    if (dialogPath.contains("radio.js")) {
                        elementType = "radio";
                    }
                    if (dialogPath.contains("textarea.js")) {
                        elementType = "textarea";
                    }
                    if (dialogPath.contains("checkbox.js")) {
                        elementType = "checkbox";
                    }
                    if (!ELEMENT_TYPE_FILTERS.containsKey(elementType)) {
                        WYSIWYGPlugin.logInfo("Invalid param: elementType = " + elementType);
                    }
                    newContent.append("items:[");
                    boolean needComma = false;
                    String defaultChoise = "";
                    for (Variable variable : WYSIWYGHTMLEditor.getCurrent().getVariablesList(true)) {
                        String formatAlias = TagParser.getFormatMapping(variable.getFormat()).getName();
                        if (ELEMENT_TYPE_FILTERS.get(elementType).contains(formatAlias)) {
                            newContent.append(needComma ? "," : "").append("['").append(variable.getName()).append("','").append(variable.getName()).append("']");
                            if (!needComma) {
                                defaultChoise = variable.getName();
                            }
                            needComma = true;
                        }
                    }
                    newContent.append("],'default':'").append(defaultChoise).append("',style : 'width : 100%;'");
                    newContent.append(fileContent.substring(idx + 12));
                    fileContent = newContent.toString();
                }
                response.getOutputStream().write(fileContent.getBytes(PluginConstants.UTF_ENCODING));
                return;
            }
            if ("GetMethodDialog".equals(commandStr)) {
                response.setContentType("text/html; charset=UTF-8");
                response.setHeader("Cache-Control", "no-cache");
                IOUtils.writeToStream(response.getOutputStream(), CKEditorDialogCreatorHelper.createFtlMethodDialog());
                return;
            }
            if ("GetOutputDialog".equals(commandStr)) {
                response.setContentType("text/html; charset=UTF-8");
                response.setHeader("Cache-Control", "no-cache");
                IOUtils.writeToStream(response.getOutputStream(), CKEditorDialogCreatorHelper.createFtlOutputDialog());
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
                    if (WYSIWYGHTMLEditor.getCurrent().getVariablesMap(false).containsKey(tagName)) {
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
                resultHtml.append(IOUtils.readStream(FtlFormat.class.getResourceAsStream("ftl.method.dialog.start")));
                List<MethodTag> tagsList = new ArrayList<MethodTag>(MethodTag.getAll().values());
                Collections.sort(tagsList, new MethodTagComparator());
                for (MethodTag tagInfo : tagsList) {
                    resultHtml.append("<option value=\"" + tagInfo.id + "\">" + tagInfo.name + "</option>");
                }
                resultHtml.append(IOUtils.readStream(FtlFormat.class.getResourceAsStream("ftl.method.dialog.end")));
            } else if ("GetAllVariables".equals(commandStr)) {
                resultHtml.append(IOUtils.readStream(FtlFormat.class.getResourceAsStream("ftl.format.dialog.start")));
                for (Variable variable : WYSIWYGHTMLEditor.getCurrent().getVariablesList(false)) {
                    resultHtml.append("<option value=\"").append(variable.getName()).append("\">").append(variable.getName()).append("</option>");
                }
                resultHtml.append(IOUtils.readStream(FtlFormat.class.getResourceAsStream("ftl.format.dialog.end")));
            } else if ("GetParameters".equals(commandStr)) {
                resultHtml.append("<table style=\"width: 100%;\">");
                int paramCounter = 0;
                for (Param param : MethodTag.getTag(tagName).params) {
                    resultHtml.append("<tr><td class='leftParam'>");
                    resultHtml.append(param.displayName);
                    resultHtml.append("</td><td class='rightParam'>");
                    if (param.isCombo() || param.isVarCombo()) {
                        resultHtml.append("<select id=\"pc_").append(paramCounter).append("\">");
                        for (OptionalValue option : param.optionalValues) {
                            if (option.container) {
                                for (Variable variable : WYSIWYGHTMLEditor.getCurrent().getVariablesList(true)) {
                                    String formatAlias = TagParser.getFormatMapping(variable.getFormat()).getName();
                                    if (option.useFilter && !option.filterType.equals(formatAlias)) {
                                        continue;
                                    }
                                    resultHtml.append("<option value=\"").append(variable.getName()).append("\">").append(variable.getName()).append("</option>");
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
                                for (Variable variable : WYSIWYGHTMLEditor.getCurrent().getVariablesList(true)) {
                                    String formatAlias = TagParser.getFormatMapping(variable.getFormat()).getName();
                                    if (option.useFilter && !option.filterType.equals(formatAlias)) {
                                        continue;
                                    }
                                    resultHtml.append("<option value=\"").append(variable.getName()).append("\">").append(variable.getName()).append("</option>");
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
            } else if ("GetFormats".equals(commandStr)) {
                Variable variable = WYSIWYGHTMLEditor.getCurrent().getVariablesMap(false).get(tagName);
                resultHtml.append("<select id=\"tagFormat\">");
                if (variable != null) {
                    String format = TagParser.getFormatMapping(variable.getFormat()).getName();
                    // It may not exist variables at all.
                    FormatTag formatTag = FormatTag.getTag(format);
                    for (String f : formatTag.formats.keySet()) {
                        resultHtml.append("<option value=\"" + f + "\">" + formatTag.formats.get(f).name + "</option>");
                    }
                }
                resultHtml.append("</select>");
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
                String elementType = request.getParameter("elementType");
                if (!ELEMENT_TYPE_FILTERS.containsKey(elementType)) {
                    WYSIWYGPlugin.logInfo("Invalid param: elementType = " + elementType);
                }
                for (Variable variable : WYSIWYGHTMLEditor.getCurrent().getVariablesList(true)) {
                    String formatAlias = TagParser.getFormatMapping(variable.getFormat()).getName();
                    if (ELEMENT_TYPE_FILTERS.get(elementType).contains(formatAlias)) {
                        if (resultHtml.length() > 0) {
                            resultHtml.append("|");
                        }
                        resultHtml.append(variable.getName());
                    }
                }
            } else {
                WYSIWYGPlugin.logInfo("Unknown cmd: " + commandStr);
            }
            IOUtils.writeToStream(response.getOutputStream(), resultHtml.toString());
        } catch (Throwable th) {
            WYSIWYGPlugin.logError("-- JS command error", th);
            response.setStatus(500);
        }
    }

    static Map<String, String> ELEMENT_TYPE_FILTERS = new HashMap<String, String>();
    static {
        ELEMENT_TYPE_FILTERS.put("hidden", "string|double|long|date|time|boolean");
        ELEMENT_TYPE_FILTERS.put("text", "string|double|long");
        ELEMENT_TYPE_FILTERS.put("textarea", "string");
        ELEMENT_TYPE_FILTERS.put("checkbox", "boolean");
        ELEMENT_TYPE_FILTERS.put("file", "file");
        ELEMENT_TYPE_FILTERS.put("radio", "string|double|long|boolean");
        ELEMENT_TYPE_FILTERS.put("select", "string|double|long");
    }
}
