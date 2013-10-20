package ru.runa.gpd.data;

import ru.runa.gpd.data.provider.GroovyFunctionsDao;
import ru.runa.gpd.data.provider.GroovyFunctionsDaoImpl;
import ru.runa.gpd.data.provider.ProjectStructureDao;
import ru.runa.gpd.data.provider.ProjectStructureDaoImpl;

public class DaoFactory {

	private static final ProjectStructureDao PROJECT_STRUCTURE_DAO = new ProjectStructureDaoImpl();
	private static final GroovyFunctionsDao GROOVY_FUNCTIONS_DAO = new GroovyFunctionsDaoImpl();

	public static ProjectStructureDao getProcessCategoryDao() {
		return PROJECT_STRUCTURE_DAO;
	}

	public static GroovyFunctionsDao getGroovyFunctionsDao() {
		return GROOVY_FUNCTIONS_DAO;
	}
}
