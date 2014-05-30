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
package ru.runa.wfe.extension.orgfunction;

import java.util.List;

import ru.runa.wfe.extension.OrgFunction;
import ru.runa.wfe.extension.OrgFunctionException;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Created on 08.01.2007
 **/
public abstract class GetActorsOrgFunctionBase extends OrgFunction {

    @Override
    public final List<? extends Executor> getExecutors(Object... parameters) throws OrgFunctionException {
        try {
            List<Executor> result = Lists.newArrayListWithExpectedSize(parameters.length);
            for (Object parameter : parameters) {
                if (parameter instanceof Executor) {
                    result.add((Executor) parameter);
                } else {
                    List<Long> codes = getActorCodes(parameter);
                    result.addAll(executorDAO.getActorsByCodes(codes));
                }
            }
            log.debug("Executors result: " + result);
            return result;
        } catch (Exception e) {
            Throwables.propagateIfPossible(e, OrgFunctionException.class);
            throw new OrgFunctionException(e);
        }
    }

    protected abstract List<Long> getActorCodes(Object... parameters);

}
