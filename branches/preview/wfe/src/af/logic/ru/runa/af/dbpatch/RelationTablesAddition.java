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
package ru.runa.af.dbpatch;

import ru.runa.af.SecuredObjectAlreadyExistsException;
import ru.runa.commons.dbpatch.DBPatchBase;

public class RelationTablesAddition extends DBPatchBase {
    @Override
    public void applyPatch() throws SecuredObjectAlreadyExistsException {
//        List<ColumnDef> columnDefinitions = new ArrayList<DBPatchBase.ColumnDef>();
//        columnDefinitions.add(new ColumnDef("ID", Types.BIGINT).setPrimaryKey());
//        columnDefinitions.add(new ColumnDef("NAME", dialect.getTypeName(java.sql.Types.VARCHAR, 255, 255, 255), false));
//        columnDefinitions.add(new ColumnDef("DESCRIPTION", dialect.getTypeName(java.sql.Types.VARCHAR, 255, 255, 255), false));
//        createTable("RELATION_GROUPS", columnDefinitions, "(NAME)");
//        columnDefinitions = new ArrayList<DBPatchBase.ColumnDef>();
//        columnDefinitions.add(new ColumnDef("ID", Types.BIGINT).setPrimaryKey());
//        columnDefinitions.add(new ColumnDef("EXECUTOR_FROM", Types.BIGINT, false));
//        columnDefinitions.add(new ColumnDef("RELATION_GROUP", Types.BIGINT, false));
//        columnDefinitions.add(new ColumnDef("EXECUTOR_TO", Types.BIGINT, false));
//        createTable("EXECUTOR_RELATIONS", columnDefinitions, null);
//        createForeignKey("FK_RELATION_FROM_EXECUTOR", "EXECUTOR_RELATIONS", "EXECUTOR_FROM", "EXECUTORS", "ID");
//        createForeignKey("FK_RELATION_TO_EXECUTOR", "EXECUTOR_RELATIONS", "EXECUTOR_TO", "EXECUTORS", "ID");
//        createForeignKey("FK_RELATION_GROUP_ID", "EXECUTOR_RELATIONS", "RELATION_GROUP", "RELATION_GROUPS", "ID");
//        createIndex("IDX_RELATION_FROM_EXECUTOR", "EXECUTOR_RELATIONS", "EXECUTOR_FROM");
//        createIndex("IDX_RELATION_TO_EXECUTOR", "EXECUTOR_RELATIONS", "EXECUTOR_TO");
//        createIndex("IDX_RELATION_GROUP_ID", "EXECUTOR_RELATIONS", "RELATION_GROUP");
//        long adminId = ((Number) session.createSQLQuery(
//                "SELECT ID FROM EXECUTORS WHERE NAME = '" + InitializerResources.getDefaultAdministratorsGroupName() + "'").uniqueResult())
//                .longValue();
//        SecuredObject relationsGroupSecuredObject = daoHolder.getSecuredObjectDAO().create(RelationsGroupSecure.INSTANCE);
//        if (isPostgreSQL()) {
//            session.flush();
//            session.createSQLQuery(
//                    "INSERT INTO SECURED_OBJECT_TYPES (ID, PERMISSION_CLASS_NAME, TYPE_CODE) VALUES (nextval('hibernate_sequence'), '"
//                            + RelationPermission.class.getName() + "', " + RelationsGroupSecure.class.getName().hashCode() + ")").executeUpdate();
//            session.createSQLQuery(
//                    "INSERT INTO SECURED_OBJECT_TYPES (ID, PERMISSION_CLASS_NAME, TYPE_CODE) VALUES (nextval('hibernate_sequence'), '"
//                            + RelationPermission.class.getName() + "', " + Relation.class.getName().hashCode() + ")").executeUpdate();
//            List<Number> ids = session.createSQLQuery(
//                    "SELECT ID FROM SECURED_OBJECT_TYPES WHERE PERMISSION_CLASS_NAME='" + RelationPermission.class.getName() + "'").list();
//            for (Number number : ids) {
//                session.createSQLQuery("INSERT INTO PRIVELEGE_MAPPINGS (TYPE_CODE, EXECUTOR_ID) VALUES (" + number.longValue() + ", " + adminId + ")")
//                        .executeUpdate();
//            }
//            for (Permission permission : new RelationPermission().getAllPermissions()) {
//                session.createSQLQuery(
//                        "INSERT INTO PERMISSION_MAPPINGS (ID, MASK, VERSION, EXECUTOR_ID, SECURED_OBJECT_ID) VALUES (nextval('hibernate_sequence'), "
//                                + permission.getMask() + ", 1, " + adminId + ", " + relationsGroupSecuredObject.getId() + ")").executeUpdate();
//            }
//        } else {
//            session.createSQLQuery(
//                    "INSERT INTO SECURED_OBJECT_TYPES (PERMISSION_CLASS_NAME, TYPE_CODE) VALUES ('" + RelationPermission.class.getName() + "', "
//                            + RelationsGroupSecure.class.getName().hashCode() + ")").executeUpdate();
//            session.createSQLQuery(
//                    "INSERT INTO SECURED_OBJECT_TYPES (PERMISSION_CLASS_NAME, TYPE_CODE) VALUES ('" + RelationPermission.class.getName() + "', "
//                            + Relation.class.getName().hashCode() + ")").executeUpdate();
//            List<Number> ids = session.createSQLQuery(
//                    "SELECT ID FROM SECURED_OBJECT_TYPES WHERE PERMISSION_CLASS_NAME='" + RelationPermission.class.getName() + "'").list();
//            for (Number number : ids) {
//                session.createSQLQuery("INSERT INTO PRIVELEGE_MAPPINGS (TYPE_CODE, EXECUTOR_ID) VALUES (" + number.longValue() + ", " + adminId + ")")
//                        .executeUpdate();
//            }
//            for (Permission permission : new RelationPermission().getAllPermissions()) {
//                session.createSQLQuery(
//                        "INSERT INTO PERMISSION_MAPPINGS (MASK, VERSION, EXECUTOR_ID, SECURED_OBJECT_ID) VALUES (" + permission.getMask() + ", 1, "
//                                + adminId + ", " + relationsGroupSecuredObject.getId() + ")").executeUpdate();
//            }
//        }
        throw new UnsupportedOperationException();
    }
}
