package ru.runa.wfe.office.doc;

import java.io.InputStream;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Maps;

public class DocxHandler extends OfficeFilesSupplierHandler<DocxConfig> {

    @Override
    protected FilesSupplierConfigParser<DocxConfig> createParser() {
        return new DocxConfigParser();
    }

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider, IFileDataProvider fileDataProvider) throws Exception {
        Map<String, Object> result = Maps.newHashMap();
        InputStream templateInputStream = config.getFileInputStream(variableProvider, fileDataProvider, true);
        if (config.getTables().size() > 0) {
            log.warn("Using deprecated pre 4.0.6 changer for table configs");
            DocxFileChangerPre406 fileChanger = new DocxFileChangerPre406(config, variableProvider, templateInputStream);
            XWPFDocument document = fileChanger.changeAll();
            document.write(config.getFileOutputStream(result, true));
        } else {
            DocxFileChanger fileChanger = new DocxFileChanger(config, variableProvider, templateInputStream);
            XWPFDocument document = fileChanger.changeAll();
            document.write(config.getFileOutputStream(result, true));
        }
        return result;
    }

}
