package ru.runa.wf.office.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class ExcelDataStore {
    private static Map<Class<? extends IExcelConstraints>, Class<? extends ExcelStorable<?, ?>>> mappings = Maps.newHashMap();
    static {
        mappings.put(CellConstraints.class, CellExcelStorable.class);
        mappings.put(RowConstraints.class, ListRowExcelStorable.class);
        mappings.put(ColumnConstraints.class, ListColumnExcelStorable.class);
    }

    public <C extends IExcelConstraints> ExcelStorable<C, ?> createStorable(C constraints) {
        try {
            ExcelStorable<C, ?> storable = (ExcelStorable<C, ?>) mappings.get(constraints.getClass()).newInstance();
            storable.setConstraints(constraints);
            return storable;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public Workbook loadWorkbook(InputStream inputStream, boolean isXLSX) throws IOException {
        if (inputStream != null) {
            if (isXLSX) {
                return new XSSFWorkbook(inputStream);
            } else {
                return new HSSFWorkbook(inputStream);
            }
        } else {
            Workbook workbook;
            if (isXLSX) {
                workbook = new XSSFWorkbook();
            } else {
                workbook = new HSSFWorkbook();
            }
            workbook.createSheet();
            workbook.createSheet();
            workbook.createSheet();
            return workbook;
        }
    }

    public void saveWorkbook(Workbook workbook, OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
        outputStream.close();
    }

    public void save(Workbook workbook, ExcelStorable<?, ?> storable) throws IOException {
        storable.storeIn(workbook);
    }

    public <C extends IExcelConstraints> ExcelStorable<C, ?> load(Workbook workbook, C constraints) throws IOException {
        try {
            ExcelStorable<C, ?> storable = createStorable(constraints);
            storable.load(workbook);
            return storable;
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, IOException.class);
            throw Throwables.propagate(e);
        }
    }

}
