package ru.runa.wfe.var;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ComplexVariable extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ComplexVariable.class);

    @Override
    public Object get(Object key) {
        Object object = super.get(key);
        log.debug("Returned " + object + " as '" + key + "' value");
        return object;
    }
        
}
