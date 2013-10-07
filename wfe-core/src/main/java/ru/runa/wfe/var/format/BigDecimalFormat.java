package ru.runa.wfe.var.format;

import java.math.BigDecimal;

/**
 * BigDecimal variable support.
 * 
 * @author dofs
 * @since 4.0
 */
public class BigDecimalFormat implements VariableFormat {

    @Override
    public Class<BigDecimal> getJavaClass() {
        return BigDecimal.class;
    }

    @Override
    public BigDecimal parse(String source) throws Exception {
        if (source == null) {
            return null;
        }
        return new BigDecimal(source);
    }

    @Override
    public String format(Object object) {
        return object != null ? object.toString() : null;
    }

}
