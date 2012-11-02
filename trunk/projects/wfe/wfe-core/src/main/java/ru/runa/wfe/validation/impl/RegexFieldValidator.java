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
package ru.runa.wfe.validation.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.commons.TypeConversionUtil;

import com.google.common.base.Strings;

public class RegexFieldValidator extends FieldValidatorSupport {

    private String expression;
    private boolean caseSensitive = true;
    private boolean trim = true;

    @Override
    public void validate() {
        String value = TypeConversionUtil.convertTo(getFieldValue(), String.class);
        // if there is no value - don't do comparison
        // if a value is required, a required validator should be added to the field
        if (Strings.isNullOrEmpty(value) || expression == null) {
            return;
        }
        // match against expression
        Pattern pattern;
        if (isCaseSensitive()) {
            pattern = Pattern.compile(expression);
        } else {
            pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        }

        String compare = value;
        if (trim) {
            compare = compare.trim();
        }
        Matcher matcher = pattern.matcher(compare);

        if (!matcher.matches()) {
            addFieldError();
        }
    }

    /**
     * @return Returns the regular expression to be matched.
     */
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * @return Returns whether the expression should be matched against in a case-sensitive way. Default is <code>true</code>.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * @return Returns whether the expression should be trimed before matching. Default is <code>true</code>.
     */
    public boolean isTrimed() {
        return trim;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

}
