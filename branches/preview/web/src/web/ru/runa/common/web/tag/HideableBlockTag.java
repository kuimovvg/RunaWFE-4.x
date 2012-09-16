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
package ru.runa.common.web.tag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.HideableBlockForm;

/**
 * Created on 01.02.2005
 * 
 * @jsp.tag name = "hideableBlock" body-content = "JSP"
 */
public class HideableBlockTag extends AbstractReturningTag {
    private static final long serialVersionUID = -7454349951667153041L;

    private String showTitle = "";

    private String hideTitle = "";

    private String hideableBlockId;

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public String getHideTitle() {
        return hideTitle;
    }

    public void setHideTitle(String hideTitle) {
        this.hideTitle = hideTitle;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public String getShowTitle() {
        return showTitle;
    }

    public void setShowTitle(String showTitle) {
        this.showTitle = showTitle;
    }

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    public void setHideableBlockId(String id) {
        hideableBlockId = id;
    }

    public String getHideableBlockId() {
        return hideableBlockId;
    }

    public int doStartTag() throws JspException {
        JspWriter jspOut = pageContext.getOut();
        try {
            jspOut.println(new Table().createStartTag());
            TR tr = new TR();
            TD headerTD = new TD();
            tr.addElement(headerTD);
            headerTD.setClass(Resources.CLASS_HIDABLEBLOCK);
            Map<String, String> params = new HashMap<String, String>();
            params.put(HideableBlockForm.HIDEABLE_BLOCK_ID, getHideableBlockId());
            params.put(HideableBlockForm.RETURN_ACTION, getReturnAction());

            boolean isContentVisible = isContentVisible();
            addStartOptionalContent(headerTD, isContentVisible);
            headerTD.addElement(Entities.NBSP);

            String href = Commons.getActionUrl(getAction(), params, pageContext, PortletUrl.Action);
            A link = new A(href);
            headerTD.addElement(link);
            link.setClass(Resources.CLASS_HIDABLEBLOCK);
            String imgLink = isContentVisible ? Resources.VISIBLE_IMAGE : Resources.HIDDEN_IMAGE;
            IMG img = new IMG(Commons.getUrl(imgLink, pageContext, PortletUrl.Resource));
            link.addElement(img);
            String imgAlt = isContentVisible ? Resources.VISIBLE_ALT : Resources.HIDDEN_ALT;
            img.setAlt(imgAlt);
            img.setClass(Resources.CLASS_HIDABLEBLOCK);
            String title = isContentVisible ? getHideTitle() : getShowTitle();
            link.addElement(title);

            headerTD.addElement(Entities.NBSP);
            addEndOptionalContent(headerTD, isContentVisible);

            tr.output(jspOut);
            jspOut.println(new TR().createStartTag());
            jspOut.println(new TD().createStartTag());
            return isContentVisible ? EVAL_BODY_INCLUDE : SKIP_BODY;
        } catch (RuntimeException e) {
            return SKIP_BODY;
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    public void addStartOptionalContent(TD td, boolean isVisible) {
    }

    public void addEndOptionalContent(TD td, boolean isVisible) throws JspException {
    }

    private boolean isContentVisible() {
        return ProfileHttpSessionHelper.getProfile(pageContext.getSession()).isBlockVisible(getHideableBlockId());
    }

    public int doEndTag() throws JspException {
        JspWriter jspOut = pageContext.getOut();
        try {
            jspOut.println(new TD().createEndTag());
            jspOut.println(new TR().createEndTag());
            jspOut.println(new Table().createEndTag());
        } catch (IOException e) {
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }
}
