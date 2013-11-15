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
package ru.runa.wf.web.tag;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Area;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;

import com.google.common.collect.Maps;

/**
 * Helper class to create links to subprocesses in graph elements.
 */
public class SubprocessGraphElementPresentationHelper {

    /**
     * Current task identity.
     */
    private final Long taskId;

    /**
     * Rendered page context.
     */
    private final PageContext pageContext;

    /**
     * Root form element.
     */
    private final TD formDataTD;

    /**
     * Created map of elements, represents links and tool tips areas.
     */
    private final org.apache.ecs.html.Map map;

    /**
     * Action, to be performed on subprocess link clicked.
     */
    private final String linkAction;

    /**
     * Creates instance of helper class to create links to subprocesses in graph
     * elements.
     * 
     * @param taskId
     *            Current task identity. May be <= 0 if not applicable.
     * @param pageContext
     *            Rendered page context.
     * @param formDataTD
     *            Root form element.
     * @param map
     *            Created map of elements, represents links and tool tips areas.
     * @param linkAction
     *            Action, to be performed on subprocess link clicked.
     */
    public SubprocessGraphElementPresentationHelper(Long taskId, PageContext pageContext, TD formDataTD, org.apache.ecs.html.Map map,
            String linkAction) {
        this.taskId = taskId;
        this.pageContext = pageContext;
        this.formDataTD = formDataTD;
        this.map = map;
        this.linkAction = linkAction;
    }

    /**
     * Creates links to subprocesses, forked in given multiple instance graph
     * element.
     * 
     * @param element
     *            Multiple instance graph element to create links.
     */
    public void createMultiinstanceLinks(MultiinstanceGraphElementPresentation element) {
        if (!element.isReadPermission()) {
            return;
        }
        int mlSize = 17;
        int maxItemsPerLine = 10;
        int additionalHeight = 0;
        int mainDivSize = mlSize * element.getSubprocessIds().size();
        if (mainDivSize > maxItemsPerLine * mlSize) {
            additionalHeight = (int) Math.ceil(((double) mainDivSize / (maxItemsPerLine * mlSize))) * mlSize;
            mainDivSize = maxItemsPerLine * mlSize;
        }
        int[] ltCoords = new int[] { element.getGraphConstraints()[2] - mainDivSize / 2,
                element.getGraphConstraints()[3] + mlSize / 2 + additionalHeight };
        StringBuffer buf = new StringBuffer();
        buf.append("<div class=\"multiInstanceContainer\" style=\"");
        buf.append("width: ").append(mainDivSize).append("px;");
        buf.append("left: ").append(ltCoords[0]).append("px;");
        buf.append("top: ").append(ltCoords[1]).append("px;\">");
        for (int i = 0; i < element.getSubprocessIds().size(); i++) {
            Long subprocessId = element.getSubprocessIds().get(i);
            buf.append("<div class=\"multiInstanceBox\" style=\"width: ");
            buf.append(mlSize).append("px; height: ").append(mlSize).append("px;\" ");
            buf.append("onmouseover=\"this.style.backgroundColor='gray';\" onmouseout=\"this.style.backgroundColor='white';\">");
            buf.append("<a href=\"").append(getSubprocessUrl(subprocessId)).append("\" style>&nbsp;").append(i + 1).append("&nbsp;</a>");
            buf.append("</div>");
            if ((i + 1) % maxItemsPerLine == 0) {
                buf.append("\n");
            }
        }
        buf.append("</div>");
        formDataTD.addElement(buf.toString());
    }

    /**
     * Create link to subprocess, forked in given subprocess graph element.
     * 
     * @param element
     *            Subprocess graph element to create link.
     * @return
     */
    public Area createSubprocessLink(SubprocessGraphElementPresentation element) {
        if (!element.isReadPermission()) {
            return null;
        }
        String url;
        if (element.isEmbedded()) {
            url = "javascript:showEmbeddedSubprocess(" + element.getSubprocessId() + ", '" + element.getSubprocessName() + "');";
        } else {
            url = getSubprocessUrl(element.getSubprocessId());
        }
        Area area = new Area("RECT", element.getGraphConstraints());
        area.setHref(url);
        area.setTitle(element.getSubprocessName());
        map.addElement(area);
        return area;
    }

    /**
     * Creates URL to subprocess with given identity.
     * 
     * @param id
     *            Identity of subprocess.
     * @return URL to subprocess.
     */
    private String getSubprocessUrl(Long id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, id);
        params.put(TaskIdForm.TASK_ID_INPUT_NAME, taskId);
        return Commons.getActionUrl(linkAction, params, pageContext, PortletUrlType.Render);
    }
}
