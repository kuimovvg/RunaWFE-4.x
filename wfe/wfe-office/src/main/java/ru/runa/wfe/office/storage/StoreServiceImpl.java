package ru.runa.wfe.office.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ru.runa.wfe.office.excel.CellConstraints;
import ru.runa.wfe.office.excel.ColumnConstraints;
import ru.runa.wfe.office.excel.IExcelConstraints;
import ru.runa.wfe.office.excel.RowConstraints;
import ru.runa.wfe.office.excel.utils.ExcelHelper;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class StoreServiceImpl implements StoreService {

    private IExcelConstraints constraints;
    private VariableFormat format;
    private String fileName;
    private String path;
    private String fullPath;

    @Override
    public void createFile(Properties properties) throws Exception {
        initParams(properties);
        if (new File(fullPath).exists()) {
            return;
        }
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
        Workbook workbook = null;
        if (fileName.endsWith(".xls")) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new XSSFWorkbook();
        }
        workbook.createSheet();
        FileOutputStream outputStream = new FileOutputStream(fullPath);
        workbook.write(outputStream);
        outputStream.close();
    }

    @Override
    public ExecutionResult findAll(Properties properties) throws Exception {
        initParams(properties);
        Workbook wb = getWorkbook(fullPath);
        return new ExecutionResult(find(wb, constraints, format, null));
    }

    @Override
    public ExecutionResult findByFilter(Properties properties, List<ConditionItem> conditions) throws Exception {
        initParams(properties);
        Workbook wb = getWorkbook(fullPath);
        return new ExecutionResult(find(wb, constraints, format, conditions));
    }

    @Override
    public ExecutionResult update(Properties properties, List<? extends ConditionItem> conditions) throws Exception {
        initParams(properties);
        Workbook wb = getWorkbook(fullPath);
        update(wb, constraints, format, conditions);
        OutputStream os = new FileOutputStream(fullPath);
        wb.write(os);
        os.close();
        return findAll(properties);
    }

    @Override
    public ExecutionResult delete(Properties properties, List<ConditionItem> conditions) throws Exception {
        initParams(properties);
        Workbook wb = getWorkbook(fullPath);
        delete(wb, constraints, format, conditions);
        OutputStream os = new FileOutputStream(fullPath);
        wb.write(os);
        os.close();
        return findAll(properties);
    }

    @Override
    public ExecutionResult save(Properties properties, List<?> records, boolean appendTo) throws Exception {
        initParams(properties);
        Workbook wb = getWorkbook(fullPath);
        save(wb, constraints, format, records, appendTo);
        OutputStream os = new FileOutputStream(fullPath);
        wb.write(os);
        os.close();
        return findAll(properties);
    }

    private void initParams(Properties properties) {
        Preconditions.checkNotNull(properties);
        constraints = (IExcelConstraints) properties.get(PROP_CONSTRAINTS);
        format = (VariableFormat) properties.get(PROP_FORMAT);
        fullPath = properties.getProperty(PROP_PATH);
        File f = new File(fullPath);
        fileName = f.getName();
        path = f.getParent();
        FileHelper.checkPath(path);
    }

    private void delete(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List<ConditionItem> conditions) {
        update(workbook, constraints, variableFormat, conditions, true);
    }

    private void update(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List<? extends ConditionItem> conditionItems) {
        update(workbook, constraints, variableFormat, conditionItems, false);
    }

    private void update(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat,
            List<? extends ConditionItem> conditionItems, boolean clear) {
        List list = find(workbook, constraints, variableFormat, null);
        boolean changed = false;
        for (ConditionItem item : conditionItems) {
            int i = 0;
            for (Object object : list) {
                if (ConditionProcessor.filter(object, item.getOperator(), item.getValue())) {
                    if (!clear) {
                        list.set(i, ((UpdateConditionItem) item).getNewValue());
                    } else {
                        list.set(i, null);
                    }
                    changed = true;
                }
                i++;
            }
        }
        if (changed) {
            save(workbook, constraints, variableFormat, list, false);
        }
    }

    @SuppressWarnings("rawtypes")
    private void save(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List records, boolean append) {
        VariableFormat format = getVariableFormat(variableFormat);
        if (constraints instanceof CellConstraints) {
            fillResultToCell(workbook, constraints, format, records);
        } else if (constraints instanceof RowConstraints) {
            fillResultToRow(workbook, constraints, format, records, append);
        } else {
            fillResultToColumn(workbook, constraints, format, records, append);
        }
    }

    @SuppressWarnings("resource")
    private Workbook getWorkbook(String fullPath) throws IOException, FileNotFoundException {
        Workbook wb = null;
        InputStream is = new FileInputStream(fullPath);
        if (fullPath.endsWith(".xls")) {
            wb = new HSSFWorkbook(is);
        } else if (fullPath.endsWith(".xlsx")) {
            wb = new XSSFWorkbook(is);
        } else {
            throw new IllegalArgumentException("excel file extension is incorrect!");
        }
        is.close();
        return wb;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List find(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List<ConditionItem> conditions) {
        boolean all = conditions == null || conditions.size() == 0;
        List result = findAll(workbook, constraints, variableFormat);
        if (!all) {
            List filtered = Lists.newArrayList();
            for (ConditionItem conditionItem : conditions) {
                for (Object object : result) {
                    if (ConditionProcessor.filter(object, conditionItem.getOperator(), conditionItem.getValue())) {
                        filtered.add(object);
                    }
                }
            }
            return filtered;
        }
        return result;
    }

    private List<?> findAll(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat) {
        List<?> result = Lists.newArrayList();
        VariableFormat format = getVariableFormat(variableFormat);
        if (constraints instanceof CellConstraints) {
            fillResultFromCell(workbook, constraints, format, result);
        } else if (constraints instanceof RowConstraints) {
            fillResultFromRow(workbook, constraints, format, result);
        } else {
            fillResultFromColumn(workbook, constraints, format, result);
        }
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void fillResultFromColumn(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List result) {
        ColumnConstraints columnConstraints = (ColumnConstraints) constraints;
        int columnIndex = columnConstraints.getColumnIndex();
        int rowIndex = columnConstraints.getRowStartIndex();
        Sheet sheet = ExcelHelper.getSheet(workbook, columnConstraints.getSheetName(), columnConstraints.getSheetIndex());
        VariableFormat format = getVariableFormat(variableFormat);
        while (true) {
            Row row = ExcelHelper.getRow(sheet, rowIndex, true);
            Cell cell = ExcelHelper.getCell(row, columnIndex, true);
            if (ExcelHelper.isCellEmptyOrNull(cell)) {
                break;
            }
            result.add(ExcelHelper.getCellValue(cell, format));
            rowIndex++;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void fillResultFromRow(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List result) {
        RowConstraints rowConstraints = (RowConstraints) constraints;
        int rowIndex = rowConstraints.getRowIndex();
        int columnIndex = rowConstraints.getColumnStartIndex();
        Sheet sheet = ExcelHelper.getSheet(workbook, rowConstraints.getSheetName(), rowConstraints.getSheetIndex());
        VariableFormat format = getVariableFormat(variableFormat);
        while (true) {
            Row row = ExcelHelper.getRow(sheet, rowIndex, true);
            Cell cell = ExcelHelper.getCell(row, columnIndex, true);
            if (ExcelHelper.isCellEmptyOrNull(cell)) {
                break;
            }

            result.add(ExcelHelper.getCellValue(cell, format));
            columnIndex++;
        }
    }

    private void fillResultToRow(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List<?> result, boolean append) {
        RowConstraints rowConstraints = (RowConstraints) constraints;
        int rowIndex = rowConstraints.getRowIndex();
        int columnIndex = rowConstraints.getColumnStartIndex();
        Sheet sheet = ExcelHelper.getSheet(workbook, rowConstraints.getSheetName(), rowConstraints.getSheetIndex());

        if (append) {
            columnIndex = getLastColumnIndex(sheet, columnIndex, rowIndex);
            columnIndex++;
        }

        VariableFormat format = getVariableFormat(variableFormat);
        for (Object object : result) {
            Row row = ExcelHelper.getRow(sheet, rowIndex, true);
            Cell cell = ExcelHelper.getCell(row, columnIndex, true);
            if (object == null) {
                cell.setCellValue((String) null);
            } else {
                ExcelHelper.setCellValue(cell, format.format(object));
            }
            columnIndex++;
        }
    }

    private void fillResultToColumn(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List<?> result, boolean append) {
        ColumnConstraints columnConstraints = (ColumnConstraints) constraints;
        int rowIndex = columnConstraints.getRowStartIndex();
        int columnIndex = columnConstraints.getColumnIndex();
        Sheet sheet = ExcelHelper.getSheet(workbook, columnConstraints.getSheetName(), columnConstraints.getSheetIndex());

        if (append) {
            rowIndex = getLastRowIndex(sheet, columnIndex, rowIndex);
            rowIndex++;
        }
        for (Object object : result) {
            Row row = ExcelHelper.getRow(sheet, rowIndex, true);
            Cell cell = ExcelHelper.getCell(row, columnIndex, true);
            if (object == null) {
                cell.setCellValue((String) null);
            } else {
                ExcelHelper.setCellValue(cell, variableFormat.format(object));
            }
            rowIndex++;
        }
    }

    private int getLastColumnIndex(Sheet sheet, int startColumnIndex, int rowIndex) {
        while (true) {
            Row row = ExcelHelper.getRow(sheet, rowIndex, true);
            Cell cell = ExcelHelper.getCell(row, startColumnIndex, true);
            if (ExcelHelper.isCellEmptyOrNull(cell)) {
                break;
            }
            startColumnIndex++;
        }
        return startColumnIndex;
    }

    private int getLastRowIndex(Sheet sheet, int startRowIndex, int columnIndex) {
        while (true) {
            Row row = ExcelHelper.getRow(sheet, startRowIndex, true);
            Cell cell = ExcelHelper.getCell(row, columnIndex, true);
            if (ExcelHelper.isCellEmptyOrNull(cell)) {
                break;
            }
            startRowIndex++;
        }
        return startRowIndex;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void fillResultFromCell(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, List result) {
        CellConstraints cellConstraints = (CellConstraints) constraints;
        int columnIndex = cellConstraints.getColumnIndex();
        int rowIndex = cellConstraints.getRowIndex();
        Sheet sheet = ExcelHelper.getSheet(workbook, cellConstraints.getSheetName(), cellConstraints.getSheetIndex());
        Row row = ExcelHelper.getRow(sheet, rowIndex, true);
        Cell cell = ExcelHelper.getCell(row, columnIndex, true);
        result.add(ExcelHelper.getCellValue(cell, variableFormat));
    }

    private void fillResultToCell(Workbook workbook, IExcelConstraints constraints, VariableFormat variableFormat, Object result) {
        CellConstraints cellConstraints = (CellConstraints) constraints;
        int columnIndex = cellConstraints.getColumnIndex();
        int rowIndex = cellConstraints.getRowIndex();
        Sheet sheet = ExcelHelper.getSheet(workbook, cellConstraints.getSheetName(), cellConstraints.getSheetIndex());
        Row row = ExcelHelper.getRow(sheet, rowIndex, true);
        Cell cell = ExcelHelper.getCell(row, columnIndex, true);
        if (result == null) {
            cell.setCellValue((String) null);
        } else {
            ExcelHelper.setCellValue(cell, variableFormat.format(result));
        }
    }

    private VariableFormat getVariableFormat(VariableFormat variableFormat) {
        VariableFormat format = null;
        if (variableFormat instanceof ListFormat) {
            format = FormatCommons.createComponent((VariableFormatContainer) variableFormat, 0);
        } else {
            format = variableFormat;
        }
        return format;
    }
}
