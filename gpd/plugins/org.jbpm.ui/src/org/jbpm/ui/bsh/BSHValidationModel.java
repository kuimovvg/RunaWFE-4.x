package ru.runa.bpm.ui.bsh;

import java.util.List;

import ru.runa.bpm.ui.common.model.Variable;

public class BSHValidationModel {

    private static List<Variable> variables;

    public static Expr fromCode(String code, List<Variable> vars) {
        if (code.length() == 0)
            return null;
        variables = vars;

        String[] strings = code.split(" ");
        // tmp
        String lexem1Text = "";
        String operator;
        String lexem2Text = "";
        if ((strings.length == 1) || (code.indexOf("\"") > 0)) {
            // i.e. var1.equals(var2) or var1.contains(var2)
            int start;
            if (code.charAt(0) != '!') {
                start = 0;
                if (code.contains("equals")) {
                    operator = "==";
                } else {
                    operator = "contains";
                }
            } else {
                start = 1;
                operator = "!=";
            }
            lexem1Text = code.substring(start, code.indexOf("."));
            lexem2Text = code.substring(code.indexOf("(") + 1, code.length() - 1);
        } else {
            lexem1Text = strings[0];
            operator = strings[1];
            if (strings.length == 3) {
                lexem2Text = strings[2];
            } else {
                for (int i = 2; i < strings.length; i++) {
                    lexem2Text += " " + strings[i];
                }
            }
        }

        if (lexem1Text.indexOf(".") > 0) {
            // Java names doesn't allowed use of point in variable name
            lexem1Text = lexem1Text.substring(0, lexem1Text.indexOf("."));
        }

        Variable var1 = getVariableByName(lexem1Text);
        if (var1 == null) {
            // variable deleted
            return null;
        }
        BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(var1.getFormat());

        Operation operation = Operation.getByOperator(operator, typeSupport);
        if (operation == null) {
            throw new NullPointerException("operation not found for operator: " + operator);
        }

        if (lexem2Text.indexOf(".") > 0) {
            // Java names doesn't allowed use of point in variable name
            lexem2Text = lexem2Text.substring(0, lexem2Text.indexOf("."));
        }
        Variable var2 = getVariableByName(lexem2Text);
        if (var2 == null) {
            return null;
        }

        return new Expr(var1, var2, operation);
    }

    private static Variable getVariableByName(String variableName) {
        for (Variable variable : variables) {
            if (variable.getName().equals(variableName)) {
                return variable;
            }
        }
        return null;
    }

    public static class Expr {
        private Variable var1;

        private Variable var2;

        private Operation operation;

        public Expr(Variable var1, Variable var2, Operation operation) {
            this.var1 = var1;
            this.var2 = var2;
            this.operation = operation;
        }

        public String generateCode() {
            return operation.generateCode(var1, var2);
        }

        public Variable getVar1() {
            return var1;
        }

        public Variable getVar2() {
            return var2;
        }

        public Operation getOperation() {
            return operation;
        }

    }

}
