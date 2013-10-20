package ru.runa.gpd.util;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base32;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Schema;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.runa.gpd.GPDProject;

public final class DatabaseStructureUtil {

    private static final String CONNECTION_URL = "connection-url";
    private static final String DRIVER_CLASS = "driver-class";
    private static final String USER_NAME = "user-name";
    private static final String USER_PASSWORD = "password";
    private static final String JNDI_NAME = "jndi-name";
    private static final String XML_EXTENSION = "xml";
    private static final String UTF_8 = "UTF-8";

    private static Database getDatabaseObjects(DriverManagerDataSource dataSource) throws SQLException {
        Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);
        Connection connection = platform.borrowConnection();
        List<String> schemas = new ArrayList<String>();
        ResultSet rs = null;
        Database model = null;
        try {
            DatabaseMetaData dmd = connection.getMetaData();
            rs = dmd.getSchemas();
            while (rs.next()) {
                schemas.add(rs.getString("TABLE_SCHEM"));
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        try {
            model = platform.readModelFromDatabase(connection, null, null, schemas, null);

        } finally {
            platform.returnConnection(connection);
        }
        return model;
    }

    public static List<Schema> getDbExplorerResourcesForProject(IProject project, IProgressMonitor monitor) throws CoreException, SQLException, UnsupportedEncodingException {
        Database database = null;
        try {
            monitor.beginTask("Loading ...", 100);
            if (project != null) {
                IFolder dbResourcesFolder = project.getFolder(GPDProject.DB_RESOURCES_FOLDER);
                dbResourcesFolder.refreshLocal(IResource.DEPTH_INFINITE, null);

                if (!dbResourcesFolder.exists() || dbResourcesFolder.members().length == 0) {
                    if (!dbResourcesFolder.exists()) {
                        dbResourcesFolder.create(true, true, null);
                    }
                    IFile datasourcesFile = project.getFile(GPDProject.DATASOURCE_FILE_NAME);
                    datasourcesFile.refreshLocal(IResource.DEPTH_ONE, null);
                    if (datasourcesFile.exists()) {
                        DataSource dataSource = getDataSource(datasourcesFile);
                        database = getDatabaseObjects(dataSource);
                        DatabaseIO databaseIO = new DatabaseIO();
                        IFolder datasourceFolder = dbResourcesFolder.getFolder(dataSource.getJndiName());
                        datasourceFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
                        if (!datasourceFolder.exists()) {
                            datasourceFolder.create(true, true, null);
                        }
                        String dbObjectName = database.getName();
                        dbObjectName = new String(new Base32().encode(dbObjectName.getBytes(UTF_8)), UTF_8);
                        String fileName = dbObjectName + "." + XML_EXTENSION;
                        IFile file = datasourceFolder.getFile(fileName);
                        file.refreshLocal(IResource.DEPTH_ONE, null);
                        if (file.exists()) {
                            file.delete(true, null);
                        }
                        file.refreshLocal(IResource.DEPTH_ONE, null);
                        String fileLocation = file.getLocation().toOSString();
                        file.create(new ByteArrayInputStream(new byte[0]), IResource.NONE, null);
                        file.refreshLocal(IResource.DEPTH_ONE, null);
                        databaseIO.write(database, fileLocation);
                        file.refreshLocal(IResource.DEPTH_ONE, null);
                        monitor.worked(2);
                    }
                } else {
                    for (IResource resource : dbResourcesFolder.members()) {
                        if (resource instanceof IFolder) {
                            IFolder dataFolder = (IFolder) resource;
                            dataFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
                            for (IResource dataFile : dataFolder.members()) {
                                if (dataFile instanceof IFile) {
                                    IFile file = (IFile) dataFile;
                                    file.refreshLocal(IResource.DEPTH_ONE, null);
                                    if (XML_EXTENSION.equals(file.getFileExtension())) {
                                        String fileName = file.getName();
                                        String fileLocation = file.getLocation().toOSString();
                                        String name = fileName.substring(0, fileName.indexOf("."));
                                        name = new String(new Base32().decode(name), UTF_8);
                                        DatabaseIO databaseIO = new DatabaseIO();
                                        fileLocation = "file:///" + fileLocation;
                                        database = databaseIO.read(new InputStreamReader(file.getContents(), UTF_8));
                                        monitor.worked(2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            monitor.done();
        }
        if (database != null)
            return database.getSchemas();
        return Collections.emptyList();
    }

    private static DataSource getDataSource(IFile datasourceFile) throws CoreException {
        Document document = XmlUtil.parseWithXSDValidation(datasourceFile.getContents(), "db_conf_file.xsd");
        Element element = (Element) document.selectSingleNode("//datasource");

        DataSource dataSource = new DataSource();
        dataSource.setJndiName(element.attributeValue(JNDI_NAME));
        dataSource.setDriverClassName(element.attributeValue(DRIVER_CLASS));
        dataSource.setUrl(element.attributeValue(CONNECTION_URL));
        dataSource.setUsername(element.attributeValue(USER_NAME));
        dataSource.setPassword(element.attributeValue(USER_PASSWORD));

        return dataSource;
    }

    private static class DataSource extends DriverManagerDataSource {
        private String jndiName;

        public String getJndiName() {
            return jndiName;
        }

        public void setJndiName(String jndiName) {
            this.jndiName = jndiName;
        }
    }
}
