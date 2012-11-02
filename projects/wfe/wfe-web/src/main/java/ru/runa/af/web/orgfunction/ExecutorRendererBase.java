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
package ru.runa.af.web.orgfunction;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import ru.runa.wfe.os.ParamRenderer;
import ru.runa.wfe.user.Executor;

public abstract class ExecutorRendererBase implements ParamRenderer {

    public boolean hasJSEditor() {
        return true;
    }

    public List<String[]> loadJSEditorData(Subject subject) throws Exception {
        List<String[]> result = new ArrayList<String[]>();
        List<? extends Executor> executors = loadExecutors(subject);
        if (executors.size() == 0) {
            result.add(new String[] { "0", "No executors found" });
        } else {
            for (Executor executor : executors) {
                result.add(new String[] { getValue(executor), executor.getName() });
            }
        }
        return result;
    }

    protected abstract List<? extends Executor> loadExecutors(Subject subject) throws Exception;

    protected abstract String getValue(Executor executor);

    public String getDisplayLabel(Subject subject, String value) {
        try {
            return getExecutor(subject, value).getName();
        } catch (Exception e) {
            return "<span class='error'>" + e.getMessage() + "</span>";
        }
    }

    public boolean isValueValid(Subject subject, String value) {
        try {
            getExecutor(subject, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected abstract Executor getExecutor(Subject subject, String value) throws Exception;
}
