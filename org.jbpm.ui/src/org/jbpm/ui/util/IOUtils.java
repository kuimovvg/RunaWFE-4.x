package org.jbpm.ui.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.common.model.FormNode;
import org.jbpm.ui.forms.FormType;
import org.jbpm.ui.forms.FormTypeProvider;

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
        DesignerLogger.logInfo("copyFileToDir " + sourceFile.getAbsolutePath() + " -> " + destDir.getAbsolutePath());
        FileInputStream fis = new FileInputStream(sourceFile);
        File destFile = new File(destDir, sourceFile.getName());
        destFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(destFile);
        copyStream(fis, fos);
    }

    public static String readStream(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(in, baos);
        return new String(baos.toByteArray(), PluginConstants.UTF_ENCODING);
    }

    public static byte[] readStreamAsBytes(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(in, baos);
        return baos.toByteArray();
    }

    public static void writeToStream(OutputStream out, String str) throws IOException {
        out.write(str.getBytes(PluginConstants.UTF_ENCODING));
        out.flush();
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buf = new byte[1024 * 8];
            int length = 0;
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
        } finally {
            in.close();
            out.close();
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
            DesignerLogger.logErrorWithoutDialog("", e);
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

    public static String fixFileName(String fileName) {
        return fileName.replaceAll(Pattern.quote("?"), Matcher.quoteReplacement(""));
    }

    public static IFile createFileSafely(IFile file) throws CoreException {
        IFolder folder = (IFolder) file.getParent();
        String fileName = file.getName();
        if (file.exists()) {
            throw new CoreException(new Status(IStatus.WARNING, "org.jbpm.ui", 0, "File already exist", null));
        }
        try {
            file.create(EMPTY_STREAM, true, null);
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
                file.create(EMPTY_STREAM, true, null);
                file.setCharset(PluginConstants.UTF_ENCODING, null);
            }
        }
        return file;
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
                baos.close();
                InputStream entryStream = new ByteArrayInputStream(baos.toByteArray());
                file.create(entryStream, true, null);
                file.setCharset(PluginConstants.UTF_ENCODING, null);
            }
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
    }

}
