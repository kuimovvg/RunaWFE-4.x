package ru.runa.gpd.formeditor.vartag;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;

import ru.runa.gpd.formeditor.WYSIWYGPlugin;

public class VarTagUtil {
    private static Pattern patternCustomTag = Pattern.compile("<customtag(.*?)></customtag>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static Pattern patternBugInCustomTag = Pattern.compile("<customtag delegation=\"(.*?)\" var=\"(.*?)\"(\\s*?)/>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static Pattern patternTextarea = Pattern.compile("<textarea(.*?)>(.*?)</textarea>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static Pattern patternMeta = Pattern.compile("<meta(.*?)>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    public static String normalizeVarTags(String html) {
        // <textarea>
        Matcher matcher = patternTextarea.matcher(html);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String group1 = matcher.group(1);
            String customtagData2 = matcher.group(2);
            customtagData2 = customtagData2.replaceAll("&quot;", "\"");
            customtagData2 = customtagData2.replaceAll("&lt;", "<");
            customtagData2 = customtagData2.replaceAll("&gt;", ">");
            matcher.appendReplacement(buffer, Matcher.quoteReplacement("<textarea" + group1 + ">" + customtagData2 + "</textarea>"));
        }
        if (buffer.length() > 0) {
            matcher.appendTail(buffer);
            html = buffer.toString();
        }
        // bug in closing customtag tag
        matcher = patternCustomTag.matcher(html);
        buffer = new StringBuffer();
        while (matcher.find()) {
            String customtagData = matcher.group(1);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement("<customtag" + customtagData + "/>"));
        }
        if (buffer.length() > 0) {
            matcher.appendTail(buffer);
            html = buffer.toString();
        }
        // bug in closing customtag tag
        matcher = patternBugInCustomTag.matcher(html);
        buffer = new StringBuffer();
        while (matcher.find()) {
            String delegation = matcher.group(1);
            String varValue = matcher.group(2);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement("<customtag var=\"" + varValue + "\" delegation=\"" + delegation + "\"/>"));
        }
        if (buffer.length() > 0) {
            matcher.appendTail(buffer);
            html = buffer.toString();
        }
        // removing all meta tags
        matcher = patternMeta.matcher(html);
        buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(""));
        }
        if (buffer.length() > 0) {
            matcher.appendTail(buffer);
            html = buffer.toString();
        }
        return html;
    }

    private static Map<String, VarTagInfo> varTags;

    public static Map<String, VarTagInfo> getVarTagsInfo() {
        if (varTags == null) {
            loadInternal();
        }
        return varTags;
    }

    public static VarTagInfo getVarTagInfo(String varTagName) {
        return getVarTagsInfo().get(varTagName);
    }

    public static final int DEFAULT_WIDTH = 250;
    public static final int DEFAULT_HEIGHT = 40;

    private static void loadInternal() {
        try {
            varTags = new HashMap<String, VarTagInfo>();
            //            IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("tk.eclipse.plugin.wysiwyg.var_tags").getExtensions();
            //            for (IExtension extension : extensions) {
            //                Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
            //                IConfigurationElement[] tagElements = extension.getConfigurationElements();
            //                for (IConfigurationElement tagElement : tagElements) {
            //                    String className = tagElement.getAttribute("className");
            //                    String name = tagElement.getAttribute("name");
            //                    String image = tagElement.getAttribute("image");
            //                    int width = getIntAttr(tagElement, "width", DEFAULT_WIDTH);
            //                    int height = getIntAttr(tagElement, "height", DEFAULT_HEIGHT);
            //                    boolean inputTag = Boolean.valueOf(tagElement.getAttribute("input"));
            //                    VarTagInfo tag = new VarTagInfo(bundle, className, name, width, height, image, inputTag);
            //                    varTags.put(className, tag);
            //                }
            //            }
        } catch (Exception e) {
            WYSIWYGPlugin.logError("Unable to load VarTags", e);
        }
    }

    private static int getIntAttr(IConfigurationElement element, String attrName, int defaultValue) {
        String attr = element.getAttribute(attrName);
        if (attr == null) {
            return defaultValue;
        }
        return Integer.parseInt(attr);
    }
}
