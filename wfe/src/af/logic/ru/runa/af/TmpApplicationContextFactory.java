package ru.runa.af;


import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.RelationDAO;
import ru.runa.af.dao.SecuredObjectDAO;
import ru.runa.af.dao.SubstitutionDAO;
import ru.runa.af.logic.CommonLogic;
import ru.runa.af.logic.ExecutorLogic;
import ru.runa.commons.ApplicationContextFactory;

public class TmpApplicationContextFactory extends ApplicationContextFactory {
    // TODO avoid this

    public static ExecutorDAO getExecutorDAO() {
        return getContext().getBean(ExecutorDAO.class);
    }
    public static SecuredObjectDAO getSecuredObjectDAO() {
        return getContext().getBean(SecuredObjectDAO.class);
    }
    public static RelationDAO getRelationDAO() {
        return getContext().getBean(RelationDAO.class);
    }
    public static SubstitutionDAO getSubstitutionDAO() {
        return getContext().getBean(SubstitutionDAO.class);
    }
    public static CommonLogic getCommonLogic() {
        return getContext().getBean(ExecutorLogic.class);
    }

}
