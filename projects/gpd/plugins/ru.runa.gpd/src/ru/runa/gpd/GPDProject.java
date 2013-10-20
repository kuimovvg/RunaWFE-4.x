package ru.runa.gpd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import ru.cg.runaex.shared.util.XmlUtils;
import ru.cg.runaex.shared.bean.project.xml.GroovyFunction;
import ru.cg.runaex.shared.bean.project.xml.GroovyFunctionList;
import ru.cg.runaex.shared.bean.project.xml.Project;

@SuppressWarnings({ "deprecation", "rawtypes" })
public class GPDProject implements IProject {

    public static final String DB_RESOURCES_FOLDER = "db_resources";
    public static final String FUNCTIONS_DESCRIPTOR_FILENAME = "groovy_functions.xml";
    public static final String DATASOURCE_FILE_NAME = "datasource.xml";
    public static final String STRUCTURE_DESCRIPTOR_FILENAME = "structure.xml";

    private IProject project;

    public GPDProject(IProject project) {
        this.project = project;
    }

    /**
     * @param adapter
     * @return
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return project.getAdapter(adapter);
    }

    /**
     * @param rule
     * @return
     * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
     */
    public boolean contains(ISchedulingRule rule) {
        return project.contains(rule);
    }

    /**
     * @param rule
     * @return
     * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
     */
    public boolean isConflicting(ISchedulingRule rule) {
        return project.isConflicting(rule);
    }

    /**
     * @param kind
     * @param builderName
     * @param args
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#build(int, java.lang.String,
     *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void build(int kind, String builderName, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
        project.build(kind, builderName, args, monitor);
    }

    /**
     * @param path
     * @return
     * @see org.eclipse.core.resources.IContainer#exists(org.eclipse.core.runtime.IPath)
     */
    public boolean exists(IPath path) {
        return project.exists(path);
    }

    /**
     * @param path
     * @return
     * @see org.eclipse.core.resources.IContainer#findMember(java.lang.String)
     */
    public IResource findMember(String path) {
        return project.findMember(path);
    }

    /**
     * @param path
     * @param includePhantoms
     * @return
     * @see org.eclipse.core.resources.IContainer#findMember(java.lang.String,
     *      boolean)
     */
    public IResource findMember(String path, boolean includePhantoms) {
        return project.findMember(path, includePhantoms);
    }

    /**
     * @param kind
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#build(int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void build(int kind, IProgressMonitor monitor) throws CoreException {
        project.build(kind, monitor);
    }

    /**
     * @param path
     * @return
     * @see org.eclipse.core.resources.IContainer#findMember(org.eclipse.core.runtime.IPath)
     */
    public IResource findMember(IPath path) {
        return project.findMember(path);
    }

    /**
     * @param config
     * @param kind
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#build(org.eclipse.core.resources.IBuildConfiguration,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void build(IBuildConfiguration config, int kind, IProgressMonitor monitor) throws CoreException {
        project.build(config, kind, monitor);
    }

    /**
     * @param path
     * @param includePhantoms
     * @return
     * @see org.eclipse.core.resources.IContainer#findMember(org.eclipse.core.runtime.IPath,
     *      boolean)
     */
    public IResource findMember(IPath path, boolean includePhantoms) {
        return project.findMember(path, includePhantoms);
    }

    /**
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#close(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void close(IProgressMonitor monitor) throws CoreException {
        project.close(monitor);
    }

    /**
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IContainer#getDefaultCharset()
     */
    public String getDefaultCharset() throws CoreException {
        return project.getDefaultCharset();
    }

    /**
     * @param checkImplicit
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IContainer#getDefaultCharset(boolean)
     */
    public String getDefaultCharset(boolean checkImplicit) throws CoreException {
        return project.getDefaultCharset(checkImplicit);
    }

    /**
     * @param description
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#create(org.eclipse.core.resources.IProjectDescription,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void create(IProjectDescription description, IProgressMonitor monitor) throws CoreException {
        create(description, IResource.NONE, monitor);
    }

    /**
     * @param path
     * @return
     * @see org.eclipse.core.resources.IContainer#getFile(org.eclipse.core.runtime.IPath)
     */
    public IFile getFile(IPath path) {
        return project.getFile(path);
    }

