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

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.IVariableProvider;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * 
 * Created on 23.11.2006
 * 
 */
public class JacobMSWordReportBuilder extends MSWordReportBuilder {

    public JacobMSWordReportBuilder(MSWordReportTaskSettings settings, IVariableProvider variableProvider) {
        super(settings, variableProvider);
    }

    @Override
    public void build(String reportTemporaryFileName) {
        ActiveXComponent wordApplication = null;
        Dispatch wordDocument = null;
        try {
            wordApplication = new ActiveXComponent("Word.Application");
            wordApplication.setProperty("DisplayAlerts", new Variant(false));
            wordApplication.setProperty("Visible", new Variant(false));
            Dispatch wordDocuments = wordApplication.getProperty("Documents").toDispatch();
            wordDocument = Dispatch.call(wordDocuments, "Open", settings.getTemplateFilePath()).toDispatch();
            replaceBookmarksWithValues(wordDocument, variableProvider, settings);
            Dispatch.call(wordDocument, "SaveAs", reportTemporaryFileName);
        } catch (RuntimeException e) {
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
            if (wordDocument != null) {
                try {
                    Dispatch.call(wordDocument, "Close", new Variant(Boolean.FALSE));
                } finally {
                    if (wordApplication != null) {
                        wordApplication.invoke("Quit", new Variant[] {});
                        wordApplication.release();
                    }
                }
            }
        }

    }

    private void replaceBookmarksWithValues(Dispatch wordDocument, IVariableProvider variableProvider, MSWordReportTaskSettings settings) {
        // Dispatch bookmarks = Dispatch.get(wordDocument,
        // "Bookmarks").toDispatch();
        // int bookmarksCount = Dispatch.get(bookmarks, "Count").toInt();
        // for (int i = 1; i <= bookmarksCount; i++) {
        // Dispatch bookmark = Dispatch.call(bookmarks, "Item", new
        // Integer(1)).toDispatch();
        // String bookmarkName = Dispatch.get(bookmark, "Name").getString();
        // String value = settings.format(bookmarkName, variableProvider);
        // Dispatch range = Dispatch.get(bookmark, "Range").toDispatch();
        // Dispatch.put(range, "Text", value);
        // }
        Dispatch bookmarks = Dispatch.get(wordDocument, "Bookmarks").toDispatch();
        int bookmarksCount = Dispatch.get(bookmarks, "Count").toInt();
        for (int i = 1; i <= bookmarksCount; i++) {
            Dispatch bookmark = Dispatch.call(bookmarks, "Item", new Integer(1)).toDispatch();
            String bookmarkName = Dispatch.get(bookmark, "Name").getString();
            String value = settings.format(bookmarkName, variableProvider);
            Dispatch range = Dispatch.get(bookmark, "Range").toDispatch();
            Dispatch.put(range, "Text", value);
        }
    }
}
