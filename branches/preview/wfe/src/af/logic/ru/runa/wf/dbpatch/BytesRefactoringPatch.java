package ru.runa.wf.dbpatch;

import ru.runa.InternalApplicationException;
import ru.runa.commons.dbpatch.DBPatchBase;

public class BytesRefactoringPatch extends DBPatchBase {

    @Override
    public void applyPatch() {
        throw new InternalApplicationException("WFE before version 3.4.3 does not supported to patch DB");
    }

}