    /**
     * @param visitor
     * @param memberFlags
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#accept(org.eclipse.core.resources.IResourceProxyVisitor,
     *      int)
     */
    public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {
        project.accept(visitor, memberFlags);
    }

    /**
     * @param path
     * @return
     * @see org.eclipse.core.resources.IContainer#getFolder(org.eclipse.core.runtime.IPath)
     */
    public IFolder getFolder(IPath path) {
        return project.getFolder(path);
    }

    /**
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#create(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void create(IProgressMonitor monitor) throws CoreException {
        create(null, monitor);
    }

    /**
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IContainer#members()
     */
    public IResource[] members() throws CoreException {
        return project.members();
    }

    /**
     * @param includePhantoms
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IContainer#members(boolean)
     */
    public IResource[] members(boolean includePhantoms) throws CoreException {
        return project.members(includePhantoms);
    }

    /**
     * @param description
     * @param updateFlags
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#create(org.eclipse.core.resources.IProjectDescription,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void create(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
        project.create(description, updateFlags, monitor);

        createProcessStructureDescriptor(monitor);
        createGroovyFunctionsDescriptor(monitor);
    }

    /**
     * @param memberFlags
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IContainer#members(int)
     */
    public IResource[] members(int memberFlags) throws CoreException {
        return project.members(memberFlags);
    }

    /**
     * @param visitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#accept(org.eclipse.core.resources.IResourceVisitor)
     */
    public void accept(IResourceVisitor visitor) throws CoreException {
        project.accept(visitor);
    }

    /**
     * @param visitor
     * @param depth
     * @param includePhantoms
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#accept(org.eclipse.core.resources.IResourceVisitor,
     *      int, boolean)
     */
    public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {
        project.accept(visitor, depth, includePhantoms);
    }

