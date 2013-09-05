package ru.runa.gpd.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class IOUtils {
    private static final ByteArrayInputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[0]);
    private static final List<String> formExtensions = new ArrayList<String>();
    static {
        for (FormType formType : FormTypeProvider.getRegisteredFormTypes()) {
            formExtensions.add(formType.getType());
        }
    }

    public static boolean looksLikeFormFile(String fileName) {
        String ext = getExtension(fileName);
        if (ext.length() == 0) {
            return true;
        }
        if (formExtensions.contains(ext)) {
            return true;
        }
        return fileName.endsWith(FormNode.VALIDATION_SUFFIX);
    }

    public static String getExtension(String fileName) {
        int lastPointIndex = fileName.lastIndexOf(".");
        if (lastPointIndex == -1) {
            // no extension
            return "";
        }
        return fileName.substring(lastPointIndex + 1);
    }

    public static void copyFileToDir(File sourceFile, File destDir) throws IOException {
        FileInputStream fis = new FileInputStream(sourceFile);
        File destFile = new File(destDir, sourceFile.getName());
        destFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(destFile);
        copyStream(fis, fos);
    }

    public static void copyFile(String source, IFile destinationFile) {
        try {
            copyFile(new FileInputStream(source), destinationFile);
        } catch (FileNotFoundException e) {
            PluginLogger.logErrorWithoutDialog("Unable to copy " + source + " to file " + destinationFile, e);
        }
    }

    public static void copyFile(InputStream source, IFile destinationFile) {
        try {
            if (destinationFile.exists()) {
                destinationFile.setContents(source, true, false, null);
            } else {
                destinationFile.create(source, true, null);
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Unable to copy to file " + destinationFile, e);
        } finally {
            Closeables.closeQuietly(source);
        }
    }
    
    public static void copyFile(InputStream source, File destinationFile) {
        try {
            byte[] from = ByteStreams.toByteArray(source);
            Files.write(from, destinationFile);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Unable to copy to file " + destinationFile, e);
        } finally {
            Closeables.closeQuietly(source);
        }
    }

    public static String readStream(InputStream in) throws IOException {
        return new String(readStreamAsBytes(in), Charsets.UTF_8);
    }

    public static byte[] readStreamAsBytes(InputStream in) throws IOException {
        try {
            return ByteStreams.toByteArray(in);
        } finally {
            Closeables.closeQuietly(in);
        }
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        try {
            ByteStreams.copy(in, out);
        } finally {
            Closeables.closeQuietly(in);
        }
    }

    public static IFile getAdjacentFile(IFile file, String fileName) {
        if (file == null) {
            return null;
        }
        return getAdjacentFile((IFolder) file.getParent(), fileName);
    }

    public static IFile getAdjacentFile(IFolder folder, String fileName) {
        IFile file = folder.getFile(fileName);
        try {
            file.refreshLocal(IResource.DEPTH_ONE, null);
        } catch (CoreException e) {
            PluginLogger.logErrorWithoutDialog("", e);
        }
        return file;
    }

    public static void createFolder(IFolder folder) throws CoreException {
        IContainer parent = folder.getParent();
        if (parent != null && !parent.exists() && parent instanceof IFolder) {
            createFolder((IFolder) parent);
        }
        if (!folder.exists()) {
            folder.create(true, true, null);
        }
    }

    public static IFile createFileSafely(IFile file) throws CoreException {
        return createFileSafely(file, EMPTY_STREAM);
    }

    public static IFile createFileSafely(IFile file, InputStream stream) throws CoreException {
        IFolder folder = (IFolder) file.getParent();
        String fileName = file.getName();
        if (file.exists()) {
            throw new CoreException(new Status(IStatus.WARNING, "ru.runa.gpd", 0, "File already exist", null));
        }
        try {
            file.create(stream, true, null);
        } catch (CoreException e) {
            // If error caused by many symbols in fileName - decreasing it
            if (fileName.length() < 10) {
                throw e;
            }
            int index = fileName.indexOf(" ");
            if (index <= 0) {
                index = 10;
            }
            String ext = getExtension(fileName);
            if (ext.length() > 30) {
                // omit extension
                ext = null;
            }
            fileName = fileName.substring(0, index);
            for (int i = 0; i < 100; i++) {
                String tryFileName = fileName + i;
                if (ext != null) {
                    tryFileName += "." + ext;
                }
                file = folder.getFile(tryFileName);
                if (!file.exists()) {
                    break;
                }
            }
            if (!file.exists()) {
                file.create(stream, true, null);
            }
        }
        file.setCharset(Charsets.UTF_8.name(), null);
        return file;
    }

    public static void createFile(IFile file) throws CoreException {
        createFile(file, EMPTY_STREAM);
    }

    public static void createFile(IFile file, InputStream stream) throws CoreException {
        if (file.exists()) {
            throw new CoreException(new Status(IStatus.WARNING, "ru.runa.gpd", 0, "File already exist", null));
        }
        file.create(stream, true, null);
        file.setCharset(Charsets.UTF_8.name(), null);
    }

    public static IFile moveFileSafely(IFile file, String fileName) throws CoreException {
        IFolder folder = (IFolder) file.getParent();
        IFile testFile = folder.getFile(fileName);
        try {
            file.move(testFile.getFullPath(), true, null);
            return testFile;
        } catch (CoreException e) {
            // If error caused by many symbols in fileName - decreasing it
            if (fileName.length() < 10) {
                throw e;
            }
            String ext = getExtension(fileName);
            if (ext.length() > 30) {
                // omit extension
                ext = "";
            }
            int index = fileName.indexOf(" ");
            if (index <= 0) {
                index = fileName.length() > 30 ? fileName.length() - ext.length() - 1 : 10;
            }
            fileName = fileName.substring(0, index);
            for (int i = 0; i < 100; i++) {
                String tryFileName = fileName + i;
                if (ext.length() != 0) {
                    tryFileName += "." + ext;
                }
                testFile = folder.getFile(tryFileName);
                if (!testFile.exists()) {
                    break;
                }
            }
            file.move(testFile.getFullPath(), true, null);
            return testFile;
        }
    }

    // unused now
    public static void renameFormFiles(FormNode formNode, String newName) throws CoreException {
        if (formNode.hasForm()) {
            IFile file = ProjectFinder.getFile(formNode.getFormFileName());
            String fileName = newName + "." + formNode.getFormType();
            IFile movedFile = moveFileSafely(file, fileName);
            formNode.setFormFileName(movedFile.getName());
        }
        if (formNode.hasFormValidation()) {
            IFile file = ProjectFinder.getFile(formNode.getValidationFileName());
            String fileName = newName + "." + FormNode.VALIDATION_SUFFIX;
            IFile movedFile = moveFileSafely(file, fileName);
            formNode.setValidationFileName(movedFile.getName());
        }
        if (formNode.hasFormScript()) {
            IFile file = ProjectFinder.getFile(formNode.getScriptFileName());
            String fileName = newName + "." + FormNode.SCRIPT_SUFFIX;
            IFile movedFile = moveFileSafely(file, fileName);
            formNode.setScriptFileName(movedFile.getName());
        }
    }

    public static void extractArchiveToFolder(InputStream archiveStream, IFolder folder) throws IOException, CoreException {
        ZipInputStream zis = new ZipInputStream(archiveStream);
        byte[] buf = new byte[1024];
        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            if (!entry.getName().contains("META-INF")) {
                IFile file = folder.getFile(entry.getName());
                ByteArrayOutputStream baos = new ByteArrayOutputStream(buf.length);
                int n;
                while ((n = zis.read(buf, 0, 1024)) > -1) {
                    baos.write(buf, 0, n);
                }
                createFile(file, new ByteArrayInputStream(baos.toByteArray()));
            }
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
    }

    public static void setUtfCharsetRecursively(IResource resource) throws CoreException {
        if (resource instanceof IProject && !((IProject) resource).isOpen()) {
            return;
        }
        if (resource instanceof IFile) {
            IFile file = (IFile) resource;
            if (!Charsets.UTF_8.name().equalsIgnoreCase(file.getCharset())) {
                file.setCharset(Charsets.UTF_8.name(), null);
            }
        }
        if (resource instanceof IContainer) {
            for (IResource member : ((IContainer) resource).members()) {
                setUtfCharsetRecursively(member);
            }
        }
    }

}
