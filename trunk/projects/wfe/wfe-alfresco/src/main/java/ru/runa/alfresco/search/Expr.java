package ru.runa.alfresco.search;

/**
 * Represents search condition.
 * @author dofs
 */
public class Expr {
    N operand;
    Op operator;
    Object[] params;
    
    public Expr() {
    }

    public Expr(N operand, Op operator, Object... params) {
        this.operand = operand;
        this.operator = operator;
        this.params = params;
        if (this.params == null) {
            this.params = new Object[0];
        }
    }

    public Object[] getParams() {
		return params;
	}
    
    @Override
    public String toString() {
        return operator.toExpression(operand, params);
    }
}
