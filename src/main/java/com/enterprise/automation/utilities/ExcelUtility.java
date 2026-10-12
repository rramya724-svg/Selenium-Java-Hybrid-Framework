package com.enterprise.automation.utilities;

import com.enterprise.automation.config.ConfigReader;
import com.enterprise.automation.constants.FrameworkConstants;
import com.enterprise.automation.enums.ConfigKey;
import com.enterprise.automation.exceptions.FrameworkException;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public final class ExcelUtility {
    private static final Logger LOGGER = LoggerUtility.getLogger(ExcelUtility.class);
    private static final ReentrantLock WRITE_LOCK = new ReentrantLock();
    private static final ThreadLocal<DataFormatter> DATA_FORMATTER = ThreadLocal.withInitial(DataFormatter::new);

    private ExcelUtility() {
        throw new IllegalStateException("ExcelUtility is a utility class and must not be instantiated");
    }

    public static List<TestDataRow> readTestData() {
        return readTestData(workbookPath(), ConfigReader.get(ConfigKey.EXCEL_SHEET_NAME, "TestData"));
    }

    public static List<TestDataRow> readTestData(Path workbookPath, String sheetName) {
        List<TestDataRow> rows = new ArrayList<>();
        try (InputStream inputStream = Files.newInputStream(workbookPath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = resolveSheet(workbook, sheetName, workbookPath);
            List<String> headers = readHeaders(sheet);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isBlankRow(row, headers.size())) {
                    continue;
                }
                Map<String, String> values = new LinkedHashMap<>();
                for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                    values.put(headers.get(columnIndex), readCell(row.getCell(columnIndex)));
                }
                rows.add(new TestDataRow(rowIndex, values));
            }
        } catch (IOException ioException) {
            throw new FrameworkException("Unable to read the test data workbook: " + workbookPath, ioException);
        } finally {
            DATA_FORMATTER.remove();
        }
        LOGGER.info("Read {} data row(s) from worksheet [{}]", rows.size(), sheetName);
        return rows;
    }

    public static Optional<TestDataRow> findByTestCaseId(String testCaseId) {
        return readTestData().stream().filter(row -> row.testCaseId().equalsIgnoreCase(testCaseId)).findFirst();
    }

    public static List<TestDataRow> readExecutableTestData() {
        return readTestData().stream().filter(TestDataRow::isExecutable).toList();
    }

    public static void writeExecutionResult(String testCaseId,
                                            String status,
                                            long executionMillis,
                                            String screenshotPath) {
        Path workbookPath = workbookPath();
        String sheetName = ConfigReader.get(ConfigKey.EXCEL_SHEET_NAME, "TestData");
        WRITE_LOCK.lock();
        try {
            updateWorkbook(workbookPath, sheetName, testCaseId, status, executionMillis, screenshotPath);
        } catch (IOException ioException) {
            LOGGER.error("Unable to write the execution result for [{}] into the workbook", testCaseId, ioException);
        } finally {
            DATA_FORMATTER.remove();
            WRITE_LOCK.unlock();
        }
    }

    private static void updateWorkbook(Path workbookPath,
                                       String sheetName,
                                       String testCaseId,
                                       String status,
                                       long executionMillis,
                                       String screenshotPath) throws IOException {
        Workbook workbook;
        try (InputStream inputStream = Files.newInputStream(workbookPath)) {
            workbook = new XSSFWorkbook(inputStream);
        }
        try (workbook) {
            Sheet sheet = resolveSheet(workbook, sheetName, workbookPath);
            List<String> headers = readHeaders(sheet);
            int idColumn = headers.indexOf(FrameworkConstants.COLUMN_TEST_CASE_ID);
            if (idColumn < 0) {
                LOGGER.warn("Column [{}] is missing; execution results cannot be written back",
                        FrameworkConstants.COLUMN_TEST_CASE_ID);
                return;
            }
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || !testCaseId.equalsIgnoreCase(readCell(row.getCell(idColumn)))) {
                    continue;
                }
                writeCell(row, headers, FrameworkConstants.COLUMN_STATUS, status.toUpperCase(Locale.ROOT));
                writeCell(row, headers, FrameworkConstants.COLUMN_EXECUTION_TIMESTAMP,
                        DateUtility.currentDisplayTimestamp());
                writeCell(row, headers, FrameworkConstants.COLUMN_EXECUTION_TIME_MS, String.valueOf(executionMillis));
                writeCell(row, headers, FrameworkConstants.COLUMN_SCREENSHOT_PATH,
                        screenshotPath == null ? FrameworkConstants.EMPTY_STRING : screenshotPath);
                break;
            }
            try (OutputStream outputStream = Files.newOutputStream(workbookPath)) {
                workbook.write(outputStream);
            }
            LOGGER.info("Execution result [{}] written back for test case [{}]", status, testCaseId);
        }
    }

    private static Path workbookPath() {
        String configured = ConfigReader.get(ConfigKey.EXCEL_PATH, "src/test/resources/testdata.xlsx");
        Path path = Path.of(configured);
        return path.isAbsolute() ? path : Path.of(FrameworkConstants.USER_DIRECTORY).resolve(configured);
    }

    private static Sheet resolveSheet(Workbook workbook, String sheetName, Path workbookPath) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new FrameworkException("Worksheet '" + sheetName + "' does not exist in " + workbookPath);
        }
        return sheet;
    }

    private static List<String> readHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new FrameworkException("Worksheet '" + sheet.getSheetName() + "' has no header row");
        }
        List<String> headers = new ArrayList<>();
        for (int columnIndex = 0; columnIndex < headerRow.getLastCellNum(); columnIndex++) {
            headers.add(readCell(headerRow.getCell(columnIndex)));
        }
        return headers;
    }

    private static boolean isBlankRow(Row row, int columnCount) {
        if (row == null) {
            return true;
        }
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            Cell cell = row.getCell(columnIndex);
            if (cell != null && cell.getCellType() != CellType.BLANK && !readCell(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String readCell(Cell cell) {
        if (cell == null) {
            return FrameworkConstants.EMPTY_STRING;
        }
        return DATA_FORMATTER.get().formatCellValue(cell).trim();
    }

    private static void writeCell(Row row, List<String> headers, String columnName, String value) {
        int columnIndex = headers.indexOf(columnName);
        if (columnIndex < 0) {
            LOGGER.debug("Column [{}] is not present in the worksheet; value not written", columnName);
            return;
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        cell.setCellValue(value);
    }
}
