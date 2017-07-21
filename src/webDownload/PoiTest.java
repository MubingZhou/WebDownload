package webDownload;

import java.awt.Color;
import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PoiTest {

	public static void main(String[] args) {
		try {
			// TODO Auto-generated method stub
			Workbook wb = new XSSFWorkbook();
			
			Sheet sheet  = wb.createSheet("my test sheet"); // create sheet
			
			Row row = sheet.createRow(0);  // create row
			
			Cell cell = row.createCell(0);
			
			cell.setCellValue("test first value");
			
			row.createCell(1).setCellValue(23);
			
			row.createCell(2).setCellValue("sdfsdf");
			
			Row row2 = sheet.createRow(1);
			
			row2.createCell(0).setCellValue("2nd row");
			
			// cell style
			XSSFCellStyle cellStyle =  (XSSFCellStyle) wb.createCellStyle();
			cellStyle.setBorderBottom(BorderStyle.THICK);
			
			cell.setCellStyle(cellStyle);
			
			
			
			
			// write Excel out
			FileOutputStream fo = new FileOutputStream(ConstVal.FILE_OUTPUT_PATH + "\\test.xlsx");
			
			wb.write(fo);
			
			fo.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
