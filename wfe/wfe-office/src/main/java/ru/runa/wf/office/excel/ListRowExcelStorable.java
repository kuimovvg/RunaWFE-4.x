package ru.runa.wf.office.excel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import ru.runa.wf.office.excel.utils.ExcelHelper;

public class ListRowExcelStorable extends ExcelStorable<RowConstraints, List<?>> {

    @Override
    public void load(Workbook workbook) {
        List<Object> list = new ArrayList<Object>();
        Row row = getRow(workbook);
        int columnIndex = constraints.getColumnStartIndex();
        while (true) {
            Cell cell = ExcelHelper.getCell(row, columnIndex, false);
            if (ExcelHelper.isCellEmptyOrNull(cell)) {
                break;
            }
            list.add(ExcelHelper.getCellValue(cell));
            columnIndex++;
        }
        setData(list);
    }

    @Override
    protected void storeIn(Workbook workbook) {
        Row row = getRow(workbook);
        List<?> list = data;
        int columnIndex = constraints.getColumnStartIndex();
        for (Object object : list) {
            Cell cell = ExcelHelper.getCell(row, columnIndex, true);
            ExcelHelper.setCellValue(cell, object);
            columnIndex++;
        }
    }

    private Row getRow(Workbook workbook) {
        Sheet sheet = ExcelHelper.getSheet(workbook, constraints.getSheetName(), constraints.getSheetIndex());
        Row row = ExcelHelper.getRow(sheet, constraints.getRowIndex(), true);
        return row;
    }

}
