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

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.ConcreteElement;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.user.Profile;

/**
 * Created on 02.09.2004
 */
public abstract class VisibleTag extends TagSupport {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(VisibleTag.class);

    private boolean isVisible = false;

    abstract protected ConcreteElement getEndElement() throws JspException;

    abstract protected ConcreteElement getStartElement() throws JspException;

    protected int doStartTagReturnedValue() throws JspException {
        return Tag.SKIP_BODY;
    }

    protected int doEndTagReturnedValue() throws JspException {
        return Tag.EVAL_PAGE;
    }

    /**
     * Returns <code>true</code>(dafault) if tag content should be displayed, or
     * <code>false</code> otherwise.
     */
    protected boolean isVisible() throws JspException {
        return true;
    }

    @Override
    public int doStartTag() {
        try {
            isVisible = isVisible();
            if (isVisible) {
                JspWriter writer = pageContext.getOut();
                ConcreteElement element = getStartElement();
                element.output(writer);
            }
            return doStartTagReturnedValue();
        } catch (JspException e) {
            throw new InternalApplicationException(e.getCause());
        }
    }

    protected Subject getSubject() {
        return SubjectHttpSessionHelper.getActorSubject(pageContext.getSession());
    }

    protected Profile getProfile() {
        return ProfileHttpSessionHelper.getProfile(pageContext.getSession());
    }

    @Override
    public int doEndTag() {
        try {
            if (isVisible) {
                JspWriter writer = pageContext.getOut();
                ConcreteElement element = getEndElement();
                element.output(writer);
            }
            return doEndTagReturnedValue();
        } catch (JspException e) {
            throw new InternalApplicationException(e.getCause());
        }
    }

    protected void handleException(Exception e) throws JspException {
        log.warn("visibleTag", e);
        throw new JspException(e);
    }
}
