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
package ru.runa.wf.web.html;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.common.web.html.TDBuilder.Env;
import ru.runa.common.web.html.TDBuilder.Env.IdentifiableExtractor;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;

/**
 * Creates {@link Identifiable} to check permissions on {@link Actor}, executed
 * action.
 */
public class SystemLogActorExtractor implements IdentifiableExtractor {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(SystemLogActorExtractor.class);
    private final Map<Long, Identifiable> cache = new HashMap<Long, Identifiable>();

    @Override
    public Identifiable getIdentifiable(Object o, Env env) {
        SystemLog systemLog = (SystemLog) o;
        try {
            if (!cache.containsKey(systemLog.getActorId())) {
                cache.put(systemLog.getActorId(), Delegates.getExecutorService().getExecutor(env.getUser(), systemLog.getActorId()));
            }
            return cache.get(systemLog.getActorId());
        } catch (Exception e) {
            log.error("Can't load actor for system log with id " + systemLog.getId(), e);
        }
        return null;
    }
}
