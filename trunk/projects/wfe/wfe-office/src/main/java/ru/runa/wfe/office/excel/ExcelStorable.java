package ru.runa.wfe.office.excel;

import org.apache.poi.ss.usermodel.Workbook;

public abstract class ExcelStorable<C extends IExcelConstraints, Data> {
    protected C constraints;
    protected Data data;

    public C getConstraints() {
        return constraints;
    }

    public void setConstraints(C constraints) {
        this.constraints = constraints;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    protected abstract void storeIn(Workbook workbook);

    protected abstract void load(Workbook workbook);

}
