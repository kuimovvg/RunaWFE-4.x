package ru.runa.common.web.action;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.HTMLUtils;
import ru.runa.wfe.var.FileVariable;

public class GetSessionFileVariableAction extends Action {
    private static final Log log = LogFactory.getLog(GetSessionFileVariableAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String fileName = request.getParameter("fileName");
        try {
            FileVariable fileVariable = (FileVariable) request.getSession().getAttribute(fileName);
            response.setContentType(fileVariable.getContentType());
            String encodedFileName = HTMLUtils.encodeFileName(fileName, request.getHeader("User-Agent"));
            response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();
            os.write(fileVariable.getData());
            os.flush();
        } catch (Exception e) {
            log.error("No file found: " + fileName, e);
        }
        return null;
    }

}
