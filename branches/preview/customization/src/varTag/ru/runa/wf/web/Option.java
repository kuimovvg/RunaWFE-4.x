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
package ru.runa.wf.web;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import ru.runa.bpm.context.exe.ISelectable;

public class Option implements ISelectable, Serializable {
    private static final long serialVersionUID = 1L;
    private final String value;
    private final String text;

    public Option(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return text;
    }

    @Override
    public String toString() {
        return value + "(" + text + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Option) {
            return value.equals(((Option) obj).value);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    // http://lingpipe-blog.com/2009/08/10/serializing-immutable-singletons-serialization-proxy/
    private static class SerializationProxy implements Externalizable {
        private static final long serialVersionUID = -4163914145668867283L;
        String value;
        String text;

        @SuppressWarnings("unused")
        public SerializationProxy() {
        }

        public SerializationProxy(Option option) {
            value = option.value;
            text = option.text;
        }

        Object readResolve() {
            return new Option(value, text);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            value = (String) in.readObject();
            text = (String) in.readObject();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(value);
            out.writeObject(text);
        }
    }
}
