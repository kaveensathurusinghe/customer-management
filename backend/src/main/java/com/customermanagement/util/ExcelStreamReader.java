package com.customermanagement.util;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.apache.poi.util.IOUtils;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

public class ExcelStreamReader {

    private static final int CHUNK_SIZE = 1000;

    public static void processInChunks(InputStream inputStream,
                                       Consumer<List<String[]>> chunkProcessor) {
        IOUtils.setByteArrayMaxOverride(500_000_000);
        try {
            OPCPackage pkg = OPCPackage.open(inputStream);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();

            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();

            if (sheets.hasNext()) {
                try (InputStream sheetStream = sheets.next()) {
                    List<String[]> chunk = new ArrayList<>(CHUNK_SIZE);

                    XMLReader parser = XMLReaderFactory.createXMLReader();
                    parser.setContentHandler(new DefaultHandler() {

                        private List<String> currentRow = new ArrayList<>();
                        private StringBuilder cellValue = new StringBuilder();
                        private boolean isStringCell;
                        private int rowCount = 0;
                        private int currentColIndex = -1;

                        @Override
                        public void startElement(String uri, String localName, String qName,
                                                 Attributes attributes) {
                            if ("row".equals(qName)) {
                                currentRow = new ArrayList<>();
                                for (int i = 0; i < 3; i++) currentRow.add(null);
                            } else if ("c".equals(qName)) {
                                String cellType = attributes.getValue("t");
                                isStringCell = "s".equals(cellType);
                                cellValue.setLength(0);
                                
                                String ref = attributes.getValue("r");
                                if (ref != null) {
                                    String colStr = ref.replaceAll("[0-9]", "");
                                    if (colStr.equals("A")) currentColIndex = 0;
                                    else if (colStr.equals("B")) currentColIndex = 1;
                                    else if (colStr.equals("C")) currentColIndex = 2;
                                    else currentColIndex = -1;
                                } else {
                                    currentColIndex = -1;
                                }
                            }
                        }

                        @Override
                        public void characters(char[] ch, int start, int length) {
                            cellValue.append(ch, start, length);
                        }

                        @Override
                        public void endElement(String uri, String localName, String qName) {
                            if ("c".equals(qName)) {
                                if (currentColIndex >= 0 && currentColIndex < 3) {
                                    String val;
                                    if (isStringCell) {
                                        try {
                                            int idx = Integer.parseInt(cellValue.toString().trim());
                                            val = strings.getItemAt(idx).getString();
                                        } catch (Exception e) {
                                            val = cellValue.toString();
                                        }
                                    } else {
                                        val = cellValue.toString();
                                    }
                                    currentRow.set(currentColIndex, val);
                                }
                            } else if ("row".equals(qName)) {
                                rowCount++;
                                if (rowCount == 1) return;

                                String[] rowData = new String[3];
                                for (int i = 0; i < 3; i++) {
                                    rowData[i] = currentRow.get(i);
                                }
                                chunk.add(rowData);

                                if (chunk.size() >= CHUNK_SIZE) {
                                    chunkProcessor.accept(new ArrayList<>(chunk));
                                    chunk.clear();
                                }
                            }
                        }
                    });

                    parser.parse(new InputSource(sheetStream));

                    if (!chunk.isEmpty()) {
                        chunkProcessor.accept(chunk);
                    }
                }
            }

            pkg.close();

        } catch (Exception e) {
            throw new RuntimeException("Failed to process Excel stream: " + e.getMessage(), e);
        }
    }
}