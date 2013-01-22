/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.logic.bot.mswordreport;

import jp.ne.so_net.ga2.no_ji.jcom.IDispatch;
import jp.ne.so_net.ga2.no_ji.jcom.JComException;
import jp.ne.so_net.ga2.no_ji.jcom.ReleaseManager;

import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Created on 23.11.2006
 * 
 */
public class JComMSWordReportBuilder implements MSWordReportBuilder {
    @Override
    public void build(String reportTemporaryFileName, IVariableProvider variableProvider, MSWordReportTaskSettings settings) {
        IDispatch wordApplication = null;
        IDispatch wordDocument = null;
        try {
            wordApplication = new IDispatch(new ReleaseManager(), "Word.Application");
            wordApplication.put("DisplayAlerts", Boolean.FALSE);
            wordApplication.put("Visible", Boolean.FALSE);
            wordDocument = (IDispatch) ((IDispatch) wordApplication.get("Documents")).method("Open", new String[] { settings.getTemplateFilePath() });
            replaceBookmarksWithValues(wordDocument, variableProvider, settings);
            wordDocument.method("SaveAs", new Object[] { reportTemporaryFileName });
        } catch (JComException e) {
            String errorMessage = e.getMessage();
            if (wordApplication != null) {
                if (wordDocument == null) {
                    errorMessage = "Could not open template document " + settings.getTemplateFilePath();
                }
            } else {
                errorMessage = "Could not instantiate MSWord application.";
            }
            throw new InternalApplicationException(errorMessage, e);
        } finally {
            try {
                if (wordDocument != null) {
                    try {
                        wordDocument.method("Close", new Object[] { Boolean.FALSE });
                    } finally {
                        if (wordApplication != null) {
                            wordApplication.method("Quit", null);
                            wordApplication.getReleaseManager().release();
                        }
                    }
                }
            } catch (JComException e) {
                throw new InternalApplicationException(e);
            }
        }
    }

    private void replaceBookmarksWithValues(IDispatch wordDocument, IVariableProvider variableProvider, MSWordReportTaskSettings settings)
            throws JComException {
        IDispatch bookmarks = (IDispatch) wordDocument.get("Bookmarks", null);
        int bookmarksCount = 1;
        int bookmarksIterations = 0;
        while (bookmarksCount > 0 && bookmarksIterations++ < 100) {
            IDispatch bookmark = (IDispatch) bookmarks.method("Item", new Integer[] { 1 });
            String bookmarkName = (String) bookmark.get("Name");
            String value = settings.format(bookmarkName, variableProvider);
            ((IDispatch) bookmark.get("Range")).put("Text", value);
            bookmarksCount = (Integer) bookmarks.get("Count", null);
        }
        for (int i = 0; i < bookmarksCount; i++) {
            IDispatch bookmark = (IDispatch) bookmarks.method("Item", new Integer[] { i + 1 });
            String bookmarkName = (String) bookmark.get("Name");
            LogFactory.getLog(getClass()).warn("Bookmark exists in result doc: " + bookmarkName);
        }
    }
}
