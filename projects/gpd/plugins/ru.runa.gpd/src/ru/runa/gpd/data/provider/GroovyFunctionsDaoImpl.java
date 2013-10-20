package ru.runa.gpd.data.provider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.GPDProject;
import ru.runa.gpd.PluginLogger;

import ru.cg.runaex.shared.util.XmlUtils;
import ru.cg.runaex.shared.bean.project.xml.GroovyFunctionList;

public class GroovyFunctionsDaoImpl implements GroovyFunctionsDao {

    private Map<String, GroovyFunctionList> CACHE = new WeakHashMap<String, GroovyFunctionList>();

    @Override
    public boolean save(IProject project, GroovyFunctionList functionList) {
        boolean result = false;
        try {
            saveToFile(project, functionList);

            CACHE.put(project.getName(), functionList);

            result = true;
        } catch (UnsupportedEncodingException ex) {
            PluginLogger.logError("Error saving groovy functions descriptor", ex);
        } catch (CoreException ex) {
            PluginLogger.logError("Error saving groovy functions descriptor", ex);
        }

        return result;
    }

    @Override
    public GroovyFunctionList get(IProject project) {
        GroovyFunctionList functionList = CACHE.get(project.getName());

        if (functionList == null) {
            try {
                functionList = loadFromFile(project);
                CACHE.put(project.getName(), functionList);
            } catch (CoreException ex) {
                PluginLogger.logError("Error loading groovy functions descriptor", ex);
            } catch (UnsupportedEncodingException ex) {
                PluginLogger.logError("Error loading groovy functions descriptor", ex);
            }
        }

        return functionList;
    }

    private void saveToFile(IProject project, GroovyFunctionList functionList) throws UnsupportedEncodingException, CoreException {
        String serializedDesriptor = XmlUtils.serializeFunctionList(functionList);
        IFile descriptorFile = getDescriptorFile(project);
        descriptorFile.setContents(new ByteArrayInputStream(serializedDesriptor.getBytes("UTF-8")), true, false, null);
    }

    private GroovyFunctionList loadFromFile(IProject project) throws CoreException, UnsupportedEncodingException {
        IFile descriptorFile = getDescriptorFile(project);
        InputStream descriptorStream = descriptorFile.getContents();
        return XmlUtils.deserializeFunctionList(descriptorStream);
    }

    private IFile getDescriptorFile(IProject project) throws CoreException {
        IFile file = project.getFile(GPDProject.FUNCTIONS_DESCRIPTOR_FILENAME);
        file.refreshLocal(IResource.DEPTH_ONE, null);
        return file;
    }

}
