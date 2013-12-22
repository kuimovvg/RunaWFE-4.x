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
package ru.runa.wf.web.action;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.form.VariableForm;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.FileVariable;

import com.google.common.base.Objects;

/**
 * Created on 27.09.2005
 * 
 * @struts:action path="/variableDownloader" name="variableForm"
 *                validate="false"
 */
public class VariableDownloaderAction extends ActionBase {
    public static final String ACTION_PATH = "/variableDownloader";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            FileVariable fileVariable = getVariable(actionForm, request);
            response.setContentType(fileVariable.getContentType());
            // http://forum.java.sun.com/thread.jspa?forumID=45&threadID=233446
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            // non-ascii filenames (Opera does not support it)
            String encodedFileName = HTMLUtils.encodeFileName(request, fileVariable.getName());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();
            os.write(fileVariable.getData());
            os.flush();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    private FileVariable getVariable(ActionForm actionForm, HttpServletRequest request) {
        VariableForm form = (VariableForm) actionForm;
        Object object;
        if (form.getLogId() != null) {
            object = Delegates.getExecutionService().getProcessLogValue(getLoggedUser(request), form.getLogId());
        } else {
            object = Delegates.getExecutionService().getVariable(getLoggedUser(request), form.getId(), form.getVariableName()).getValue();
        }
        if (object instanceof FileVariable) {
            return (FileVariable) object;
        }
        if (object instanceof List<?>) {
            List<FileVariable> list = (List<FileVariable>) object;
            return list.get(form.getListIndex());
        }
        if (object instanceof Map<?, ?>) {
            Map<Object, FileVariable> map = (Map<Object, FileVariable>) object;
            for (Map.Entry<Object, FileVariable> entry : map.entrySet()) {
                if (Objects.equal(String.valueOf(entry.getKey()), form.getMapKey())) {
                    return entry.getValue();
                }
            }
            throw new IllegalArgumentException("No file found by key = " + form.getMapKey() + "; all values: " + map);
        }
        throw new IllegalArgumentException("Unexpected variable type: " + object + " by name " + form.getVariableName());
    }

}
