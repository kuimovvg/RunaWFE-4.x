package ru.runa.wf.dbpatch;

import java.sql.Types;

import ru.runa.commons.dbpatch.DBPatchBase;

public class ChangeStringVariableMaxSize extends DBPatchBase {

    public void applyPatch() {
        modifyColumn("JBPM_VARIABLEINSTANCE", "STRINGVALUE_", dialect.getTypeName(Types.VARCHAR, 1024, 0, 0));
    }

}
