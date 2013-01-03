package ru.runa.gpd.bot;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;

import ru.runa.gpd.BotStationNature;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.ProjectFinder;

import com.google.common.io.ByteStreams;

public class BotStationImportCommand extends BotImportCommand {
    public BotStationImportCommand(InputStream inputStream) {
        super(inputStream, null, null);
    }

    public BotStationImportCommand(InputStream inputStream, String botName, String botStationName) {
        super(inputStream, botName, botStationName);
    }

    @Override
    protected void execute(IProgressMonitor progressMonitor) throws InvocationTargetException {
        try {
            ZipInputStream zin = new ZipInputStream(inputStream);
            ZipEntry entry;
            String botStationName = "";
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals("botstation")) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(zin, PluginConstants.UTF_ENCODING));
                    String name = r.readLine();
                    botStationName = name;
                    String addr = r.readLine();
                    for (IProject botStationProject : ProjectFinder.getAllBotStations()) {
                        if (botStationProject.getName().equals(name)) {
                            throw new Exception(Localization.getString("ImportParWizardPage.error.processWithSameNameExists"));
                        }
                    }
                    IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
                    final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(name);
                    description.setNatureIds(new String[] { BotStationNature.NATURE_ID });
                    newProject.create(description, null);
                    newProject.open(IResource.BACKGROUND_REFRESH, null);
                    newProject.refreshLocal(IResource.DEPTH_INFINITE, null);
                    IFolder folder = newProject.getFolder("/src/botstation/");
                    IOUtils.createFolder(folder);
                    IFile gpdFile = folder.getFile("botstation");
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(botStationName);
                    buffer.append("\n");
                    buffer.append(addr);
                    buffer.append("\n");
                    gpdFile.create(new ByteArrayInputStream(buffer.toString().getBytes(PluginConstants.UTF_ENCODING)), true, null);
                    continue;
                }
                //deploy bot
                this.botName = entry.getName();
                this.botStationName = botStationName;
                this.inputStream = new ByteArrayInputStream(ByteStreams.toByteArray(zin));
                super.execute(progressMonitor);
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
}
