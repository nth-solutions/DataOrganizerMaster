package dataorganizer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.incesoft.tools.excel.xlsx.Sheet;
import com.incesoft.tools.excel.xlsx.SimpleXLSXWorkbook;

import java.awt.Robot;
import java.awt.event.KeyEvent;

import java.awt.Desktop;

public class templateOpenerClass {
	public static void start(String TemplateFile, String outputFile, String datafile) throws Exception {
		 SimpleXLSXWorkbook workbook = new SimpleXLSXWorkbook(new File(TemplateFile));				//Opens template file as XLSXWorkbook
		 OutputStream o = new BufferedOutputStream(new FileOutputStream(outputFile));						//Creates output stream for writing the new file(tmp.xlsx at time of writing)
		 testWrite(workbook, o, datafile);																	//Writes all data from the datafile to outputstream with workbook as template
		 openNewFile(outputFile);																			//Opens file with default application
		 Robot robot = new Robot();																			
		 robot.delay(1000);																				    //Initial delay for application startup
		 robot.keyPress(KeyEvent.VK_CONTROL);																//Press CTRL
		 robot.keyPress(KeyEvent.VK_A);																		//Press A
		 robot.delay(50);																					//Make sure Excel has all selected
		 robot.keyRelease(KeyEvent.VK_CONTROL);																//Release CTRL
		 robot.keyRelease(KeyEvent.VK_A);																	//Release A
		 robot.keyPress(KeyEvent.VK_DELETE);																//Press Delete
		 robot.keyRelease(KeyEvent.VK_DELETE);																//Release Delete
		 robot.keyPress(KeyEvent.VK_CONTROL);																//Press CTRL
		 robot.keyPress(KeyEvent.VK_Z);																		//Press Z
		 robot.delay(50);																					//Make sure Excel undoes
		 robot.keyRelease(KeyEvent.VK_CONTROL);																//Release CTRL
		 robot.keyRelease(KeyEvent.VK_Z);																	//Release Z
		 File ofp = new File(outputFile);																	//Reference outputFile as File
		 ofp.delete();																						//Delete the outputFile from disk (Held in ram by Excel)
	 }

	private static void openNewFile(String outputFile) throws IOException {		//Opens file with windows default application (.xlsx = Excel)
		Desktop desktop = Desktop.getDesktop();									//Create desktop object
		File file = new File(outputFile);										//Create file object to reference
		desktop.open(file);														//Opens file
	}

	private static void testWrite(SimpleXLSXWorkbook workbook, OutputStream outputStream, String datafile) throws Exception {				//write datafile to outputstream with workbook template
		Sheet sheet = workbook.getSheet(0);						//Reference FIRST sheet in book (0 indexed)
	    copyDatatoTemplate(sheet, 1200, 0, datafile); 			//copy data from datafile from row 0 to row 1200 into sheet
	    workbook.commit(outputStream);							//Save
	}

	 private static void copyDatatoTemplate(Sheet sheet,int rowCount, int rowOffset, String datafile) throws IOException {					//copy data from datafile to sheet starting at rowOffset to rowCount
		     File csvData = new File(datafile);														//Open raw data file
		     List<String> CSVData = Files.readAllLines(csvData.toPath(),Charset.defaultCharset());	//Read all lines into CSVData for loop
		     int countX=0;																			//Count rows written
		     for (int r = rowOffset; r < rowCount; r++) {											//loop rows
		         int modfiedRowLength = sheet.getModfiedRowLength();								//see current Row to write
		         String[] RowData = CSVData.get(countX).split(",");									//copy all datapoints in row
		         for(int i=0;i<RowData.length;i++) {												//loop for each datapoint
		        	 sheet.modify(modfiedRowLength, i, RowData[i], null);							//write each datapoint(RowData[i]) to sheet by row(modifiedRowLength) and position in row(i) with style 'null'
		         }
		         countX++;
		     }
	 }


}
