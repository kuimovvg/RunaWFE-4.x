package ru.runa.gpd.data.provider;

import ru.cg.runaex.shared.bean.project.xml.Project;

public interface ProjectStructureDao {
	
	boolean save(Project project);
	
	Project get(String projectName);
}
