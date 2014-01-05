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

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.FileVariable;

/**
 * This class is marker class for validation.
 */
public class FileFormat implements VariableFormat {

    @Override
    public Class<? extends FileVariable> getJavaClass() {
        return FileVariable.class;
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String format(Object object) {
        if (object == null) {
            return null;
        }
        FileVariable fileVariable = (FileVariable) object;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName", fileVariable.getName());
        jsonObject.put("contentType", fileVariable.getContentType());
        jsonObject.put("data", DatatypeConverter.printBase64Binary(fileVariable.getData()));
        return jsonObject.toString();
        //return ((FileVariable) object).getName();
    }

    @Override
    public FileVariable parse(String string) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(string);
        String fileName = (String) object.get("fileName");
        if (fileName == null) {
            throw new InternalApplicationException("Attribute 'fileName' is not set in " + string);
        }
        String contentType = (String) object.get("contentType");
        if (contentType == null) {
            throw new InternalApplicationException("Attribute 'contentType' is not set in " + string);
        }
        String data = (String) object.get("data");
        if (data == null) {
            throw new InternalApplicationException("Attribute 'data' is not set in " + string);
        }
        return new FileVariable(fileName, DatatypeConverter.parseBase64Binary(data), contentType);
    }

}
