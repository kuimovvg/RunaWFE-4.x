package ru.runa.common.web.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.WebResources;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.ViewLogForm;
import ru.runa.wfe.commons.IOCommons;

import com.google.common.io.Closeables;

/**
 * 
 * @author dofs
 * 
 * @struts:action path="/viewLogs" name="viewLogForm" validate="false"
 * @struts.action-forward name="success" path="/displayLogs.do" redirect =
 *                        "false"
 */
public class ViewLogsAction extends ActionBase {
    public static final String ACTION_PATH = "/viewLogs";
    private static int limitLinesCount = WebResources.getViewLogsLimitLinesCount();
    private static int autoReloadTimeoutSec = WebResources.getViewLogsAutoReloadTimeout();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            String logDirPath = IOCommons.getLogDirPath();
            request.setAttribute("logDirPath", logDirPath);
            ViewLogForm form = (ViewLogForm) actionForm;
            if (form.getFileName() != null) {
                File file = new File(logDirPath, form.getFileName());
                int allLinesCount = countLines(file);
                form.setAllLinesCount(allLinesCount);

                if (form.getMode() == ViewLogForm.MODE_PAGING) {
                    request.setAttribute("pagingToolbar", createPagingToolbar(form));
                }

                String logFileContent;
                if (form.getMode() == ViewLogForm.MODE_SEARCH) {
                    List<Integer> lineNumbers = new ArrayList<Integer>();
                    String lines = searchLines(file, form, lineNumbers);
                    StringBuffer b = new StringBuffer(lines.length() + 200);
                    b.append("<table class=\"log\"><tr><td class=\"lineNumbers\">");
                    for (Integer num : lineNumbers) {
                        b.append(num).append("<br>");
                    }
                    b.append("</td><td class=\"content\">");
                    b.append(lines);
                    b.append("</td></tr></table>");
                    logFileContent = b.toString();
                } else if (form.getMode() == ViewLogForm.MODE_ERRORS_AND_WARNS) {
                    List<Integer> lineNumbers = new ArrayList<Integer>();
                    String lines = searchErrorsAndWarns(file, lineNumbers);
                    StringBuffer b = new StringBuffer(lines.length() + 200);
                    b.append("<table class=\"log\"><tr><td class=\"lineNumbers\">");
                    for (Integer num : lineNumbers) {
                        b.append(num).append("<br>");
                    }
                    b.append("</td><td class=\"content\">");
                    b.append(lines);
                    b.append("</td></tr></table>");
                    logFileContent = b.toString();
                } else {
                    String lines = readLines(file, form);
                    StringBuffer b = new StringBuffer(lines.length() + 200);
                    b.append("<table class=\"log\"><tr><td class=\"lineNumbers\">");
                    for (int i = form.getStartLine(); i <= form.getEndLine(); i++) {
                        b.append(i).append("<br>");
                    }
                    b.append("</td><td class=\"content\">");
                    b.append(lines);
                    b.append("</td></tr></table>");
                    logFileContent = b.toString();
                }
                request.setAttribute("logFileContent", logFileContent);
            }
            form.setLimitLinesCount(limitLinesCount);
            request.setAttribute("autoReloadTimeoutSec", autoReloadTimeoutSec);
        } catch (Exception e) {
            addError(request, e);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

    private int countLines(File file) throws IOException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            while ((readChars = is.read(c)) != -1) {
                for (int i = 0; i < readChars; i++) {
                    if (c[i] == '\n') {
                        count++;
                    }
                }
            }
            if (count > 0) {
                // count last line
                count++;
            }
            return count;
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    private String createPagingToolbar(ViewLogForm form) {
        if (form.getAllLinesCount() > limitLinesCount) {
            StringBuffer b = new StringBuffer();
            int n = form.getAllLinesCount() / limitLinesCount;
            if (form.getAllLinesCount() % limitLinesCount != 0) {
                n++;
            }
            for (int i = 0; i < n; i++) {
                int startFrom = i * limitLinesCount + 1;
                int endTo = startFrom + limitLinesCount - 1;
                String text;
                if (i == n - 1) {
                    text = "[" + startFrom + "-*]";
                } else {
                    text = "[" + startFrom + "-" + endTo + "]";
                }
                String href = "/wfe" + ViewLogsAction.ACTION_PATH + ".do?fileName=" + form.getFileName() + "&mode=1&startLine=" + startFrom
                        + "&endLine=" + endTo;
                b.append("<a href=\"").append(href).append("\">").append(text).append("</a>&nbsp;&nbsp;&nbsp;");
            }
            return b.toString();
        }
        return null;
    }

    private String readLines(File file, ViewLogForm form) throws IOException {
        if (form.getEndLine() - form.getStartLine() > limitLinesCount) {
            form.setEndLine(form.getStartLine() + limitLinesCount - 1);
        }
        int startLineNumber = form.getStartLine();
        int endLineNumber = form.getEndLine();
        InputStream is = null;
        try {
            int initialSize = (endLineNumber - startLineNumber) * 100;
            if (initialSize <= 0) {
                initialSize = 1000;
            }
            StringBuffer b = new StringBuffer(initialSize);
            is = new FileInputStream(file);
            LineNumberReader lnReader = new LineNumberReader(new InputStreamReader(is));
            String line = lnReader.readLine();
            while (line != null) {
                if (lnReader.getLineNumber() >= startLineNumber) {
                    if (endLineNumber != 0 && lnReader.getLineNumber() > endLineNumber) {
                        break;
                    }
                    line = StringEscapeUtils.escapeHtml(line);
                    line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                    b.append(line).append("<br>");
                }
                line = lnReader.readLine();
            }
            for (int i = lnReader.getLineNumber() + 1; i <= endLineNumber; i++) {
                b.append("<br>");
            }
            return b.toString();
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    private String searchLines(File file, ViewLogForm form, List<Integer> lineNumbers) throws IOException {
        InputStream is = null;
        try {
            StringBuffer b = new StringBuffer(1000);
            is = new FileInputStream(file);
            LineNumberReader lnReader = new LineNumberReader(new InputStreamReader(is));
            String line = lnReader.readLine();
            int i = 1;
            while (line != null) {
                boolean result;
                if (form.isSearchCaseSensitive()) {
                    result = StringUtils.contains(line, form.getSearch());
                } else {
                    result = StringUtils.containsIgnoreCase(line, form.getSearch());
                }
                if (result) {
                    line = StringEscapeUtils.escapeHtml(line);
                    line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                    b.append(line).append("<br>");
                    lineNumbers.add(i);
                    if (lineNumbers.size() > limitLinesCount) {
                        break;
                    }
                }
                line = lnReader.readLine();
                i++;
            }
            return b.toString();
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    private String searchErrorsAndWarns(File file, List<Integer> lineNumbers) throws IOException {
        InputStream is = null;
        try {
            // TODO may be use more structured parsing
            // http://logging.apache.org/log4j/companions/receivers/apidocs/org/apache/log4j/varia/LogFilePatternReceiver.html
            StringBuffer b = new StringBuffer(1000);
            is = new FileInputStream(file);
            LineNumberReader lnReader = new LineNumberReader(new InputStreamReader(is));
            String line = lnReader.readLine();
            int i = 1;
            boolean found = false;
            while (line != null) {
                if (found && line.length() > 0 && (Character.isWhitespace(line.charAt(0)) || Character.isLetter(line.charAt(0)))) {
                    line = StringEscapeUtils.escapeHtml(line);
                    line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                    b.append(line).append("<br>");
                    lineNumbers.add(i);
                } else {
                    found = StringUtils.contains(line, " ERROR ") || StringUtils.contains(line, " WARN ");
                    if (found) {
                        line = StringEscapeUtils.escapeHtml(line);
                        line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                        b.append(line).append("<br>");
                        lineNumbers.add(i);
                    }
                }
                line = lnReader.readLine();
                i++;
            }
            return b.toString();
        } finally {
            Closeables.closeQuietly(is);
        }
    }
}