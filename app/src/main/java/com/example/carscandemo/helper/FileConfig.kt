package com.example.carscandemo.helper

import android.os.Environment
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileConfig {


    fun createExcelFile(filePath: String) {
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("Sheet 1")

        // Data to populate in the Excel sheet (without descriptions)
        val data = listOf(
            listOf(Constants.DEBUG_ON, "0"),
            listOf(Constants.GATE_NO, "E01"),
            listOf(Constants.PORT, ""),
            listOf(Constants.LOOP_PRESENT, "1"),
            listOf(Constants.MAX_DIST, ""),
            listOf(Constants.TIME_OUT, "20"),
            listOf(Constants.GATE_RELAY_TIME_BUFFER, "3000"),
            listOf(Constants.ENTRY_OR_EXIT, "E"),
            listOf(Constants.LED_PRESENT, "0"),
            listOf(Constants.DISPLAY_ON, "1"),
            listOf(Constants.HEART_BEAT_INTERVAL, "10000"),
            listOf(Constants.AUDIO_ON, "1"),
            listOf(Constants.HEADER_DISPLAY_A, "أفلاك مواقف السيارات"),
            listOf(Constants.HEADER_DISPLAY_FONT_SIZE_A, "38"),
            listOf(Constants.HEADER_DISPLAY_E, "AFLAK CAR PARK"),
            listOf(Constants.HEADER_DISPLAY_FONT_SIZE_E, "38"),
            listOf(Constants.PRINTER_SIZE, "56mm"),
            listOf(Constants.PORT_NO, "12000"),
            listOf(Constants.BEAT_WRITE_INTERVAL, "5"),
            listOf(Constants.OFC_START, "08:00"),
            listOf(Constants.OFC_END, "17:00"),
            listOf(Constants.WEEK_OFF, "6,7"),
            listOf(Constants.NETWORK_PRESENT, "1")
        )

        // Populate the data into the sheet
        for ((index, rowData) in data.withIndex()) {
            val row = sheet.createRow(index)
            rowData.forEachIndexed { cellIndex, value ->
                row.createCell(cellIndex).setCellValue(value)
            }
        }

        // Write the output to the file
        FileOutputStream(filePath).use { outputStream ->
            workbook.write(outputStream)
        }

        workbook.close()
    }



    fun readExcelFile(filePath: String): Map<String, String> {
        val colors = mutableMapOf<String, String>()

        FileInputStream(filePath).use { inputStream ->
            val workbook: Workbook = WorkbookFactory.create(inputStream)
            val sheet: Sheet = workbook.getSheetAt(0)

            // Use iterator to ensure all rows are handled, even if they have no physical data
            for (row in sheet) {
                // Ensure the row has at least two cells
                if (row.lastCellNum >= 2) {
                    val keyCell = row.getCell(0)
                    val valueCell = row.getCell(1)

                    if (keyCell != null && valueCell != null) {
                        // Get the key as a string
                        val key = when (keyCell.cellType) {
                            CellType.STRING -> keyCell.stringCellValue
                            CellType.NUMERIC -> keyCell.numericCellValue.toString() // Convert numeric to string
                            else -> "" // Handle other cell types if necessary
                        }

                        // Get the value as a string
                        val value = when (valueCell.cellType) {
                            CellType.STRING -> valueCell.stringCellValue
                            CellType.NUMERIC -> valueCell.numericCellValue.toString() // Convert numeric to string
                            else -> "" // Handle other cell types if necessary
                        }

                        // Ensure key and value are not empty before adding them to the map
                        if (key.isNotEmpty() && value.isNotEmpty()) {
                            colors[key] = value
                        }
                    }
                }
            }

            workbook.close()
        }

        return colors
    }

}