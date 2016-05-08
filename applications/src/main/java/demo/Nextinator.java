package demo;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import java.io.File;
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

    public static void init() throws IOException, InvalidFormatException {
        nextinator = new Nextinator();
        nextinator.doInit();
    }

    public static Nextinator get() {
        return nextinator;
    }

    public void doInit() throws IOException, InvalidFormatException {
        this.file = getFile();
        workbook = WorkbookFactory.create(this.file);
        sheet = workbook.getSheetAt(0);
        lastRow = sheet.getLastRowNum();
        firstRow = sheet.getFirstRowNum();
        currentRow = firstRow;
    }

    public String nextPDBId() {
        currentRow++;
        Row row = sheet.getRow(currentRow);
        Cell cell = row.getCell(PDB_COLUMN);
        return cell.getStringCellValue();
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
