package ru.runa.gpd.ui.custom;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

import ru.runa.gpd.PluginLogger;

public abstract class LoggingDoubleClickListener implements IDoubleClickListener {
    @Override
    public final void doubleClick(DoubleClickEvent event) {
        try {
            onDoubleClick(event);
        } catch (Throwable th) {
            PluginLogger.logError(th);
        }
    }

    public abstract void onDoubleClick(DoubleClickEvent event);
}
