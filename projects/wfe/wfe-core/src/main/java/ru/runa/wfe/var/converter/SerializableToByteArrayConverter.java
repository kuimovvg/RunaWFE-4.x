/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.var.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import ru.runa.wfe.WfException;
import ru.runa.wfe.var.Converter;

public class SerializableToByteArrayConverter implements Converter {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean supports(Object value) {
        if (value == null) {
            return true;
        }
        return Serializable.class.isAssignableFrom(value.getClass());
    }

    @Override
    public Object convert(Object o) {
        try {
            ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(memoryStream);
            objectStream.writeObject(o);
            objectStream.flush();
            return memoryStream.toByteArray();
        } catch (IOException e) {
            throw new WfException("couldn't serialize '" + o + "'", e);
        }
    }

    @Override
    public Object revert(Object o) {
        byte[] bytes = (byte[]) o;
        InputStream memoryStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream objectStream = new ObjectInputStream(memoryStream);
            return objectStream.readObject();
        } catch (IOException ex) {
            throw new WfException("failed to read object", ex);
        } catch (ClassNotFoundException ex) {
            throw new WfException("serialized object class not found", ex);
        }
    }

}
