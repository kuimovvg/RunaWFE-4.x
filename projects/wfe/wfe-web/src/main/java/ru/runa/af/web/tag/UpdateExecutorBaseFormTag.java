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
package ru.runa.af.web.tag;

import javax.servlet.jsp.JspException;

import ru.runa.common.web.tag.IdentifiableFormTag;
import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.user.Executor;

/**
 * Created on 23.08.2004
 * 
 */
public abstract class UpdateExecutorBaseFormTag extends IdentifiableFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected Identifiable getIdentifiable() throws JspException {
        return getExecutor();
    }

    protected Executor getExecutor() {
        try {
            ExecutorService executorService = DelegateFactory.getExecutorService();
            return executorService.getExecutor(getSubject(), getIdentifiableId());
        } catch (Exception e) {
            return null;
        }
    }
}