    /**
     * @param depth
     * @param monitor
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IContainer#findDeletedMembersWithHistory(int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor) throws CoreException {
        return project.findDeletedMembersWithHistory(depth, monitor);
    }

    /**
     * @param deleteContent
     * @param force
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#delete(boolean, boolean,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {
        project.delete(deleteContent, force, monitor);
    }

    /**
     * @param visitor
     * @param depth
     * @param memberFlags
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#accept(org.eclipse.core.resources.IResourceVisitor,
     *      int, int)
     */
    public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
        project.accept(visitor, depth, memberFlags);
    }

    /**
     * @param charset
     * @throws CoreException
     * @deprecated
     * @see org.eclipse.core.resources.IContainer#setDefaultCharset(java.lang.String)
     */
    public void setDefaultCharset(String charset) throws CoreException {
        project.setDefaultCharset(charset);
    }

    /**
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#getActiveBuildConfig()
     */
    public IBuildConfiguration getActiveBuildConfig() throws CoreException {
        return project.getActiveBuildConfig();
    }

    /**
     * @param charset
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IContainer#setDefaultCharset(java.lang.String,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void setDefaultCharset(String charset, IProgressMonitor monitor) throws CoreException {
        project.setDefaultCharset(charset, monitor);
    }

    /**
     * @param configName
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#getBuildConfig(java.lang.String)
     */
    public IBuildConfiguration getBuildConfig(String configName) throws CoreException {
        return project.getBuildConfig(configName);
    }

    /**
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#getBuildConfigs()
     */
    public IBuildConfiguration[] getBuildConfigs() throws CoreException {
        return project.getBuildConfigs();
    }

    /**
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#clearHistory(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void clearHistory(IProgressMonitor monitor) throws CoreException {
        project.clearHistory(monitor);
    }

    /**
     * @param type
     * @param matcherDescription
     * @param updateFlags
     * @param monitor
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IContainer#createFilter(int,
     *      org.eclipse.core.resources.FileInfoMatcherDescription, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public IResourceFilterDescription createFilter(int type, FileInfoMatcherDescription matcherDescription, int updateFlags, IProgressMonitor monitor) throws CoreException {
        return project.createFilter(type, matcherDescription, updateFlags, monitor);
    }

    /**
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#getContentTypeMatcher()
     */
    public IContentTypeMatcher getContentTypeMatcher() throws CoreException {
        return project.getContentTypeMatcher();
    }

    /**
     * @param destination
     * @param force
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#copy(org.eclipse.core.runtime.IPath,
     *      boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
        project.copy(destination, force, monitor);
    }

    /**
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#getDescription()
     */
    public IProjectDescription getDescription() throws CoreException {
        return project.getDescription();
    }

    /**
     * @param name
     * @return
     * @see org.eclipse.core.resources.IProject#getFile(java.lang.String)
     */
    public IFile getFile(String name) {
        return project.getFile(name);
    }

    /**
     * @param name
     * @return
     * @see org.eclipse.core.resources.IProject#getFolder(java.lang.String)
     */
    public IFolder getFolder(String name) {
        return project.getFolder(name);
    }

    /**
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IContainer#getFilters()
     */
    public IResourceFilterDescription[] getFilters() throws CoreException {
        return project.getFilters();
    }

    /**
     * @param natureId
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#getNature(java.lang.String)
     */
    public IProjectNature getNature(String natureId) throws CoreException {
        return project.getNature(natureId);
    }

    /**
     * @param destination
     * @param updateFlags
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#copy(org.eclipse.core.runtime.IPath,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
        project.copy(destination, updateFlags, monitor);
    }

    /**
     * @param plugin
     * @return
     * @deprecated
     * @see org.eclipse.core.resources.IProject#getPluginWorkingLocation(org.eclipse.core.runtime.IPluginDescriptor)
     */
    public IPath getPluginWorkingLocation(IPluginDescriptor plugin) {
        return project.getPluginWorkingLocation(plugin);
    }

    /**
     * @param id
     * @return
     * @see org.eclipse.core.resources.IProject#getWorkingLocation(java.lang.String)
     */
    public IPath getWorkingLocation(String id) {
        return project.getWorkingLocation(id);
    }

    /**
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#getReferencedProjects()
     */
    public IProject[] getReferencedProjects() throws CoreException {
        return project.getReferencedProjects();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IProject#getReferencingProjects()
     */
    public IProject[] getReferencingProjects() {
        return project.getReferencingProjects();
    }

    /**
     * @param configName
     * @param includeMissing
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#getReferencedBuildConfigs(java.lang.String,
     *      boolean)
     */
    public IBuildConfiguration[] getReferencedBuildConfigs(String configName, boolean includeMissing) throws CoreException {
        return project.getReferencedBuildConfigs(configName, includeMissing);
    }

    /**
     * @param configName
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#hasBuildConfig(java.lang.String)
     */
    public boolean hasBuildConfig(String configName) throws CoreException {
        return project.hasBuildConfig(configName);
    }

    /**
     * @param natureId
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#hasNature(java.lang.String)
     */
    public boolean hasNature(String natureId) throws CoreException {
        return project.hasNature(natureId);
    }

    /**
     * @param natureId
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#isNatureEnabled(java.lang.String)
     */
    public boolean isNatureEnabled(String natureId) throws CoreException {
        return project.isNatureEnabled(natureId);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IProject#isOpen()
     */
    public boolean isOpen() {
        return project.isOpen();
    }

    /**
     * @param description
     * @param force
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#copy(org.eclipse.core.resources.IProjectDescription,
     *      boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {
        project.copy(description, force, monitor);
    }

    /**
     * @param options
     * @param snapshotLocation
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#loadSnapshot(int, java.net.URI,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void loadSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor) throws CoreException {
        project.loadSnapshot(options, snapshotLocation, monitor);
    }

    /**
     * @param description
     * @param force
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#move(org.eclipse.core.resources.IProjectDescription,
     *      boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void move(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {
        project.move(description, force, monitor);
    }

    /**
     * @param description
     * @param updateFlags
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#copy(org.eclipse.core.resources.IProjectDescription,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
        project.copy(description, updateFlags, monitor);
    }

    /**
     * @param updateFlags
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#open(int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void open(int updateFlags, IProgressMonitor monitor) throws CoreException {
        project.open(updateFlags, monitor);
    }

    /**
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#open(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void open(IProgressMonitor monitor) throws CoreException {
        project.open(monitor);
    }

    /**
     * @param type
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#createMarker(java.lang.String)
     */
    public IMarker createMarker(String type) throws CoreException {
        return project.createMarker(type);
    }

    /**
     * @param options
     * @param snapshotLocation
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#saveSnapshot(int, java.net.URI,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void saveSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor) throws CoreException {
        project.saveSnapshot(options, snapshotLocation, monitor);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#createProxy()
     */
    public IResourceProxy createProxy() {
        return project.createProxy();
    }

    /**
     * @param force
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#delete(boolean,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
        project.delete(force, monitor);
    }

    /**
     * @param description
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#setDescription(org.eclipse.core.resources.IProjectDescription,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void setDescription(IProjectDescription description, IProgressMonitor monitor) throws CoreException {
        project.setDescription(description, monitor);
    }

    /**
     * @param updateFlags
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#delete(int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
        project.delete(updateFlags, monitor);
    }

    /**
     * @param description
     * @param updateFlags
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IProject#setDescription(org.eclipse.core.resources.IProjectDescription,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void setDescription(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
        project.setDescription(description, updateFlags, monitor);
    }

    /**
     * @param type
     * @param includeSubtypes
     * @param depth
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#deleteMarkers(java.lang.String,
     *      boolean, int)
     */
    public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
        project.deleteMarkers(type, includeSubtypes, depth);
    }

    /**
     * @param other
     * @return
     * @see org.eclipse.core.resources.IResource#equals(java.lang.Object)
     */
    public boolean equals(Object other) {
        return project.equals(other);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#exists()
     */
    public boolean exists() {
        return project.exists();
    }

    /**
     * @param id
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#findMarker(long)
     */
    public IMarker findMarker(long id) throws CoreException {
        return project.findMarker(id);
    }

    /**
     * @param type
     * @param includeSubtypes
     * @param depth
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#findMarkers(java.lang.String,
     *      boolean, int)
     */
    public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
        return project.findMarkers(type, includeSubtypes, depth);
    }

    /**
     * @param type
     * @param includeSubtypes
     * @param depth
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#findMaxProblemSeverity(java.lang.String,
     *      boolean, int)
     */
    public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException {
        return project.findMaxProblemSeverity(type, includeSubtypes, depth);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getFileExtension()
     */
    public String getFileExtension() {
        return project.getFileExtension();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getFullPath()
     */
    public IPath getFullPath() {
        return project.getFullPath();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getLocalTimeStamp()
     */
    public long getLocalTimeStamp() {
        return project.getLocalTimeStamp();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getLocation()
     */
    public IPath getLocation() {
        return project.getLocation();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getLocationURI()
     */
    public URI getLocationURI() {
        return project.getLocationURI();
    }

    /**
     * @param id
     * @return
     * @see org.eclipse.core.resources.IResource#getMarker(long)
     */
    public IMarker getMarker(long id) {
        return project.getMarker(id);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getModificationStamp()
     */
    public long getModificationStamp() {
        return project.getModificationStamp();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getName()
     */
    public String getName() {
        return project.getName();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getPathVariableManager()
     */
    public IPathVariableManager getPathVariableManager() {
        return project.getPathVariableManager();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getParent()
     */
    public IContainer getParent() {
        return project.getParent();
    }

    /**
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#getPersistentProperties()
     */
    public Map<QualifiedName, String> getPersistentProperties() throws CoreException {
        return project.getPersistentProperties();
    }

    /**
     * @param key
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#getPersistentProperty(org.eclipse.core.runtime.QualifiedName)
     */
    public String getPersistentProperty(QualifiedName key) throws CoreException {
        return project.getPersistentProperty(key);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getProject()
     */
    public IProject getProject() {
        return project.getProject();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getProjectRelativePath()
     */
    public IPath getProjectRelativePath() {
        return project.getProjectRelativePath();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getRawLocation()
     */
    public IPath getRawLocation() {
        return project.getRawLocation();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getRawLocationURI()
     */
    public URI getRawLocationURI() {
        return project.getRawLocationURI();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getResourceAttributes()
     */
    public ResourceAttributes getResourceAttributes() {
        return project.getResourceAttributes();
    }

    /**
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#getSessionProperties()
     */
    public Map<QualifiedName, Object> getSessionProperties() throws CoreException {
        return project.getSessionProperties();
    }

    /**
     * @param key
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#getSessionProperty(org.eclipse.core.runtime.QualifiedName)
     */
    public Object getSessionProperty(QualifiedName key) throws CoreException {
        return project.getSessionProperty(key);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getType()
     */
    public int getType() {
        return project.getType();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#getWorkspace()
     */
    public IWorkspace getWorkspace() {
        return project.getWorkspace();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#isAccessible()
     */
    public boolean isAccessible() {
        return project.isAccessible();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#isDerived()
     */
    public boolean isDerived() {
        return project.isDerived();
    }

    /**
     * @param options
     * @return
     * @see org.eclipse.core.resources.IResource#isDerived(int)
     */
    public boolean isDerived(int options) {
        return project.isDerived(options);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#isHidden()
     */
    public boolean isHidden() {
        return project.isHidden();
    }

    /**
     * @param options
     * @return
     * @see org.eclipse.core.resources.IResource#isHidden(int)
     */
    public boolean isHidden(int options) {
        return project.isHidden(options);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#isLinked()
     */
    public boolean isLinked() {
        return project.isLinked();
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#isVirtual()
     */
    public boolean isVirtual() {
        return project.isVirtual();
    }

    /**
     * @param options
     * @return
     * @see org.eclipse.core.resources.IResource#isLinked(int)
     */
    public boolean isLinked(int options) {
        return project.isLinked(options);
    }

    /**
     * @param depth
     * @return
     * @deprecated
     * @see org.eclipse.core.resources.IResource#isLocal(int)
     */
    public boolean isLocal(int depth) {
        return project.isLocal(depth);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#isPhantom()
     */
    public boolean isPhantom() {
        return project.isPhantom();
    }

    /**
     * @return
     * @deprecated
     * @see org.eclipse.core.resources.IResource#isReadOnly()
     */
    public boolean isReadOnly() {
        return project.isReadOnly();
    }

    /**
     * @param depth
     * @return
     * @see org.eclipse.core.resources.IResource#isSynchronized(int)
     */
    public boolean isSynchronized(int depth) {
        return project.isSynchronized(depth);
    }

    /**
     * @return
     * @see org.eclipse.core.resources.IResource#isTeamPrivateMember()
     */
    public boolean isTeamPrivateMember() {
        return project.isTeamPrivateMember();
    }

    /**
     * @param options
     * @return
     * @see org.eclipse.core.resources.IResource#isTeamPrivateMember(int)
     */
    public boolean isTeamPrivateMember(int options) {
        return project.isTeamPrivateMember(options);
    }

    /**
     * @param destination
     * @param force
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#move(org.eclipse.core.runtime.IPath,
     *      boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
        project.move(destination, force, monitor);
    }

    /**
     * @param destination
     * @param updateFlags
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#move(org.eclipse.core.runtime.IPath,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
        project.move(destination, updateFlags, monitor);
    }

    /**
     * @param description
     * @param force
     * @param keepHistory
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#move(org.eclipse.core.resources.IProjectDescription,
     *      boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        project.move(description, force, keepHistory, monitor);
    }

    /**
     * @param description
     * @param updateFlags
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#move(org.eclipse.core.resources.IProjectDescription,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
        project.move(description, updateFlags, monitor);
    }

    /**
     * @param depth
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#refreshLocal(int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
        project.refreshLocal(depth, monitor);
    }

    /**
     * @param value
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#revertModificationStamp(long)
     */
    public void revertModificationStamp(long value) throws CoreException {
        project.revertModificationStamp(value);
    }

    /**
     * @param isDerived
     * @throws CoreException
     * @deprecated
     * @see org.eclipse.core.resources.IResource#setDerived(boolean)
     */
    public void setDerived(boolean isDerived) throws CoreException {
        project.setDerived(isDerived);
    }

    /**
     * @param isDerived
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#setDerived(boolean,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void setDerived(boolean isDerived, IProgressMonitor monitor) throws CoreException {
        project.setDerived(isDerived, monitor);
    }

    /**
     * @param isHidden
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#setHidden(boolean)
     */
    public void setHidden(boolean isHidden) throws CoreException {
        project.setHidden(isHidden);
    }

    /**
     * @param flag
     * @param depth
     * @param monitor
     * @throws CoreException
     * @deprecated
     * @see org.eclipse.core.resources.IResource#setLocal(boolean, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {
        project.setLocal(flag, depth, monitor);
    }

    /**
     * @param value
     * @return
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#setLocalTimeStamp(long)
     */
    public long setLocalTimeStamp(long value) throws CoreException {
        return project.setLocalTimeStamp(value);
    }

    /**
     * @param key
     * @param value
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#setPersistentProperty(org.eclipse.core.runtime.QualifiedName,
     *      java.lang.String)
     */
    public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
        project.setPersistentProperty(key, value);
    }

    /**
     * @param readOnly
     * @deprecated
     * @see org.eclipse.core.resources.IResource#setReadOnly(boolean)
     */
    public void setReadOnly(boolean readOnly) {
        project.setReadOnly(readOnly);
    }

    /**
     * @param attributes
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#setResourceAttributes(org.eclipse.core.resources.ResourceAttributes)
     */
    public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
        project.setResourceAttributes(attributes);
    }

    /**
     * @param key
     * @param value
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#setSessionProperty(org.eclipse.core.runtime.QualifiedName,
     *      java.lang.Object)
     */
    public void setSessionProperty(QualifiedName key, Object value) throws CoreException {
        project.setSessionProperty(key, value);
    }

    /**
     * @param isTeamPrivate
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#setTeamPrivateMember(boolean)
     */
    public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {
        project.setTeamPrivateMember(isTeamPrivate);
    }

    /**
     * @param monitor
     * @throws CoreException
     * @see org.eclipse.core.resources.IResource#touch(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void touch(IProgressMonitor monitor) throws CoreException {
        project.touch(monitor);
    }

    private void createProcessStructureDescriptor(IProgressMonitor monitor) throws CoreException {

        if (!project.isOpen()) {
            project.open(monitor);
        }

        IFile structureDescriptorFile = project.getFile(STRUCTURE_DESCRIPTOR_FILENAME);

        InputStream source = null;
        try {
            if (!structureDescriptorFile.exists()) {
                byte[] bytes = createInitialDescriptor();
                source = new ByteArrayInputStream(bytes);
                structureDescriptorFile.create(source, IResource.NONE, monitor);
            }
        } catch (UnsupportedEncodingException e) {
            PluginLogger.logError(e);
        } finally {
            if (source != null) {
                try {
                    source.close();
                } catch (IOException e) {
                    PluginLogger.logError(e);
                }
            }
        }
    }

    private void createGroovyFunctionsDescriptor(IProgressMonitor monitor) throws CoreException {

        if (!project.isOpen()) {
            project.open(monitor);
        }

        InputStream source = null;
        try {
            IFile file = project.getFile(GPDProject.FUNCTIONS_DESCRIPTOR_FILENAME);
            file.refreshLocal(IResource.DEPTH_ONE, null);
            GroovyFunctionList list = new GroovyFunctionList();
            list.setGroovyFunctionList(new LinkedList<GroovyFunction>());
            String generatedDescriptor = XmlUtils.serializeFunctionList(list);
            if (!file.exists()) {
                file.create(new ByteArrayInputStream(generatedDescriptor.getBytes("UTF-8")), IResource.NONE, null);
            }
        } catch (UnsupportedEncodingException e) {
            PluginLogger.logError(e);
        } finally {
            if (source != null) {
                try {
                    source.close();
                } catch (IOException e) {
                    PluginLogger.logError(e);
                }
            }
        }
    }

    private byte[] createInitialDescriptor() throws UnsupportedEncodingException {
        Project project = new Project();
        project.setProjectName(this.project.getName());
        String generatedDescriptor = XmlUtils.serializeProjectStructure(project);
        return generatedDescriptor.getBytes("UTF-8");
    }

}
