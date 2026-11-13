package com.smarterevententrysystem.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.swing.table.TableModel;

/** Saves a JTable's data as a .csv file that Excel opens directly. */
public class CsvExporter {

    private CsvExporter() { }

    public static void export(File file, TableModel model) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');   // lets Excel detect UTF-8
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (c > 0) {
                    writer.write(',');
                }
                writer.write(escape(model.getColumnName(c)));
            }
            writer.write("\r\n");
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    if (c > 0) {
                        writer.write(',');
                    }
                    writer.write(escape(model.getValueAt(r, c)));
                }
                writer.write("\r\n");
            }
        }
    }

    private static String escape(Object value) {
        String text = value == null ? "" : value.toString();
        // stop Excel from treating text like a formula
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
