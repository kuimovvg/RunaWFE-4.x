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

import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONObject;

import com.google.common.base.Objects;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.FileVariable;

/**
 * This class is marker class for validation.
 */
public class FileFormat extends VariableFormat implements VariableDisplaySupport {

    @Override
    public Class<? extends FileVariable> getJavaClass() {
        return FileVariable.class;
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String convertToStringValue(Object object) {
        return ((FileVariable) object).getName();
    }

    @Override
    public FileVariable convertFromStringValue(String string) throws Exception {
        throw new UnsupportedOperationException("file variable cannot be deserializes from string");
    }

    @Override
    protected Object convertToJSONValue(Object value) {
        FileVariable fileVariable = (FileVariable) value;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName", fileVariable.getName());
        jsonObject.put("contentType", fileVariable.getContentType());
        jsonObject.put("data", DatatypeConverter.printBase64Binary(fileVariable.getData()));
        return jsonObject;
    }
    
    @Override
    protected Object convertFromJSONValue(Object jsonValue) {
        JSONObject object = (JSONObject) jsonValue;
        String fileName = (String) object.get("fileName");
        if (fileName == null) {
            throw new InternalApplicationException("Attribute 'fileName' is not set in " + object);
        }
        String contentType = (String) object.get("contentType");
        if (contentType == null) {
            throw new InternalApplicationException("Attribute 'contentType' is not set in " + object);
        }
        String data = (String) object.get("data");
        if (data == null) {
            throw new InternalApplicationException("Attribute 'data' is not set in " + object);
        }
        return new FileVariable(fileName, DatatypeConverter.parseBase64Binary(data), contentType);
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object, Object context) {
        Integer index = null;
        Object key = null;
        if (context instanceof List) {
            index = ((List) context).indexOf(object);
        }
        if (context instanceof Map) {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) context).entrySet()) {
                if (Objects.equal(object, entry.getValue())) {
                    key = entry.getKey();
                    break;
                }
            }
        }
        return FormatCommons.getFileOutput(webHelper, processId, name, (FileVariable) object, index, key);
    }
    
}
