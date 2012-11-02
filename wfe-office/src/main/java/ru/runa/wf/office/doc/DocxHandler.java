package ru.runa.wf.office.doc;

import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import ru.runa.wf.office.shared.FilesSupplierConfigParser;
import ru.runa.wf.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Maps;

public class DocxHandler extends OfficeFilesSupplierHandler<DocxConfig> {

    @Override
    protected FilesSupplierConfigParser<DocxConfig> createParser() {
        return new DocxConfigParser();
    }

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        Map<String, Object> result = Maps.newHashMap();
        DocxFileChanger fileChanger = new DocxFileChanger(config, variableProvider);
        XWPFDocument document = fileChanger.changeAll();
        document.write(config.getFileOutputStream(result, true));
        return result;
    }

}
