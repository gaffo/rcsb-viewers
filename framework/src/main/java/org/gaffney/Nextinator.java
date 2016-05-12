package org.gaffney;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

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

        cleanupWorkbook(workbook);
        workbook = WorkbookFactory.create(new FileInputStream(this.file));

        sheet = workbook.getSheetAt(0);
        lastRow = sheet.getLastRowNum();
        firstRow = sheet.getFirstRowNum();
        currentRow = firstRow;
    }
    private static Row copyRow(Sheet worksheet, int sourceRowNum, int destinationRowNum) {
        // Get the source / new row
        Row newRow = worksheet.getRow(destinationRowNum);
        Row sourceRow = worksheet.getRow(sourceRowNum);

        // If the row exist in destination, push down all rows by 1 else create a new row
        if (newRow != null) {
            worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1);
        } else {
            newRow = worksheet.createRow(destinationRowNum);
        }

        // Loop through source columns to add to new row
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            // Grab a copy of the old/new cell
            Cell oldCell = sourceRow.getCell(i);
            Cell newCell = newRow.createCell(i);

            // If the old cell is null jump to next cell
            if (oldCell == null) {
                newCell = null;
                continue;
            }

            // Use old cell style
            newCell.setCellStyle(oldCell.getCellStyle());

            // If there is a cell comment, copy
            if (newCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }

            // If there is a cell hyperlink, copy
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }

            // Set the cell data type
            newCell.setCellType(oldCell.getCellType());

            // Set the cell data value
            switch (oldCell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_ERROR:
                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    newCell.setCellFormula(oldCell.getCellFormula());
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());
                    break;
                case Cell.CELL_TYPE_STRING:
                    newCell.setCellValue(oldCell.getRichStringCellValue());
                    break;
            }
        }
        return newRow;
    }

    private void cleanupWorkbook(Workbook workbook) throws IOException {
        Set<String> seenPdbs = new HashSet<>();

        Sheet sheet = workbook.getSheetAt(0);

        int initialLastRow = sheet.getLastRowNum();

        for (int rN = 1; rN < initialLastRow; rN++) {
            Row row = sheet.getRow(rN);
            if (row.getCell(0) == null) {
                break;
            }
            if (row.getCell(0).getStringCellValue() == null) {
                break;
            }
            String pdbsRaw = row.getCell(PDB_COLUMN).getStringCellValue();
            String pdbs[] = pdbsRaw.split(",");
            if (pdbs.length == 1) {
                continue;
            }
            row.getCell(PDB_COLUMN).setCellValue(pdbs[0]);
            for (int i = 1; i < pdbs.length; i++) {
                if (seenPdbs.contains(pdbs[i])) {
                    continue;
                }
                System.out.printf("Cleaning up row %d of %d, pdb %d\n", rN, sheet.getLastRowNum(), i);
                Row newRow = copyRow(sheet, rN, sheet.getLastRowNum() + 1);
                row.getCell(PDB_COLUMN).setCellValue(pdbs[i]);
                seenPdbs.add(pdbs[i]);
            }
        }
        workbook.write(new FileOutputStream(this.file));
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

    public String ligandId() {
        Row row = sheet.getRow(currentRow);
        Cell cell = row.getCell(0);
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
