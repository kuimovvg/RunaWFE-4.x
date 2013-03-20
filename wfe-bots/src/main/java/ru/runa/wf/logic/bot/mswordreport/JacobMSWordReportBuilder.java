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
        } catch (Exception e) {
            log.error("", e);
            if (wordApplication != null && wordDocument == null) {
                throw new MSWordReportException(MSWordReportException.OPEN_TEMPLATE_DOCUMENT_FAILED, settings.getTemplateFilePath());
            }
            throw new MSWordReportException(MSWordReportException.MSWORD_APP_COMM_ERROR);
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
        Dispatch bookmarks = Dispatch.get(wordDocument, "Bookmarks").toDispatch();
        for (BookmarkVariableMapping mapping : settings.getMappings()) {
            try {
                Dispatch bookmark = Dispatch.call(bookmarks, "Item", mapping.getBookmarkName()).toDispatch();
                String value = getVariableValue(mapping);
                Dispatch range = Dispatch.get(bookmark, "Range").toDispatch();
                Dispatch.put(range, "Text", value);
            } catch (MSWordReportException e) {
                if (settings.isStrictMode()) {
                    throw e;
                }
                log.warn("Seems like variable is missed: " + e.getLocalizedMessage(), e);
            } catch (Exception e) {
                if (mapping.isOptional() || !settings.isStrictMode()) {
                    log.warn("No bookmark found in template document by name '" + mapping.getBookmarkName() + "'");
                } else {
                    log.error("", e);
                    throw new MSWordReportException(MSWordReportException.BOOKMARK_NOT_FOUND_IN_TEMPLATE, mapping.getBookmarkName());
                }
            }
        }
        int bookmarksCount = Dispatch.get(bookmarks, "Count").toInt();
        for (int i = 0; i < bookmarksCount; i++) {
            Dispatch bookmark = Dispatch.call(bookmarks, "Item", new Integer(i + 1)).toDispatch();
            String bookmarkName = Dispatch.get(bookmark, "Name").getString();
            log.warn("Bookmark exists in result document: '" + bookmarkName + "'");
        }
    }
}
