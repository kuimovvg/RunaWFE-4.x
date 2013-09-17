package ru.runa.wfe.office.doc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.office.doc.DocxConfig.TableConfig;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormat;

public class DocxFileChanger {
    private static final String PLACEHOLDER1_START = "${";
    private static final String PLACEHOLDER1_END = "}";
    private static final String PLACEHOLDER2_START = "<";
    private static final String PLACEHOLDER2_END = ">";
    private final DocxConfig config;
    private final IVariableProvider variableProvider;
    private final XWPFDocument document;

    public DocxFileChanger(DocxConfig config, IVariableProvider variableProvider, IFileDataProvider fileDataProvider) throws IOException {
        this.config = config;
        this.variableProvider = variableProvider;
        document = new XWPFDocument(config.getFileInputStream(variableProvider, fileDataProvider, true));
    }

    public XWPFDocument changeAll() throws Exception {
        List<IBodyElement> bodyElements = new ArrayList<IBodyElement>(document.getBodyElements());
        for (IBodyElement bodyElement : bodyElements) {
            if (bodyElement instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                handleParagraph(paragraph);
            }
            if (bodyElement instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) bodyElement;
                List<XWPFTableRow> rows = table.getRows();
                for (XWPFTableRow row : rows) {
                    List<XWPFTableCell> cells = row.getTableCells();
                    for (XWPFTableCell cell : cells) {
                        List<XWPFParagraph> paragraphs = cell.getParagraphs();
                        for (XWPFParagraph paragraph : paragraphs) {
                            handleParagraph(paragraph);
                        }
                    }
                }
            }
        }
        return document;
    }

    private String getStyleIdbyName(String styleName) throws Exception {
        List<CTStyle> styles = document.getStyle().getStyleList();
        List<String> styleNames = new ArrayList<String>();
        for (CTStyle ctStyle : styles) {
            String name = ctStyle.getName().getVal();
            if (styleName.equals(name)) {
                return ctStyle.getStyleId();
            }
            styleNames.add(name);
        }
        throw new InternalApplicationException("Style '" + styleName + "' not found in template, all style names: " + styleNames);
    }

    private void handleParagraph(XWPFParagraph paragraph) throws Exception {
        String pText = paragraph.getParagraphText();
        if (!pText.contains(PLACEHOLDER1_START) && !pText.contains(PLACEHOLDER2_START)) {
            return;
        }
        if (pText.contains(PLACEHOLDER1_START) && !pText.contains(PLACEHOLDER1_END)) {
            throw new InternalApplicationException("No placeholder end for " + PLACEHOLDER1_START + " found in " + pText);
        }
        if (pText.contains(PLACEHOLDER2_START) && !pText.contains(PLACEHOLDER2_END)) {
            throw new InternalApplicationException("No placeholder end for " + PLACEHOLDER2_START + " found in " + pText);
        }
        List<XWPFRun> runs = paragraph.getRuns();
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String text = run.getText(0);
            String placeholderStart;
            String placeholderEnd;
            if (text.contains(PLACEHOLDER1_START)) {
                placeholderStart = PLACEHOLDER1_START;
                placeholderEnd = PLACEHOLDER1_END;
            } else if (text.contains(PLACEHOLDER2_START)) {
                placeholderStart = PLACEHOLDER2_START;
                placeholderEnd = PLACEHOLDER2_END;
            } else {
                continue;
            }
            StylesHolder stylesHolder = new StylesHolder(run);
            String placeholder = text.substring(text.indexOf(placeholderStart) + placeholderStart.length());
            while (!placeholder.contains(placeholderEnd)) {
                // search end in next run
                int nextIndex = i + placeholderEnd.length();
                if (runs.size() <= nextIndex) {
                    throw new InternalApplicationException("No placeholder end for " + placeholderStart + " can be found in " + pText);
                }
                run = runs.get(nextIndex);
                text = run.getText(0);
                paragraph.removeRun(nextIndex);
                placeholder += text;
            }
            int plEndIndex = placeholder.indexOf(placeholderEnd);
            String remainder = placeholder.substring(plEndIndex + placeholderEnd.length());
            placeholder = placeholder.substring(0, plEndIndex);

            TableConfig tableConfig = config.getTables().get(placeholder);
            if (tableConfig != null) {
                paragraph.removeRun(i);
                XWPFRun newRun = paragraph.insertNewRun(i);
                newRun.setText(remainder);

                int rows = 0;
                int columns = tableConfig.getColumns().size();
                List<List<?>> content = new ArrayList<List<?>>();
                for (String variableName : tableConfig.getColumns()) {
                    List<?> list = variableProvider.getValueNotNull(List.class, variableName);
                    if (list.size() > rows) {
                        rows = list.size();
                    }
                    content.add(list);
                }
                // tables
                XWPFTable table = document.createTable(rows, columns);
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        XWPFTableRow tableRow = table.getRow(r);
                        Object o = null;
                        if (content.get(c).size() > r) {
                            o = content.get(c).get(r);
                        }
                        String s = o != null ? o.toString() : "";
                        XWPFTableCell cell = tableRow.getCell(c);
                        cell.setText(s);
                    }
                }
                if (tableConfig.isAddBreak()) {
                    // Add a break between the tables
                    document.createParagraph().createRun().addBreak();
                }
                if (tableConfig.getStyleName() != null) {
                    table.setStyleID(getStyleIdbyName(tableConfig.getStyleName()));
                }
                // TODO document.insertTable(0, table);
            } else {
                WfVariable variable = variableProvider.getVariable(placeholder);
                if (variable == null || variable.getValue() == null) {
                    if (config.isStrictMode()) {
                        throw new InternalApplicationException("No template variable defined in process: '" + placeholder + "'");
                    }
                    continue;
                }
                VariableFormat format = config.getTypeHints().get(placeholder);
                if (format == null) {
                    format = variable.getFormatNotNull();
                }
                String replacement = format.format(variable.getValue());
                paragraph.removeRun(i);
                XWPFRun newRun = paragraph.insertNewRun(i);
                newRun.setText(replacement + remainder);
                stylesHolder.applyStyles(newRun);
            }
        }
    }

    private class StylesHolder {
        private final boolean bold;
        private final String color;
        private final String fontFamily;
        private final int fontSize;
        private final boolean italic;
        private final boolean strike;
        private final VerticalAlign subscript;
        private final UnderlinePatterns underlinePatterns;

        public StylesHolder(XWPFRun run) {
            bold = run.isBold();
            color = run.getColor();
            fontFamily = run.getFontFamily();
            fontSize = run.getFontSize();
            italic = run.isItalic();
            strike = run.isStrike();
            subscript = run.getSubscript();
            underlinePatterns = run.getUnderline();
        }

        public void applyStyles(XWPFRun run) {
            // absence of checks caused logical errors in result document
            run.setBold(bold);
            if (color != null) {
                run.setColor(color);
            }
            if (fontFamily != null) {
                run.setFontFamily(fontFamily);
            }
            if (fontSize > 0) {
                run.setFontSize(fontSize);
            }
            run.setItalic(italic);
            run.setStrike(strike);
            if (subscript != null) {
                run.setSubscript(subscript);
            }
            if (underlinePatterns != null) {
                run.setUnderline(underlinePatterns);
            }
        }
    }
}
