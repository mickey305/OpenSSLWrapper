package com.mickey305.openssl.wrapper.scalafx.plugin;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Created by K.Misaki on 2017/08/12.
 *
 * 参考URL：http://qiita.com/tool-taro/items/4b3c802bb114a9110ecb
 *
 */
public class ExcelExporter<E> {
    private enum Style {
        Header, String, StringWrap, Int, Double, Yen, Percent, Datetime
    }

    private String path;
    private Map<Style, CellStyle> styles;

    public ExcelExporter(String path) {
        this.setPath(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<Style, CellStyle> getStyles() {
        return styles;
    }

    public void setStyles(Map<Style, CellStyle> styles) {
        this.styles = styles;
    }

    protected void execute(final List<E> lines, Function<E, String[]> callback) throws IOException {
        final String outputFilePath = this.getPath();
        Workbook book = null;
        FileOutputStream fout = null;

        this.setStyles(new HashMap<>());
        Map<Style, CellStyle> styles = this.getStyles();

        try {
            book = new SXSSFWorkbook();

            Font font = book.createFont();
            font.setFontName("ＭＳ ゴシック");
            font.setFontHeightInPoints((short) 9);

            DataFormat format = book.createDataFormat();

            //ヘッダ文字列用のスタイル
            CellStyle style_header = book.createCellStyle();
            style_header.setBorderBottom(BorderStyle.THIN);
            ExcelExporter.setBorder(style_header, BorderStyle.THIN);
            style_header.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_CORNFLOWER_BLUE.getIndex());
            style_header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style_header.setVerticalAlignment(VerticalAlignment.TOP);
            style_header.setFont(font);
            styles.put(Style.Header, style_header);

            //文字列用のスタイル
            CellStyle style_string = book.createCellStyle();
            ExcelExporter.setBorder(style_string, BorderStyle.THIN);
            style_string.setVerticalAlignment(VerticalAlignment.TOP);
            style_string.setFont(font);
            styles.put(Style.String, style_string);

            //改行が入った文字列用のスタイル
            CellStyle style_string_wrap = book.createCellStyle();
            ExcelExporter.setBorder(style_string_wrap, BorderStyle.THIN);
            style_string_wrap.setVerticalAlignment(VerticalAlignment.TOP);
            style_string_wrap.setWrapText(true);
            style_string_wrap.setFont(font);
            styles.put(Style.StringWrap, style_string_wrap);

            //整数用のスタイル
            CellStyle style_int = book.createCellStyle();
            ExcelExporter.setBorder(style_int, BorderStyle.THIN);
            style_int.setDataFormat(format.getFormat("#,##0;-#,##0"));
            style_int.setVerticalAlignment(VerticalAlignment.TOP);
            style_int.setFont(font);
            styles.put(Style.Int, style_int);

            //小数用のスタイル
            CellStyle style_double = book.createCellStyle();
            ExcelExporter.setBorder(style_double, BorderStyle.THIN);
            style_double.setDataFormat(format.getFormat("#,##0.0;-#,##0.0"));
            style_double.setVerticalAlignment(VerticalAlignment.TOP);
            style_double.setFont(font);
            styles.put(Style.Double, style_double);

            //円表示用のスタイル
            CellStyle style_yen = book.createCellStyle();
            ExcelExporter.setBorder(style_yen, BorderStyle.THIN);
            style_yen.setDataFormat(format.getFormat("\"\\\"#,##0;\"\\\"-#,##0"));
            style_yen.setVerticalAlignment(VerticalAlignment.TOP);
            style_yen.setFont(font);
            styles.put(Style.Yen, style_yen);

            //パーセント表示用のスタイル
            CellStyle style_percent = book.createCellStyle();
            ExcelExporter.setBorder(style_percent, BorderStyle.THIN);
            style_percent.setDataFormat(format.getFormat("0.0%"));
            style_percent.setVerticalAlignment(VerticalAlignment.TOP);
            style_percent.setFont(font);
            styles.put(Style.Percent, style_percent);

            //日時表示用のスタイル
            CellStyle style_datetime = book.createCellStyle();
            ExcelExporter.setBorder(style_datetime, BorderStyle.THIN);
            style_datetime.setDataFormat(format.getFormat("yyyy/mm/dd hh:mm:sss"));
            style_datetime.setVerticalAlignment(VerticalAlignment.TOP);
            style_datetime.setFont(font);
            styles.put(Style.Datetime, style_datetime);

            book = createSheet(book, lines, callback);

            //ファイル出力
            fout = new FileOutputStream(outputFilePath);
            book.write(fout);
        }
        finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    // nop
                }
            }
            if (book != null) {
                try {
                    /*
                        SXSSFWorkbookはメモリ空間を節約する代わりにテンポラリファイルを大量に生成するため、
                        不要になった段階でdisposeしてテンポラリファイルを削除する必要がある
                     */
                    ((SXSSFWorkbook) book).dispose();
                }
                catch (Exception e) {
                    // nop
                }
            }
        }
    }

    private Workbook createSheet(Workbook book, List<E> lines, Function<E, String[]> callback) {
        Row row;
        int rowNumber;
        Cell cell;
        int colNumber;
        Map<Style, CellStyle> styles = this.getStyles();

        Sheet sheet = book.createSheet();
        if (sheet instanceof SXSSFSheet) {
            ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        }

        //シート名称の設定
        book.setSheetName(0, "Log List");

        //ヘッダ行の作成
        rowNumber = 0;
        colNumber = 0;
        row = sheet.createRow(rowNumber);

        cell = row.createCell(colNumber++);
        cell.setCellStyle(styles.get(Style.Header));
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ID");

        cell = row.createCell(colNumber++);
        cell.setCellStyle(styles.get(Style.Header));
        cell.setCellType(CellType.STRING);
        cell.setCellValue("PID");

        cell = row.createCell(colNumber++);
        cell.setCellStyle(styles.get(Style.Header));
        cell.setCellType(CellType.STRING);
        cell.setCellValue("COMMAND");

        cell = row.createCell(colNumber++);
        cell.setCellStyle(styles.get(Style.Header));
        cell.setCellType(CellType.STRING);
        cell.setCellValue("TIMESTAMP(START)");

        cell = row.createCell(colNumber);
        cell.setCellStyle(styles.get(Style.Header));
        cell.setCellType(CellType.STRING);
        cell.setCellValue("TIMESTAMP(END)");

        //ウィンドウ枠の固定
        sheet.createFreezePane(0, 1);

        //ヘッダ行にオートフィルタの設定
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, colNumber));

        //列幅の自動調整
        for (int j = 0; j <= colNumber; j++) {
            sheet.autoSizeColumn(j, true);
        }

        //データ行の生成(10行作ってみる)
        for (int j = 0; j < lines.size(); j++) {
            rowNumber++;
            colNumber = 0;
            row = sheet.createRow(rowNumber);
            String[] data = callback.apply(lines.get(j));

            cell = row.createCell(colNumber++);
            cell.setCellStyle(styles.get(Style.Int));
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(data[0]);

            cell = row.createCell(colNumber++);
            cell.setCellStyle(styles.get(Style.Int));
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(data[1]);

            cell = row.createCell(colNumber++);
            cell.setCellStyle(styles.get(Style.String));
            cell.setCellType(CellType.STRING);
            cell.setCellValue(data[2]);

            cell = row.createCell(colNumber++);
            cell.setCellStyle(styles.get(Style.Datetime));
            cell.setCellType(CellType.STRING);
            cell.setCellValue(data[3]);

            cell = row.createCell(colNumber++);
            cell.setCellStyle(styles.get(Style.Datetime));
            cell.setCellType(CellType.STRING);
            cell.setCellValue(data[4]);

            //列幅の自動調整
            for (int k = 0; k <= colNumber; k++) {
                sheet.autoSizeColumn(k, true);
            }
        }

        return book;
    }

    private static void setBorder(CellStyle style, BorderStyle border) {
        style.setBorderBottom(border);
        style.setBorderTop(border);
        style.setBorderLeft(border);
        style.setBorderRight(border);
    }
}
