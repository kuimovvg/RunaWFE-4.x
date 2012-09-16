package ru.runa.bpm.ui.bsh;

import java.util.ArrayList;
import java.util.List;

import ru.runa.bpm.ui.bsh.BSHTypeSupport.StringType;
import ru.runa.bpm.ui.common.model.Variable;
import ru.runa.bpm.ui.resource.Messages;

public class Operation {
    private static final List<Operation> OPERATIONS_LIST = new ArrayList<Operation>();
    public static final String VOID = "void";
    
    public static final Operation EQ = new Eq();
    public static final Operation NOT_EQ = new NotEq();

    static void registerOperation(Operation operation) {
        OPERATIONS_LIST.add(operation);
    }
    
    static class Eq extends Operation {

        private Eq() {
            super(Messages.getString("BSH.Operation.equals"), "==");
            registerOperation(this);
        }
        
        @Override
        public String generateCode(Variable variable, Object lexem2) {
            if ("null".equals(lexem2)) {
                return variable.getName() + " == void";
            }
            BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(variable.getFormat());
            if (typeSupport instanceof StringType) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(typeSupport.wrap(variable));
                buffer.append(".equals(");
                buffer.append(typeSupport.wrap(lexem2));
                buffer.append(")");
                return buffer.toString();
            }
            return super.generateCode(variable, lexem2);
        }

    }

    static class NotEq extends Operation {

        private NotEq() {
            super(Messages.getString("BSH.Operation.notequals"), "!=");
            registerOperation(this);
        }
        
        @Override
        public String generateCode(Variable variable, Object lexem2) {
            if ("null".equals(lexem2)) {
                return variable.getName() + " != void";
            }
            BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(variable.getFormat());
            if (typeSupport instanceof StringType) {
                StringBuffer buffer = new StringBuffer("!");
                buffer.append(typeSupport.wrap(variable));
                buffer.append(".equals(");
                buffer.append(typeSupport.wrap(lexem2));
                buffer.append(")");
                return buffer.toString();
            }
            return super.generateCode(variable, lexem2);
        }

    }

    private String visibleName;

    private String operator;

    public Operation(String visibleName, String operator) {
        this.visibleName = visibleName;
        this.operator = operator;
    }

    public String getVisibleName() {
        return visibleName;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return operator.hashCode() + 37 * visibleName.hashCode();
    }

    public String generateCode(Variable variable, Object lexem2) {
        BSHTypeSupport typeSupport = BSHTypeSupport.getByFormat(variable.getFormat());

        StringBuffer buffer = new StringBuffer();
        buffer.append(typeSupport.wrap(variable));
        buffer.append(" ");
        buffer.append(getOperator());
        buffer.append(" ");
        buffer.append(typeSupport.wrap(lexem2));

        return buffer.toString();
    }

    public static List<Operation> getAll(BSHTypeSupport typeSupport) {
        List<Operation> allWithExt = new ArrayList<Operation>();
        allWithExt.addAll(OPERATIONS_LIST);
        if (typeSupport == null) {
            return null;
        }
        List<Operation> extOperations = typeSupport.getTypedOperations();
        if (extOperations != null) {
            allWithExt.addAll(extOperations);
        }
        return allWithExt;
    }

    public static Operation getByName(String name, BSHTypeSupport typeSupport) {
        for (Operation operation : getAll(typeSupport)) {
            if (operation.getVisibleName().equals(name)) {
                return operation;
            }
        }
        return null;
    }

    public static Operation getByOperator(String operator, BSHTypeSupport typeSupport) {
        for (Operation operation : getAll(typeSupport)) {
            if (operation.getOperator().equals(operator)) {
                return operation;
            }
        }
        return null;
    }
}
