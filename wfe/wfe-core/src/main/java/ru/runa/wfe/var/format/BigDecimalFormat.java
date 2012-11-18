package ru.runa.wfe.var.format;

import java.math.BigDecimal;

/**
 * BigDecimal variable support.
 * 
 * @author dofs
 * @since 4.0
 */
public class BigDecimalFormat implements VariableFormat<BigDecimal> {

    @Override
    public BigDecimal parse(String[] source) throws Exception {
        return new BigDecimal(source[0]);
    }

    @Override
    public String format(BigDecimal object) {
        return String.valueOf(object);
    }

}
