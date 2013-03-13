package ru.runa.wf.web;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class leaved in this namespace for compatibility with v3x only.
 * 
 * @author Dofs
 * 
 */
public class Option extends ru.runa.wfe.commons.web.Option {
    private static final long serialVersionUID = 1L;

    public Option(String value, String label) {
        super(value, label);
    }

    // http://lingpipe-blog.com/2009/08/10/serializing-immutable-singletons-serialization-proxy/
    private static class SerializationProxy implements Externalizable {
        private static final long serialVersionUID = -4163914145668867283L;
        String value;
        String label;

        @SuppressWarnings("unused")
        public SerializationProxy() {
        }

        public SerializationProxy(Option option) {
            value = option.getValue();
            label = option.getLabel();
        }

        Object readResolve() {
            return new Option(value, label);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            value = (String) in.readObject();
            label = (String) in.readObject();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(value);
            out.writeObject(label);
        }
    }

}
