package ru.runa.wfe.office.doc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.xmlbeans.XmlCursor;

import ru.runa.wfe.commons.SafeIndefiniteLoop;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.office.OfficeProperties;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

// TODO use scripting name for variables!
public class DocxUtils {
    private static final String LINE_DELIMITER = "\n";
    public static final String PLACEHOLDER_START = OfficeProperties.getDocxPlaceholderStart();
    public static final String PLACEHOLDER_END = OfficeProperties.getDocxPlaceholderEnd();
    public static final String ELEMENT_START = OfficeProperties.getDocxElementStart();
    public static final String ELEMENT_END = OfficeProperties.getDocxElementEnd();
    public static final String CLOSING_PLACEHOLDER_START = PLACEHOLDER_START + "/";

    public static int getPictureType(DocxConfig config, String fileName) {
        if (fileName.endsWith(".emf")) {
            return XWPFDocument.PICTURE_TYPE_EMF;
        } else if (fileName.endsWith(".wmf")) {
            return XWPFDocument.PICTURE_TYPE_WMF;
        } else if (fileName.endsWith(".pict")) {
            return XWPFDocument.PICTURE_TYPE_PICT;
        } else if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            return XWPFDocument.PICTURE_TYPE_JPEG;
        } else if (fileName.endsWith(".png")) {
            return XWPFDocument.PICTURE_TYPE_PNG;
        } else if (fileName.endsWith(".dib")) {
            return XWPFDocument.PICTURE_TYPE_DIB;
        } else if (fileName.endsWith(".gif")) {
            return XWPFDocument.PICTURE_TYPE_GIF;
        } else if (fileName.endsWith(".tiff")) {
            return XWPFDocument.PICTURE_TYPE_TIFF;
        } else if (fileName.endsWith(".eps")) {
            return XWPFDocument.PICTURE_TYPE_EPS;
        } else if (fileName.endsWith(".bmp")) {
            return XWPFDocument.PICTURE_TYPE_BMP;
        } else if (fileName.endsWith(".wpg")) {
            return XWPFDocument.PICTURE_TYPE_WPG;
        }
        config.reportProblem("Unsupported picture: " + fileName + ". Expected emf|wmf|pict|jpeg|png|dib|gif|tiff|eps|bmp|wpg");
        return -1;
    }

    public static void setCellText(final XWPFTableCell cell, String text) {
        new SafeIndefiniteLoop(100) {

            @Override
            protected void doOp() {
                cell.removeParagraph(0);
            }

            @Override
            protected boolean continueLoop() {
                return cell.getParagraphs().size() > 0;
            }
        }.doLoop();
        cell.setText(text != null ? text : "");
    }

    public static Object getValue(DocxConfig config, IVariableProvider variableProvider, Object value, String selector) {
        if (Strings.isNullOrEmpty(selector)) {
            return value;
        }
        StringTokenizer tokenizer = new StringTokenizer(selector, "\\.");
        while (tokenizer.hasMoreTokens()) {
            String variableName = tokenizer.nextToken();
            String keyName = null;
            int elementStartIndex = variableName.indexOf(ELEMENT_START);
            if (elementStartIndex > 0 && variableName.endsWith(ELEMENT_END)) {
                keyName = variableName.substring(elementStartIndex + ELEMENT_START.length(), variableName.length() - ELEMENT_END.length());
                variableName = variableName.substring(0, elementStartIndex);
            }
            if (value == null) {
                value = variableProvider.getValue(variableName);
            } else {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(variableName);
                } else {
                    try {
                        value = PropertyUtils.getProperty(value, variableName);
                    } catch (Exception e) {
                        config.reportProblem(e);
                    }
                }
            }
            if (value == null) {
                // TODO? config.reportProblem("returning null for " + selector +
                // " at stage " + variableName);
                return null;
            }
            if (keyName != null) {
                if (value instanceof Map) {
                    Object key = variableProvider.getValue(keyName);
                    if (key == null) {
                        key = keyName;
                        if (keyName.startsWith("\"") && keyName.endsWith("\"")) {
                            key = keyName.substring(1, keyName.length() - 1);
                        }
                    }
                    value = ((Map<?, ?>) value).get(key);
                } else if (value instanceof List) {
                    Integer index;
                    try {
                        index = Integer.parseInt(keyName);
                    } catch (Exception e) {
                        index = variableProvider.getValue(Integer.class, keyName);
                    }
                    if (index == null) {
                        config.reportProblem("Null index for " + keyName);
                    }
                    value = ((List<?>) value).get(index);
                } else {
                    config.reportProblem("Unable to get element '" + keyName + "' value from " + value);
                }
            }
        }
        return value;
    }

    public static <T extends AbstractIteratorOperation> T parseIterationOperation(DocxConfig config, IVariableProvider variableProvider,
            String string, T operation) {
        if (string.startsWith(CLOSING_PLACEHOLDER_START)) {
            return null;
        }
        if (string.startsWith(PLACEHOLDER_START) && string.endsWith(PLACEHOLDER_END)) {
            String placeholder = string.substring(PLACEHOLDER_START.length(), string.length() - PLACEHOLDER_END.length());
            StringTokenizer tokenizer = new StringTokenizer(placeholder, " ");
            String iteratorWithContainerVariable = tokenizer.nextToken();
            int colonIndex = iteratorWithContainerVariable.indexOf(":");
            if (colonIndex > 0) {
                operation.setIterateBy(IterateBy.identifyByString(config, iteratorWithContainerVariable));
                operation.setContainerVariableName(iteratorWithContainerVariable.substring(colonIndex + 1));
            } else {
                operation.setContainerVariableName(iteratorWithContainerVariable);
            }
            if (tokenizer.hasMoreElements()) {
                String lexem = tokenizer.nextToken();
                if (operation instanceof ColumnExpansionOperation) {
                    ((ColumnExpansionOperation) operation).setContainerSelector(lexem);
                }
                if (operation instanceof LoopOperation) {
                    ((LoopOperation) operation).setIteratorVariableName(lexem);
                }
            }
            if (tokenizer.hasMoreElements()) {
                config.reportProblem("Found invalid escape sequence '" + string + "'");
                return null;
            }
            WfVariable variable = variableProvider.getVariable(operation.getContainerVariableName());
            if (variable != null) {
                operation.setContainerVariable(variable);
            }
            if (!operation.isValid()) {
                // config.reportProblem("Invalid " + operation);
                return null;
            }
            return operation;
        }
        return null;
    }

    public static void replaceInParagraphs(DocxConfig config, MapDelegableVariableProvider variableProvider, List<XWPFParagraph> paragraphs) {
        Stack<Operation> operations = new Stack<Operation>();
        for (XWPFParagraph paragraph : Lists.newArrayList(paragraphs)) {
            String paragraphText = paragraph.getText();
            LoopOperation loopOperation = parseIterationOperation(config, variableProvider, paragraphText, new LoopOperation());
            if (loopOperation != null && loopOperation.isValid()) {
                loopOperation.setHeaderParagraph(paragraph);
                operations.push(loopOperation);
                continue;
            } else if (!operations.isEmpty()) {
                if (operations.peek() instanceof LoopOperation) {
                    if (operations.peek().isEndBlock(paragraphText)) {
                        XWPFDocument document = paragraph.getDocument();
                        int insertPosition = document.getParagraphPos(document.getPosOfParagraph(paragraph));
                        loopOperation = (LoopOperation) operations.pop();
                        Iterator<? extends Object> iterator = loopOperation.createIterator();
                        Object iteratorValue0 = null;
                        if (iterator.hasNext()) {
                            iteratorValue0 = iterator.next();
                        }
                        while (iterator.hasNext()) {
                            Object iteratorValue = iterator.next();
                            variableProvider.add(loopOperation.getIteratorVariable(iteratorValue));
                            for (XWPFParagraph templateParagraph : loopOperation.getBodyParagraphs()) {
                                XmlCursor cursor = document.getDocument().getBody().getPArray(insertPosition).newCursor();
                                XWPFParagraph newParagraph = document.insertNewParagraph(cursor);
                                insertPosition++;
                                for (XWPFRun templateRun : templateParagraph.getRuns()) {
                                    XWPFRun newRun = newParagraph.createRun();
                                    StylesHolder stylesHolder = new StylesHolder(templateRun);
                                    stylesHolder.applyStyles(newRun);// templateRun.getCTR().getRPr().getShd().getFill()
                                    String text = templateRun.getText(0);
                                    if (text != null) {
                                        newRun.setText(text);
                                    }
                                }
                                replaceInParagraph(config, variableProvider, newParagraph);
                            }
                        }
                        if (iteratorValue0 != null) {
                            variableProvider.add(loopOperation.getIteratorVariable(iteratorValue0));
                            for (XWPFParagraph templateParagraph : loopOperation.getBodyParagraphs()) {
                                replaceInParagraph(config, variableProvider, templateParagraph);
                            }
                        }
                        document.removeBodyElement(document.getPosOfParagraph(loopOperation.getHeaderParagraph()));
                        document.removeBodyElement(document.getPosOfParagraph(paragraph));
                        variableProvider.remove(loopOperation.getIteratorVariableName());
                        continue;
                    }
                    ((LoopOperation) operations.peek()).getBodyParagraphs().add(paragraph);
                } else if (operations.peek() instanceof IfOperation) {
                }
            } else {
                replaceInParagraph(config, variableProvider, paragraph);
            }
        }
        if (!operations.isEmpty()) {
            config.reportProblem("Found unconsistency for operations: not ended " + operations);
        }
    }

    public static void replaceInParagraph(DocxConfig config, IVariableProvider variableProvider, XWPFParagraph paragraph) {
        String paragraphText = paragraph.getParagraphText();
        if (!paragraphText.contains(PLACEHOLDER_START)) {
            return;
        }
        if (!paragraphText.contains(PLACEHOLDER_END)) {
            config.reportProblem("No placeholder end '" + PLACEHOLDER_END + "' found in " + paragraphText);
            return;
        }
        List<ReplaceOperation> operations = Lists.newArrayList();
        for (XWPFRun run : Lists.newArrayList(paragraph.getRuns())) {
            String text = run.getText(0);
            String replacedText = replaceText(config, variableProvider, operations, text);
            if (!Objects.equal(replacedText, text)) {
                if (replacedText.contains(LINE_DELIMITER)) {
                    StringTokenizer tokenizer = new StringTokenizer(replacedText, LINE_DELIMITER);
                    while (tokenizer.hasMoreTokens()) {
                        run.setText(tokenizer.nextToken(), 0);
                        if (tokenizer.hasMoreTokens()) {
                            run.addBreak();
                            run = paragraph.insertNewRun(paragraph.getRuns().indexOf(run) + 1);
                        }
                    }
                } else {
                    run.setText(replacedText, 0);
                }
            }
            for (ReplaceOperation replaceOperation : Lists.newArrayList(operations)) {
                if (replaceOperation instanceof InsertImageOperation) {
                    InsertImageOperation imageOperation = (InsertImageOperation) replaceOperation;
                    FileVariable fileVariable = imageOperation.getFileVariable();
                    try {
                        run.addPicture(new ByteArrayInputStream(fileVariable.getData()), imageOperation.getImageType(), fileVariable.getName(),
                                imageOperation.getWidth(), imageOperation.getHeight());
                    } catch (Exception e) {
                        config.reportProblem(e);
                    }
                    operations.remove(replaceOperation);
                }
            }
        }
    }

    private static String replaceText(DocxConfig config, IVariableProvider variableProvider, List<ReplaceOperation> operations, String text) {
        ReplaceOperation operation;
        if (operations.size() > 0 && !operations.get(operations.size() - 1).isPlaceholderRead()) {
            operation = operations.get(operations.size() - 1);
        } else {
            operation = new ReplaceOperation();
            operations.add(operation);
        }
        if (!operation.isStarted()) {
            // search start
            int placeholderStartIndex = text.indexOf(PLACEHOLDER_START);
            if (placeholderStartIndex >= 0) {
                String start = text.substring(0, placeholderStartIndex);
                operation.appendPlaceholder("");
                String remainder = text.substring(placeholderStartIndex + PLACEHOLDER_START.length());
                return start + replaceText(config, variableProvider, operations, remainder);
            }
            return text;
        } else {
            // search end
            int placeholderEndIndex = text.indexOf(PLACEHOLDER_END);
            if (placeholderEndIndex >= 0) {
                operation.appendPlaceholder(text.substring(0, placeholderEndIndex));
                operation.setEnded(true);
                String remainder = text.substring(placeholderEndIndex + PLACEHOLDER_END.length());
                Object value = getValue(config, variableProvider, null, operation.getPlaceholder());
                if (value == null) {
                    if (config.isStrictMode()) {
                        config.reportProblem("No template variable defined in process: '" + operation.getPlaceholder() + "'");
                    }
                }
                if (value instanceof FileVariable) {
                    try {
                        operations.remove(operation);
                        FileVariable fileVariable = (FileVariable) value;
                        InsertImageOperation imageOperation = new InsertImageOperation(operation.getPlaceholder(), fileVariable);
                        imageOperation.setValue("");
                        int imageType = getPictureType(config, fileVariable.getName());
                        if (imageType > 0) {
                            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileVariable.getData()));
                            // TODO does not work without
                            // org.apache.poi.ooxml-schemas 1.1
                            imageOperation.setImageType(imageType);
                            imageOperation.setWidth(Units.toEMU(image.getWidth()));
                            imageOperation.setHeight(Units.toEMU(image.getHeight()));
                            operations.add(imageOperation);
                            operation = imageOperation;
                        }
                    } catch (Exception e) {
                        config.reportProblem(e);
                    }
                } else {
                    VariableFormat valueFormat = null;
                    String placeholder = operation.getPlaceholder();
                    if (placeholder.contains(ELEMENT_START) && placeholder.endsWith(ELEMENT_END)) {
                        placeholder = placeholder.substring(0, placeholder.indexOf(ELEMENT_START));
                        WfVariable containerVariable = variableProvider.getVariable(placeholder);
                        if (containerVariable != null) {
                            int index = containerVariable.getValue() instanceof Map ? 1 : 0;
                            valueFormat = FormatCommons.createComponent((VariableFormatContainer) containerVariable.getFormatNotNull(), index);
                        }
                    } else {
                        WfVariable variable = variableProvider.getVariable(placeholder);
                        if (variable != null) {
                            valueFormat = variable.getFormatNotNull();
                        }
                    }
                    String replacement;
                    if (valueFormat != null) {
                        replacement = valueFormat.format(value);
                        if (replacement == null) {
                            replacement = "";
                        }
                    } else {
                        replacement = TypeConversionUtil.convertTo(String.class, value);
                    }
                    operation.setValue(replacement);
                }
                return operation.getValue() + replaceText(config, variableProvider, operations, remainder);
            } else {
                operation.appendPlaceholder(text);
                return "";
            }
        }
    }

    public static class StylesHolder {
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

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StylesHolder)) {
                return false;
            }
            StylesHolder h = (StylesHolder) obj;
            return bold == h.bold && Objects.equal(color, h.color) && Objects.equal(fontFamily, h.fontFamily) && italic == h.italic
                    && strike == h.strike && Objects.equal(subscript, h.subscript) && Objects.equal(underlinePatterns, h.underlinePatterns);
        }
    }

}
