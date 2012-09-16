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
package ru.runa.af.web.action;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.cactus.ServletTestCase;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.wf.service.WebWfServiceTestHelper;

/**
 */
public abstract class StrutsTestCase extends ServletTestCase {

    protected WebWfServiceTestHelper testHelper;

    protected ActionMapping getActionMapping(Map forwards) {
        ActionMapping mapping = new ActionMapping();
        for (Iterator iter = forwards.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            String path = (String) entry.getValue();
            mapping.addForwardConfig(new ActionForward(name, path, false));
        }
        return mapping;
    }

    public abstract String getTestPrefix();

    protected void setUp() throws Exception {
        super.setUp();
        testHelper = new WebWfServiceTestHelper(getTestPrefix());
        testHelper.createDefaultExecutorsMap();
        SubjectHttpSessionHelper.addActorSubject(testHelper.getAuthorizedPerformerSubject(), session);
        ProfileHttpSessionHelper.setProfile(testHelper.getDefaultProfile(testHelper.getAuthorizedPerformerSubject()), session);
    }

    protected void tearDown() throws Exception {
        testHelper.releaseResources();
        testHelper = null;
        SubjectHttpSessionHelper.removeActorSubject(session);
        ProfileHttpSessionHelper.removeProfile(session);
        super.tearDown();
    }

    protected ActionMessages getGlobalErrors() {
        return (ActionMessages) session.getAttribute(Globals.ERROR_KEY);
    }

    protected void clearGlobalErrors() {
        session.removeAttribute(Globals.ERROR_KEY);
    }

    protected ActionMessages getGlobalMessages() {
        return (ActionMessages) session.getAttribute(Globals.MESSAGE_KEY);
    }

    protected FormFile getFile(String fileName) throws IOException {
        FormFile diskFile = new TestFormFile(fileName);
        return diskFile;
    }

    private class TestFormFile implements FormFile {
        private final String fileName;

        private final byte[] data;

        public TestFormFile(String fileName) throws IOException {
            this.fileName = fileName;
            data = WebWfServiceTestHelper.readBytesFromFile(fileName);
        }

        public String getContentType() {
            return "file";
        }

        public void setContentType(String contentType) {
        }

        public int getFileSize() {
            return data.length;
        }

        public void setFileSize(int fileSize) {
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
        }

        public byte[] getFileData() throws FileNotFoundException, IOException {
            return data.clone();
        }

        public InputStream getInputStream() throws FileNotFoundException, IOException {
            return new ByteArrayInputStream(data.clone());
        }

        public void destroy() {

        }

    }

}
