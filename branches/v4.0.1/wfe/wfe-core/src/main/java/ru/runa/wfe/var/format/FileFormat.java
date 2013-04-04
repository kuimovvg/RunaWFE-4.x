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
package ru.runa.wfe.var.format;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.FileVariable;

import com.google.common.collect.Maps;

/**
 * This class is marker class for validation.
 */
public class FileFormat implements VariableFormat<FileVariable>, VariableDisplaySupport<FileVariable> {

    @Override
    public String format(FileVariable object) {
        return object.getName();
    }

    @Override
    public FileVariable parse(String[] strings) {
        throw new UnsupportedOperationException("file variable cannot be representable as string");
    }

    @Override
    public String getHtml(User user, WebHelper webHelper, Long processId, String name, FileVariable value) {
        return getHtml(value.getName(), webHelper, processId, name, 0, null);
    }

    public static String getHtml(String fileName, WebHelper webHelper, Long processId, String name, int listIndex, Object mapKey) {
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("id", processId);
        params.put("variableName", name);
        if (listIndex != 0) {
            params.put("listIndex", String.valueOf(listIndex));
        }
        if (mapKey != null) {
            params.put("mapKey", String.valueOf(mapKey));
        }
        return getHtml(fileName, webHelper, params);
    }

    public static String getHtml(String fileName, WebHelper webHelper, Long logId) {
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("logId", logId);
        return getHtml(fileName, webHelper, params);
    }

    private static String getHtml(String fileName, WebHelper webHelper, Map<String, Object> params) {
        String href = webHelper.getActionUrl("/variableDownloader", params);
        return "<a href=\"" + href + "\">" + fileName + "</>";
    }
}
