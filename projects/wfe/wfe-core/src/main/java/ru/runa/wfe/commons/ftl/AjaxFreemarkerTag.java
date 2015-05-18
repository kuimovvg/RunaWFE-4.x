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
package ru.runa.wfe.commons.ftl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Objects;

public abstract class AjaxFreemarkerTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;
    public static final String TAG_SESSION_PREFIX = "ftltag_";

    /**
     * Used only if multiple tags of the same type used in same form.
     *
     * @return qualifier, usually variable name
     */
    public String getQualifier() {
        return getParameterAsString(0);
    }

    @Override
    protected final Object executeTag() throws Exception {
        return renderRequest();
    }

    /**
     * Is invoked on first (static) page rendering
     *
     * @return tag html
     */
    protected abstract String renderRequest() throws Exception;

    /**
     * Invoked on ajax request
     */
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("qualifier", getQualifier()).toString();
    }
}
