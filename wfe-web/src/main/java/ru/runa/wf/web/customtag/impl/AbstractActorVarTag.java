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
package ru.runa.wf.web.customtag.impl;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.Actor;

/**
 * Created on 09.05.2005
 * 
 */
public abstract class AbstractActorVarTag implements VarTag {
    private static final Log log = LogFactory.getLog(AbstractActorVarTag.class);

    @Override
    final public String getHtml(Subject subject, String varName, Object var, PageContext pageContext) throws Exception {
        if (var == null) {
            log.warn("Vartag variable is not set: " + varName);
            return "<p class='error'>null</p>";
        }
        Long code = TypeConversionUtil.convertTo(var, Long.class);
        Actor actor = getActor(subject, code);
        return actorToString(actor);
    }

    public abstract String actorToString(Actor actor);

    private Actor getActor(Subject subject, long code) {
        return DelegateFactory.getExecutorService().getActorByCode(subject, code);
    }

}
