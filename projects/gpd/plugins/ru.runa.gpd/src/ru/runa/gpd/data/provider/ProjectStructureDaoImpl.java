package ru.runa.gpd.data.provider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import ru.cg.runaex.shared.bean.project.xml.Project;
import ru.cg.runaex.shared.util.XmlUtils;
import ru.runa.gpd.GPDProject;
import ru.runa.gpd.PluginLogger;

public class ProjectStructureDaoImpl implements ProjectStructureDao {
	
	private Map<String, Project> CACHE = new WeakHashMap<String, Project>();

	@Override
	public boolean save(Project project) {
		boolean result = false;
		try {
			saveToFile(project);
			
			CACHE.put(project.getProjectName(), project);
			
			result = true;
		} catch (UnsupportedEncodingException ex) {
			PluginLogger.logError("Error saving structure descriptor", ex);						
		} catch (CoreException ex) {
			PluginLogger.logError("Error saving structure descriptor", ex);			
		}
		
		return result;
	}
	
	@Override
	public Project get(String projectName) {	
		Project project = CACHE.get(projectName);
		
		if (project == null) {
			try {
				project = loadFromFile(projectName);
				
				CACHE.put(project.getProjectName(), project);
			} catch (CoreException ex) {
				PluginLogger.logError("Error loading structure descriptor", ex);
			}				
		}
		
		return project;
	}
	
	private void saveToFile(Project project) throws UnsupportedEncodingException, CoreException {
		String serializedDesriptor =  XmlUtils.serializeProjectStructure(project);			
		
		IFile descriptorFile = getDescriptorFile(project.getProjectName());
		descriptorFile.setContents(new ByteArrayInputStream(serializedDesriptor.getBytes("UTF-8")), true, false, null);
	}
	
	private Project loadFromFile(String projectName) throws CoreException {
		IFile descriptorFile = getDescriptorFile(projectName);
		InputStream descriptorStream = descriptorFile.getContents();
		return XmlUtils.deserializeProjectStructure(descriptorStream);
	}
	
	private IFile getDescriptorFile(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace(); 

		//TODO: Filename must be constant
		String descriptorPathStr = projectName.concat("/").concat(GPDProject.STRUCTURE_DESCRIPTOR_FILENAME);
		Path descriptorPath = new Path(descriptorPathStr); 
		return workspace.getRoot().getFile(descriptorPath);
	}

}
