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

import org.apache.ecs.html.TD;

import ru.runa.af.AuthenticationException;
import ru.runa.af.Identifiable;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TDBuilder.Env.IdentifiableExtractor;
import ru.runa.wf.ProcessInstanceStub;

/**
 * @author Konstantinov Aleksey
 */
public class ProcessInstanceVariableTDBuilder implements TDBuilder {

    class ProcessInstanceIdExtractor implements IdentifiableExtractor {
        public Identifiable getIdentifiable(final Object o, final Env env) {
            return new Identifiable() {
                public Long getId() {
                    return ((ProcessInstanceStub) o).getId();
                }

                public int identifiableType() {
                    return 0;
                }
            };
        }

    }

    String varName = null;

    public ProcessInstanceVariableTDBuilder(String varName) {
        this.varName = varName;
    }

    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getValue(object, env));
        return td;
    }

    public String getValue(Object object, Env env) {
        String value = "";
        try {
            Object val = env.getTaskVariable(object, new ProcessInstanceIdExtractor(), varName);
            if (val != null) {
                value = val.toString();
            }
        } catch (AuthenticationException e) {
        }
        return value;
    }

    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
