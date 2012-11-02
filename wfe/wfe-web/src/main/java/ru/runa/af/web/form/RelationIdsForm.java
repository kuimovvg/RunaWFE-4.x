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
package ru.runa.af.web.form;

import ru.runa.common.web.form.IdsForm;

/**
 * @struts:form name = "relationIdsForm"
 */
public class RelationIdsForm extends IdsForm {
    private static final long serialVersionUID = 1L;
    private String relationName;
    private String executorId;

    private String onSuccess;
    private String onFailure;

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getExecutorId() {
        return executorId;
    }

    public void setExecutorId(String executorId) {
        this.executorId = executorId;
    }

    public String getSuccess() {
        return onSuccess;
    }

    public void setSuccess(String onSuccess) {
        this.onSuccess = onSuccess;
    }

    public String getFailure() {
        return onFailure;
    }

    public void setFailure(String onFailure) {
        this.onFailure = onFailure;
    }
}
