package org.gaffney;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mike on 5/8/2016.
 */
public class Nextinator {
    private static Nextinator nextinator;
    private File file;
    private Workbook workbook;
    private Sheet sheet;
    private int lastRow;
    private int firstRow;
    private int currentRow;
    private static final int PDB_COLUMN = 8;
    private static final int OUTPUT_COLUMN = 11;

    public static void init() throws IOException, InvalidFormatException {
        nextinator = new Nextinator();
        nextinator.doInit();
    }

    public static Nextinator get() {
        return nextinator;
    }

    public void doInit() throws IOException, InvalidFormatException {
        this.file = getFile();
        workbook = WorkbookFactory.create(new FileInputStream(this.file));
        sheet = workbook.getSheetAt(0);
        lastRow = sheet.getLastRowNum();
        firstRow = sheet.getFirstRowNum();
        currentRow = firstRow;
    }

    public String nextPDBId() {
        Cell cell = null;
        while (true) {
            currentRow++;
            if (currentRow > this.lastRow) {
                return null;
            }
            Row row = sheet.getRow(currentRow);
            cell = row.getCell(PDB_COLUMN);
            Cell resultCell = row.getCell(OUTPUT_COLUMN);
            if (resultCell == null) {
                break;
            }
            String value = resultCell.getStringCellValue();
            if (value.equalsIgnoreCase("no")){
                continue;
            }
            if (value.equalsIgnoreCase("yes")) {
                continue;
            }
            break;
        }
        return cell.getStringCellValue();
    }

    public void setValue(String value, String comment) throws IOException, InvalidFormatException {
        Row row = sheet.getRow(currentRow);
        row.getCell(OUTPUT_COLUMN, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(value);
        row.getCell(OUTPUT_COLUMN + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(comment);
        workbook.write(new FileOutputStream(file));
    }

    private File getFile() {
        File f = null;
        while (f == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDragEnabled(true);
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            int r = fileChooser.showOpenDialog(null);

            if (r != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(null, "You must choose a file, try again.");
                continue;
            }

            f = fileChooser.getSelectedFile();
            if (f == null) {
                JOptionPane.showMessageDialog(null, "You must choose a file, try again.");
                continue;
            }
        }
        return f;
    }
}
