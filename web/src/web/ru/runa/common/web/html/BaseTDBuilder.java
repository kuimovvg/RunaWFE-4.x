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
package ru.runa.common.web.html;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.jsp.JspException;

import org.apache.commons.beanutils.BeanUtils;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.common.web.html.TDBuilder.Env.IdentifiableExtractor;

/**
 * Created 07.07.2005
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public abstract class BaseTDBuilder implements TDBuilder {
    private final Permission permission;
    private final IdentifiableExtractor identifiableExtractor;

    public BaseTDBuilder(Permission permission) {
        this.permission = permission;
        identifiableExtractor = null;
    }

    public BaseTDBuilder(Permission permission, IdentifiableExtractor identifiableExtractor) {
        this.permission = permission;
        this.identifiableExtractor = identifiableExtractor;
    }

    protected boolean isEnabled(Object object, Env env) throws JspException {
        try {
            if (permission == null) {
                return false;
            }
            return env.isAllowed(permission, identifiableExtractor);
        } catch (AuthorizationException e) {
            throw new JspException(e);
        } catch (AuthenticationException e) {
            throw new JspException(e);
        }
    }

    protected String readProperty(Object object, String propertyName, boolean isExceptionOnAbsent) throws JspException {
        try {
            return BeanUtils.getProperty(object, propertyName);
        } catch (IllegalAccessException e) {
            throw new JspException(e);
        } catch (InvocationTargetException e) {
            throw new JspException(e);
        } catch (NoSuchMethodException e) {
            if (isExceptionOnAbsent) {
                throw new JspException(e);
            }
        }
        return "";
    }

    protected IdentifiableExtractor getExtractor() {
        return identifiableExtractor;
    }

    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
