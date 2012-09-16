package ru.runa.wf.dbpatch;

import ru.runa.commons.dbpatch.DBPatchBase;

public class SpringIntegrationPatch extends DBPatchBase {

    @Override
    protected void applyPatch() throws Exception {
        removeColumn("JBPM_DELEGATION", "CONFIGTYPE_");
        try {
            removeTable("PROPERTY_IDS");
        } catch (Exception e) {
            // may not exist
            log.warn("Unable to delete: " + e.getMessage());
        }
        removeColumn("JBPM_PROCESSDEFINITION", "CLASS_");

    }
    
}
