package ru.runa.gpd.ui.custom;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.ProcessFileUtils;

public abstract class ProcessFileComposite extends Composite {
    // TODO integrate with complex variables commit
    private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    protected final IFile file;

    public ProcessFileComposite(Composite parent, IFile file) {
        super(parent, SWT.NONE);
        this.file = file;
        setLayout(new GridLayout(3, false));
        rebuild();
    }

    private void rebuild() {
        for (Control control : getChildren()) {
            control.dispose();
        }
        if (!file.exists()) {
            if (hasTemplate()) {
                SWTUtils.createLink(this, Localization.getString("button.create"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        IOUtils.copyFile(getTemplateInputStream(), file);
                        firePropertyChange(PropertyNames.VALUE, null, file.getName());
                        rebuild();
                    }
                });
            }
            SWTUtils.createLink(this, Localization.getString("button.import"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
                    if (getFileExtension() != null) {
                        dialog.setFilterExtensions(new String[] { "*." + getFileExtension() });
                    }
                    String path = dialog.open();
                    if (path == null) {
                        return;
                    }
                    IOUtils.copyFile(path, file);
                    firePropertyChange(PropertyNames.VALUE, null, file.getName());
                    rebuild();
                }
            });
        } else {
            SWTUtils.createLink(this, Localization.getString("button.change"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file, true);
                }
            });
            SWTUtils.createLink(this, Localization.getString("button.export"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                    dialog.setText(Localization.getString("button.export"));
                    if (getFileExtension() != null) {
                        dialog.setFilterExtensions(new String[] { "*." + getFileExtension() });
                    }
                    dialog.setFileName(file.getName());
                    String path = dialog.open();
                    if (path == null) {
                        return;
                    }
                    IOUtils.copyFile(file.getContents(), new File(path));
                }
            });
            if (isDeleteFileOperationSupported()) {
                SWTUtils.createLink(this, Localization.getString("button.delete"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        ProcessFileUtils.deleteProcessFile(file);
                        rebuild();
                        firePropertyChange(PropertyNames.VALUE, file.getName(), null);
                    }
                });
            }
        }
        layout(true, true);
    }

    protected abstract boolean hasTemplate();

    protected abstract InputStream getTemplateInputStream();

    protected abstract String getFileExtension();

    protected abstract boolean isDeleteFileOperationSupported();

    protected void firePropertyChange(String propName, Object old, Object newValue) {
        listeners.firePropertyChange(propName, old, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // duplicates
        removePropertyChangeListener(listener);
        listeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }
}
