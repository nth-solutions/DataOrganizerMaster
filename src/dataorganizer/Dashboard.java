package dataorganizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JTabbedPane;
import java.awt.Font;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;

import java.awt.CardLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.PureJavaIllegalStateException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;

public class Dashboard extends JFrame {

	private JPanel contentPanel;


	//GUI Elements, define here if they need to be accessed in the Frame class
	//Panels
	private JTabbedPane mainTabbedPanel;
	private JPanel startReadButtonPanel;
	private JPanel fileNamePanel;
	private JPanel paramPanel;
	private JPanel getModuleIDPanel;
	private JPanel fileNameModifierPanel;
	private JPanel fileLocationPanel;
	private JPanel serialPortPanel;
	//Labels
	private JLabel generalStatusLabel;
	private JLabel moduleSerialNumberLabel;
	private JLabel hardwareIDLabel;
	private JLabel firmwareIDLabel;

	//CheckBoxes
	private JCheckBox timedTestCheckbox;
	private JCheckBox delayAfterStartCheckbox;
	private JCheckBox manualCalibrationCheckbox;

	//Text Fields
	//Configuration Tab
	private JTextField testLengthTextField;
	private JTextField accelGyroSampleRateTextField;
	private JTextField magSampleRateTextField;
	private JTextField delayAfterStartTextField;
	private JTextField timer0TickThreshTextField;
	//Read Tab
	private JTextField prefixTextField;
	private JTextField suffixTextField;
	private JTextField fileNameTextField;
	private JTextField numTestsTextFieldRead;
	private JTextField testLengthTextFieldRead;
	private JTextField accelGyroSampleRateTextFieldRead;
	private JTextField magSampleRateTextFieldRead;
	private JTextField accelSensitivityTextFieldRead;
	private JTextField gyroSensitivityTextFieldRead;
	private JTextField accelFilterTextFieldRead;
	private JTextField gyroFilterTextFieldRead;

	//Combo Boxes
	private JComboBox commPortCombobox;
	private JComboBox accelSensitivityCombobox;
	private JComboBox gyroSensitivityCombobox;
	private JComboBox accelFilterCombobox;
	private JComboBox gyroFilterCombobox;

	//Buttons
	private JButton refreshPortButton;
	private JButton disconnectButton;
	private JButton getCurrentConfigsButton;
	private JButton writeConfigsButton;
	private JButton getModuleIDButton;
	private JButton browseButton;
	private JButton readDataButton;

	//Progress Bars
	private JProgressBar progressBar;

	//Test Parameter Variables and Constants
	public static final int NUM_TEST_PARAMETERS = 13;

	//Test Parameters (All must be of type "int")
	private int timedTestFlag;
	private int testLength;      			
	private int accelGyroSampleRate;    		
	private int magSampleRate;          			
	private int accelSensitivity;       		
	private int gyroSensitivity;        		
	private int accelFilter;            			
	private int gyroFilter;             		

	//Flags
	private boolean readMode = true;
	private boolean organizeAbort = false;
	private boolean readAbort = false;
	private boolean paramAbort = false;
	private boolean portInitialized = false;
	private boolean frameInitialized = false;
	private boolean portOpened = false;
	private boolean dataStreamsInitialized = false;

	private Thread readThread;
	private Thread organizeThread;
	private Thread paramThread;

	//Output File Info and Variables
	private String nameOfFile = "";     			//Sets the name of file to an empty string to start
	private String fileOutputDirectoryStr;			//The directory to write the test to
	private int expectedTestNum = 0;                //The number of test that are expected to be received 
	private Organizer organizer = new Organizer();  //Object used for creating .CSV files

	//Serial Port Variables
	private SerialPort serialPort;      			//Object for the serial port class
	private static CommPortIdentifier portId;       //Object used for opening a comm ports
	private static Enumeration portList;            //Object used for finding comm ports
	private InputStream inputStream;                //Object used for reading serial data 
	private OutputStream outputStream;              //Object used for writing serial data

	public static Dashboard frameInstance;
	private JPanel erasePanel;
	private JPanel panel;
	private JButton bulkEraseButton;
	private JButton sectorEraseButton;





	private Dashboard() {
		setTitle("Data Organizer Master");
		createComponents();
		initDataFields();
		setVisible(true);
	}

	/**
	 * Necessary for singleton design pattern, especially the "synchronized" keyword
	 * @return the dashboard instance, singleton pattern specifies only one instance can exist
	 */
	public static synchronized Dashboard getFrameInstance() {
		if (frameInstance == null) {
			frameInstance = new Dashboard();
		}
		return frameInstance;
	}



	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch(Exception e) {
			System.out.println("Error Setting Look and Feel: " + e);
		}

		Dashboard frame = Dashboard.getFrameInstance();
		frame.findPorts();


		while(true) {

		}
	}


	//Serial Port Methods******************************************************************************************************************************
	/**
	 * Builds a list the names of all the serial ports to place in the combo box
	 * @param evt event pasted in by any button or action that this method was called by (method of passing info related to the source)
	 */
	private void findPorts() {
		//Fills the portEnum data structure (functions like arrayList) with ports (data type that encapsulates the name and hardware interface info)
		Enumeration<CommPortIdentifier> portEnumList = CommPortIdentifier.getPortIdentifiers();   

		//Stores the names of the ports
		ArrayList<String> portNames = new ArrayList<String>();

		//Iterate through each port object in the portEnumList and stores the name of the port in the portNames array
		while (portEnumList.hasMoreElements()) {                   //adds the serial ports to a string array
			CommPortIdentifier portIdentifier = portEnumList.nextElement();
			portNames.add(portIdentifier.getName());
		}

		//If at least 1 serial port is found, fill the combo box with all the known port names. Otherwise, notify the user that there are no visible dongles. 
		if (portNames.size() > 0) {
			commPortCombobox.setEnabled(true);
			commPortCombobox.setModel(new DefaultComboBoxModel(portNames.toArray()));
		}
		else {
			generalStatusLabel.setText("No Serial Dongle Found");
		}
	}

	/**
	 * This method handles which methods will be called when the user selects a port from the comm port combobox
	 */
	private void portSelectedHandler() {

		if (commPortCombobox.getSelectedItem() != null) {

			String selectedCommID = commPortCombobox.getSelectedItem().toString();      //creates a string of selected item; Name of the comm port as a string

			openSerialPort(selectedCommID);                                        //opens the serial port with the selected comm Port


			generalStatusLabel.setText("Serial Port Opened Successfully, Awaiting Commands");

			portInitialized = true;
		}

	}


	/**
	 * Opens serial port with the name passed in as a parameter in addition to initializing input and output streams.
	 * @param commPortID Name of comm port that will be opened
	 */
	public void openSerialPort(String commPortID) {                     //Method that creates the serial port
		portList = CommPortIdentifier.getPortIdentifiers();                     //creates list of avaiable com ports

		while (portList.hasMoreElements()) {                                    //Loops through the com ports
			CommPortIdentifier tempPortId = (CommPortIdentifier) portList.nextElement();
			if (tempPortId.getName().equals(commPortID)) {                             //If the avaliable Comm Port equals the comm port that aws selected earlier
				portId = tempPortId;
				break;
			}
		}

		try {
			serialPort = (SerialPort) portId.open("portHandler", 2000);
			portOpened = true;
		} 
		catch (PortInUseException e) {
			generalStatusLabel.setText("Dongle Already In Use");
		}

		if (portOpened) {
			try {
				inputStream = serialPort.getInputStream();              //creates input stream
				outputStream = serialPort.getOutputStream();            //creates output stream
				dataStreamsInitialized = true;
				disconnectButton.setEnabled(true);
				getModuleIDButton.setEnabled(true);
				readDataButton.setEnabled(true);
				writeConfigsButton.setEnabled(true);
			} 
			catch (IOException e) {
				generalStatusLabel.setText("Error Communicating with Serial Dongle");
			}

		}
	}

	/**
	 * Updates the serial port settings (specifically baud rate) based on which tab is currently selected
	 */
	public void updateSerialPortSettings() {
		if (portOpened) {
			stopAllThreads();
			//Read Mode
			if (mainTabbedPanel.getSelectedIndex() == 0) {
				try {
					serialPort.setSerialPortParams(115200,      //Opens the serial port at 115200 Baud for high speed reading
							SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					outputStream = serialPort.getOutputStream();
					inputStream = serialPort.getInputStream();
				} 
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Serial Dongle Compatability!");
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating with Dongle");
				}
			}
			//Configuration Mode
			else if (mainTabbedPanel.getSelectedIndex() == 1) {
				try {
					serialPort.setSerialPortParams(38400,      //Opens the serial port at 115200 Baud for high speed reading
							SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					outputStream = serialPort.getOutputStream();
					inputStream = serialPort.getInputStream();
				} 
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Serial Dongle Compatability!");
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating with Dongle");
				}
			}

			//Calibration Mode
			else if (mainTabbedPanel.getSelectedIndex() == 2) {
				try {
					serialPort.setSerialPortParams(115200,      //Opens the serial port at 115200 Baud for high speed reading
							SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					outputStream = serialPort.getOutputStream();
					inputStream = serialPort.getInputStream();
				} 
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Serial Dongle Compatability!");
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating with Dongle");
				}		
			}
		}
	}

	/**
	 * Closes serial port and updates gui labels/ software flags
	 */
	private void closeSerialPort() {
		//If the disconnect button is pressed: disconnects from the serial port and resets the UI   
		if (serialPort != null) {
			stopAllThreads();
			serialPort.close();     //closes the serial port
			portInitialized = false;
			portOpened = false;
			dataStreamsInitialized = false;
			generalStatusLabel.setText("Port Closed");                              //says the port closed
			disconnectButton.setEnabled(false);
			getModuleIDButton.setEnabled(false);
			readDataButton.setEnabled(false);
			writeConfigsButton.setEnabled(false);
		}
	}
	public void stopParamThread() {
		if (paramThread != null) {
			if(!paramThread.isAlive()) {
				paramAbort = true;
				while(paramThread.isAlive()) {
				}
				paramAbort = false;
			}
		}
	}

	public void stopReadThread() {
		if (readThread != null) {
			if(!readThread.isAlive()) {
				readAbort = true;
				while(readThread.isAlive()) {
				}
				readAbort = false;
			}
		}
	}

	public void stopAllThreads() {

		//Read Data Thread
		stopReadThread();

		//Send Parameters Thread
		stopParamThread();
	}

	public void clearInputStream(){
		if (dataStreamsInitialized) {
			try {
				while (inputStream.available() > 0) {
					inputStream.read();
				}
			}
			catch (IOException e) {
				generalStatusLabel.setText("Error Communicating with Dongle");
			}

		}
	}


	//Read and Write Methods***************************************************************************************************************************

	public boolean waitForPreamble(int start, int stop) {
		try {
			long startTime = System.currentTimeMillis();
			boolean preambleReceived = false;
			while (((System.currentTimeMillis() - startTime) < 500)) {
				if (inputStream.available() > 0) {
					int temp;
					for(int counter = start; counter <= stop;) {
						temp = inputStream.read();
						if (temp == counter) {    
							counter++;
						} 
						else {
							counter = 1;
						}
					}
					preambleReceived = true;
					break;
					//System.out.println(temp);
				}
			}
			if (!preambleReceived) {
				generalStatusLabel.setText("Module Unresponsive (Timeout), Try Again");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(255, 0, 0));
				return false;
			}
		}
		catch(IOException e) {
			generalStatusLabel.setText("Error Commicating with Dongle");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255, 0, 0));
		}





		return true;
	}

	public boolean configureForHandshake() {
		try {
			serialPort.setSerialPortParams(38400,      //Opens the serial port at 115200 Baud for high speed reading
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			outputStream = serialPort.getOutputStream();
			inputStream = serialPort.getInputStream();
		} 
		catch (UnsupportedCommOperationException e) {
			generalStatusLabel.setText("Check Serial Dongle Compatability!");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		}
		catch (IOException e) {
			generalStatusLabel.setText("Error Communicating with Dongle");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		}
		return true;

	}

	public boolean configureForImport() {
		try {
			serialPort.setSerialPortParams(115200,      //Opens the serial port at 115200 Baud for high speed reading
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			outputStream = serialPort.getOutputStream();
			inputStream = serialPort.getInputStream();
		} 
		catch (UnsupportedCommOperationException e) {
			generalStatusLabel.setText("Check Serial Dongle Compatability!");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		}
		catch (IOException e) {
			generalStatusLabel.setText("Error Communicating with Dongle");
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			return false;
		}
		return true;

	}


	public boolean selectMode(char modeDelimiter) {
		try {
			if(!configureForHandshake()) {
				return false;
			}


			clearInputStream();

			int attemptCounter = 0;
			boolean modeSelected = false;
			while(!modeSelected) {
				//Start condition followed by 'S' to tell firmware to start saving new parameters
				outputStream.write(new String("1111" + modeDelimiter).getBytes());

				//long startTime = System.currentTimeMillis();
				int temp = -1;
				long startTime = System.currentTimeMillis();
				while((System.currentTimeMillis() - startTime) < 50) {
					if (inputStream.available() > 0) {
						temp = inputStream.read();
						break;
					}	
				}
				if (temp == (int)modeDelimiter) {
					modeSelected = true;
					return true;
				}
				//If an unknown character or '?' is received, try sending again
				else {
					attemptCounter++;
				}

				if (attemptCounter == 10) {

					//Command not recognized module
					if (temp == '?') {
						generalStatusLabel.setText("Command Not Recognized by Module, Check Firmware Version");

					}

					else if (temp == -1) {
						generalStatusLabel.setText("Module Unresponsive or Connected Improperly (Timeout)");
					}

					else {
						generalStatusLabel.setText("Communication Error, Try Again");
					}
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255,0,0));
					//Exit method, communication failed
					return false;
				}
			}

		}
		catch (IOException e) {                                          //If there is an IOException
			generalStatusLabel.setText("Error Communicating with Dongle");    //Notify the user that something broke
			Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, e);
			//Exit method, communication failed
			return false;
		} 
		catch (NullPointerException e) {                                  //If there is a NullPointer
			generalStatusLabel.setText("Please Select a Port");  //The serial port was not open; notifies the user about the mistake
			Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, e);
			//Exit method, communication failed
			return false;
		}
		return true;
	}
	public void getModuleInfoButtonHandler() {
		getModuleIDButton.setEnabled(false);
		getModuleInfo();
		getModuleIDButton.setEnabled(true);
	}

	public boolean getModuleInfo() {
		//stopAllThreads();

		//Configure Baud Rate for 38400 temporarily
		configureForHandshake();


		if (dataStreamsInitialized) {

			if(!selectMode('I')) {
				return false;
			}

			try {

				//System.out.println("Start Condition");
				boolean infoReceived = false;
				int temp;
				int idCounter = 0;
				int [] moduleInfo = new int[3];
				long startTime = System.currentTimeMillis();
				while (idCounter < 3 && (System.currentTimeMillis() - startTime) < 1500) {

					if(!waitForPreamble(1,4)) {
						return false;
					}

					if (inputStream.available() >= 2) {
						temp = inputStream.read() * 256 + inputStream.read();
						//System.out.println(temp);
						outputStream.write(temp / 256);
						outputStream.write(temp % 256);

						long echoStart = System.currentTimeMillis();
						while (((System.currentTimeMillis() - echoStart) < 200)) {

							if (inputStream.available() >= 2) {
								startTime = System.currentTimeMillis(); // Reset Timer

								if (inputStream.read() == 'C') {

									if (inputStream.read() == 'A') {
										moduleInfo[idCounter] = temp;
										idCounter++;
										System.out.println(idCounter);
									}
								}
							}
							if (idCounter == 3) {
								infoReceived = true;
								break;
							}
						}
					}

				}

				//System.out.println("Stop Condition");
				if (!infoReceived) {
					generalStatusLabel.setText("Module Unresponsive (Timeout), Try Again");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				else {
					moduleSerialNumberLabel.setText("Module Serial Number: " + moduleInfo[0]);
					hardwareIDLabel.setText("Module Hardware ID: " + moduleInfo[1] + "x");
					firmwareIDLabel.setText("Module Firmware ID: " + moduleInfo[2]);
					generalStatusLabel.setText("Module Information Successful Received");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(51, 204, 51));
				}
				return true;
			}
			catch (IOException e) {
				generalStatusLabel.setText("Error Communicating with Dongle");
			}
		}
		return false;
	}


	/**
	 * Handles the methods that are called when the write button is pressed
	 */
	private void writeButtonHandler() {
		stopAllThreads();
		Runnable sendParamOperation = new Runnable() {
			public void run() {
				writeConfigsButton.setEnabled(false);
				sendParameters();
				writeConfigsButton.setEnabled(true);
			}
		};

		Thread paramThread = new Thread(sendParamOperation);
		paramThread.start();

	}

	/**
	 * This method handles the transmission of test parameters to the module with build in handshakes to verify each parameter is correctly received
	 * @return flag that states whether the operation was successful (used mostly for easy exiting of this method upon comm failure)
	 */
	private boolean sendParameters() {
		//Method for writing data to URI module. Sends programming config to URI module    
		configureForHandshake();
		progressBar.setValue(0);

		try {
			if (dataStreamsInitialized && !paramAbort) {

				if(!selectMode('S')) {
					return false;
				}

				if (timedTestCheckbox.isSelected()) {
					timedTestFlag = 1;
				}
				else {
					timedTestFlag = 0;
				}

				int[] writeData = new int[NUM_TEST_PARAMETERS];
				writeData[0] = 0;			//Serial Number
				writeData[1] = 5;			//Module ID (Hardware Version)
				writeData[2] = 16;			//Firmware ID 
				writeData[3] = getTickThreshold(Integer.parseInt(accelGyroSampleRateTextField.getText()));		//Timer0 Tick Threshold (Interrupt)
				writeData[4] = 0;			//Delay After Start
				writeData[5] = timedTestFlag;			//Timed Test Flag
				writeData[6] = Integer.parseInt(testLengthTextField.getText());     //Test Duration
				writeData[7] = Integer.parseInt(accelGyroSampleRateTextField.getText());//Accel Gyro Sample Rate
				writeData[8] = Integer.parseInt(magSampleRateTextField.getText());    //Mag Sample Rate
				writeData[9] = Integer.parseInt(accelSensitivityCombobox.getSelectedItem().toString());  //Accel Sensitivity
				writeData[10] = Integer.parseInt(gyroSensitivityCombobox.getSelectedItem().toString());   //Gyro Sensitivity
				writeData[11] = Integer.parseInt(accelFilterCombobox.getSelectedItem().toString());  //Accel Filter
				writeData[12] = Integer.parseInt(gyroFilterCombobox.getSelectedItem().toString());  //Gyro Filter

				for (int paramNum = 0; paramNum < writeData.length; paramNum++) {
					boolean paramReceived = false;
					progressBar.setValue((int)(100 * ((double)paramNum / (double)writeData.length) / 1.2));
					int attemptCounter = 0;
					while (!paramReceived) {

						//Send Preamble
						outputStream.write(new String("1234").getBytes());

						//Send parameter in binary (not ASCII) MSB first
						outputStream.write(writeData[paramNum] / 256);
						outputStream.write(writeData[paramNum] % 256);


						//long startTime = System.currentTimeMillis();
						int temp = 0;
						boolean paramEchoed = false;
						while(!paramEchoed) {

							//If the module echoed all 4 digits of the parameter, read and store them in the tempDigits array
							if (inputStream.available() >= 2) {
								temp = inputStream.read() * 256 + inputStream.read(); 
								paramEchoed = true;
							}	
						}

						//If module echoed correctly, send 'A' for Acknowledge
						if (temp == writeData[paramNum]) {
							outputStream.write(new String("CA").getBytes());
							paramReceived = true;
							attemptCounter = 0;
						}
						//If module echoed incorrectly, send 'N' for Not-Acknowledge
						else {
							outputStream.write(new String("CN").getBytes());
							attemptCounter++;
						}

						//After 3 failed attempts, exit and notify the user
						if (attemptCounter == 3) {
							generalStatusLabel.setText("Module not Echoing Properly, Check Connections");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(255,0,0));
							//Exit method, communication failed
							return false;
						}

						if (paramAbort) {
							return false;
						}
					}
				}

			}
			else if (paramAbort){
				generalStatusLabel.setText("Write Aborted");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(255,0,0));
				//Exit method, communication failed
				return false;
			}
			else {
				generalStatusLabel.setText("Data Not Sent, No Port Selected");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(255,0,0));
				//Exit method, communication failed
				return false;
			}
		}
		catch (IOException e) {                                          //If there is an IOException
			generalStatusLabel.setText("Data Not Sent, Error Communicating with Dongle");    //Notify the user that something broke
			Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, e);
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			//Exit method, communication failed
			return false;
		} 
		catch (NullPointerException e) {                                  //If there is a NullPointer
			generalStatusLabel.setText("Data Not Sent, No Port Selected");  //The serial port was not open; notifies the user about the mistake
			Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, e);
			progressBar.setValue(100);
			progressBar.setForeground(new Color(255,0,0));
			//Exit method, communication failed
			return false;
		}



		generalStatusLabel.setText("Module Configuration Successful, Parameters Have Been Updated");
		progressBar.setForeground(new Color(51, 204, 51));
		progressBar.setValue(100);
		return true;
	}



	public void readButtonHandler() {
		//stopAllThreads();
		
		Runnable readOperation = new Runnable() {
			public void run() {
				readDataButton.setEnabled(false);
				dataListener();
				readDataButton.setEnabled(true);
			}
		};

		readThread = new Thread(readOperation);
		readThread.start();
		
	}

	/**
	 * This method handles the transfer of data from the module for the purpose of storing it in a file for analyzing (Not for calibration)
	 */
	public boolean dataListener() {                                //serial Port listner. Checks for incomming data 
		if(!selectMode('E')) {
			return false;
		}
		if (dataStreamsInitialized && !readAbort) {
			try {
				configureForHandshake();

				generalStatusLabel.setText("Reading Data From Module");
				contentPanel.revalidate();
				configureForImport();
				
				boolean dataReceived = false;
				long importStartTime = System.currentTimeMillis();
				while ((System.currentTimeMillis() - importStartTime) < 15000) {
					if (inputStream.available() > 0) {      
						progressBar.setValue(0);                           //sets the progress bat to 0 percent

						ArrayList<Integer> testParameters = new ArrayList<Integer>();     
						ArrayList<Integer> testData = new ArrayList<Integer>();
						//ArrayList<ArrayList<Integer>> Tests = new ArrayList<ArrayList<Integer>>();

						//Check for test parameter preamble
						waitForPreamble(1,4);
						System.out.println("Start Found");

						//Determine number of tests to expect/ get test parameters
						expectedTestNum = -1;
						while(expectedTestNum == -1) {
							if(inputStream.available() > 0) {
								expectedTestNum = inputStream.read();
							}

						}

						generalStatusLabel.setText("Collecting Data for " + expectedTestNum + " Tests");//Tells the user how many tests are being transmitted
						//reads the parameters for the test that are sent from the URI module
						int paramNum = 0;
						while (paramNum < NUM_TEST_PARAMETERS) {
							if (inputStream.available() >= 2) {
								testParameters.add(paramNum, (int) ((inputStream.read() * 256) + (inputStream.read())));
								System.out.println(testParameters.get(paramNum));
								paramNum++;
							}
							if (readAbort) {
								return false;
							}
						}


						//each parameter is sent as two bytes. The higher byte is multiplied by 256 and the bottom byte is added on
						timedTestFlag = testParameters.get(5);
						testLength = testParameters.get(6);
						accelGyroSampleRate = testParameters.get(7);
						magSampleRate = testParameters.get(8);
						accelSensitivity = testParameters.get(9);
						gyroSensitivity = testParameters.get(10);
						accelFilter = testParameters.get(11);
						gyroFilter = testParameters.get(12);					


						//Populate dashboard with the parameters sent by the module
						testLengthTextFieldRead.setText(Integer.toString(testLength));            //Test Length
						numTestsTextFieldRead.setText(Integer.toString(expectedTestNum));
						accelGyroSampleRateTextFieldRead.setText(Integer.toString(accelGyroSampleRate)); //Accel Gyro Sample Rate
						magSampleRateTextFieldRead.setText(Integer.toString(magSampleRate));           //Mag Sample Rate
						accelSensitivityTextFieldRead.setText(Integer.toString(accelSensitivity));       //Accel Sensitivity
						gyroSensitivityTextFieldRead.setText(Integer.toString(gyroSensitivity));         //Gyro Sensitivity
						accelFilterTextFieldRead.setText(Integer.toString(accelFilter));           //Accel Filter
						gyroFilterTextFieldRead.setText(Integer.toString(gyroFilter));             //Gyro Filter 
						nameOfFile = fileNameTextField.getText();

						Date date = new Date();
						nameOfFile += (" " + accelGyroSampleRate + "-" + magSampleRate + " " + accelSensitivity + "G-" + accelFilter + " " + gyroSensitivity + "dps-" + gyroFilter + " MAG-N " + date.getDate() + getMonth(date.getMonth()) + (date.getYear() - 100));
						fileNameTextField.setText(nameOfFile);


						//Loops until it all of the tests are collected

						int testNum = 1;                                     //tracks the current index the test is on

						while (testNum <= expectedTestNum) {
							progressBar.setValue((int) ((100 *((double) testNum / (double) expectedTestNum))/ 1.25));      //Update the progress bar so the last test is no longer being displayed
							
							int temp = 0;
							testData = new ArrayList<Integer>();
							//Start Condition test, The program is expecting to receive "1-2-3-4-5-6-7-8" as the start condition
							waitForPreamble(1,8);

							boolean stopCondition = false;
							while (!stopCondition) {    //read all of the data on the serial buffer and store it in the test array
								//System.out.println(inputStream.available());
								if (inputStream.available() > 0) {
									for(int counter = 8; counter > 0;) {
										temp = inputStream.read();
										//System.out.println(temp);
										testData.add(temp);
										//System.out.println(Tests.get(testNum).get(i) + ".");

										//System.out.println(temp);
										if (temp == counter) {    
											counter--;
										} 
										else {
											counter = 8;
										}

									}
									if (testData.size() >= 8) {    //if the start condition was received correctly
										testData.set(testData.size() - 8, -1);
										stopCondition = true;                      //start condition flag is set to true so data collection will begin
										generalStatusLabel.setText("Found the Stop Condition For Test " + testNum + ".");    //display to the user where the program is
									}
								}
								if (readAbort) {
									return false;
								}
							}

							//System.out.println("EXIT");
							//ArrayList<Integer> finalData = new ArrayList<Integer>();
							int[] finalData = new int[testData.size()];                   //store all of the data from the single collected test in another array so it is final
							int j = 0;
							while(testData.get(j) != -1) {
								finalData[j] = testData.get(j);
								//System.out.println(finalData[j]);
								j++;

								if (readAbort) {
									return false;
								}
							}
							finalData[j] = -1;


							nameOfFile = prefixTextField.getText() + " (#" + (testNum) + ") " + fileNameTextField.getText() + " " + suffixTextField.getText() + ".CSV";  //Add a number and .CSV to the file name
							final int testID = testNum;		//Must be final to work in the sortData routine
							final int numTests = expectedTestNum;
							Runnable organizerOperation = new Runnable() {
								public void run() {
									organizer.sortData(finalData, testID, numTests, nameOfFile, (accelGyroSampleRate / magSampleRate), (1 / accelGyroSampleRate), false, false, fileOutputDirectoryStr);  //create the .CSV with neccessary parameters

								}
							};

							Thread organizerThread = new Thread(organizerOperation);
							organizerThread.start();      //start the new thread



							testNum++;              //The test number is incremented to collect data for the next test
						}
					}
				}
				if(!dataReceived) {
					generalStatusLabel.setText("Data Transfer Complete");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(51, 204, 51));
					return true;
				}
				else {
					generalStatusLabel.setText("Timeout");
					return false;
				}
				
			}
			catch (IOException e){
				generalStatusLabel.setText("Comm Port Error! Try Again");
				return false;
			}
			catch (PureJavaIllegalStateException e) {
				generalStatusLabel.setText("Error, Try Again");
				return false;
			}


		}
		return false;

	}

	public void initDataFields() {
		if (mainTabbedPanel.getSelectedIndex() == 1) {
			//Checkboxes
			timedTestCheckbox.setSelected(true);
			delayAfterStartCheckbox.setSelected(false);
			manualCalibrationCheckbox.setSelected(false);

			//Text Fields

			testLengthTextField.setText("25");
			accelGyroSampleRateTextField.setText("960");
			magSampleRateTextField.setText("96");
			delayAfterStartTextField.setText("0");
			timer0TickThreshTextField.setText("0");


			//Comboboxes
			accelSensitivityCombobox.setModel(new DefaultComboBoxModel(new String [] {"2", "4", "8", "16"}));
			gyroSensitivityCombobox.setModel(new DefaultComboBoxModel(new String [] {"250", "500", "1000", "2000"}));
			accelFilterCombobox.setModel(new DefaultComboBoxModel(new String [] {"5", "10", "20", "41", "92", "184", "460", "1130 (OFF)"}));
			gyroFilterCombobox.setModel(new DefaultComboBoxModel(new String [] {"10", "20", "41", "92", "184", "250", "3600", "8800 (OFF)"}));

			//Set Default Selection for Comboboxes
			accelSensitivityCombobox.setSelectedIndex(3);	//16g
			gyroSensitivityCombobox.setSelectedIndex(3);	//2000dps
			accelFilterCombobox.setSelectedIndex(4);		//92Hz
			gyroFilterCombobox.setSelectedIndex(3);			//92Hz
		}

	}

	/**
	 * Updates the data fields on whichever tab is currently selected if applicable
	 */
	public void updateDataFields() {
		if (frameInitialized && mainTabbedPanel.getSelectedIndex() == 1) {
			//Timed Test Checkbox (Allows user to swap between timed and untimed test)
			if (timedTestCheckbox.isSelected()) {
				testLengthTextField.setEditable(true);
				testLengthTextField.setEnabled(true);
			}
			else {
				testLengthTextField.setEditable(false);
				testLengthTextField.setEnabled(false);
			}

			//Delay After Start Checkbox (Allows Editing of Timer0 Tick Threshold)
			if (delayAfterStartCheckbox.isSelected()) {
				delayAfterStartTextField.setEditable(true);
				delayAfterStartTextField.setEnabled(true);
			}
			else {
				delayAfterStartTextField.setEditable(false);
				delayAfterStartTextField.setEnabled(false);
			}

			//Manual Calibration Checkbox (Allows Editing of Timer0 Tick Threshold)
			if (manualCalibrationCheckbox.isSelected()) {
				timer0TickThreshTextField.setEditable(true);
				timer0TickThreshTextField.setEnabled(true);
			}
			else {
				timer0TickThreshTextField.setEditable(false);
				timer0TickThreshTextField.setEnabled(false);
			}
		}
	}
	public void browseButtonHandler() {
		JFileChooser chooser;
		chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			fileOutputDirectoryStr = chooser.getSelectedFile().toString();
		}
		else {
			fileOutputDirectoryStr = null;
		}
	}

	public void updateProgress(int progress) {   //Method that updates the progress with the percentage that has been completed so far in making the .CSV file
		//progressBar.setValue(progress);
	}

	public void setWriteStatusLabel(String label) {
		generalStatusLabel.setText(label);        //Tell the user a new .CSV has been created.
	}

	public boolean getOrganizeAbortFlag() {
		return organizeAbort;
	}

	public int getTickThreshold(int accelGyroSampleRate) {
		switch (accelGyroSampleRate) {
		case(60):			//60Hz
			return 33173;
		case(120):
			return 33021;
		case (240):
			return 16343;
		case (480):
			return 8021;
		case (500):
			return 7679;
		case (960):
			return 3689;
		default:	//960-96
			return 3848;
		}
	}
	public String getMonth(int month) {  //Method for changing the data in int form to a string
		switch (month) {
		case (0):
			return "JAN";
		case (1):
			return "FEB";
		case (2):
			return "MAR";
		case (3):
			return "APR";
		case (4):
			return "MAY";
		case (5):
			return "JUN";
		case (6):
			return "JUL";
		case (7):
			return "AUG";
		case (8):
			return "SEP";
		case (9):
			return "OCT";
		case (10):
			return "NOV";
		case (11):
			return "DEC";
		}
		return "NOP";
	}

	public boolean getPortInitializedFlag() {
		return portInitialized;
	}

	public void setPortInitializedFlag(boolean flag) {
		portInitialized = flag;
	}

	public boolean getReadModeFlag() {
		return readMode;
	}



	/**
	 * Create the frame.
	 */
	public void createComponents() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 550, 665);
		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPanel);
		contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));


		serialPortPanel = new JPanel();
		serialPortPanel.setPreferredSize(new Dimension(500, 150));
		contentPanel.add(serialPortPanel);
		serialPortPanel.setLayout(new GridLayout(0,1, 0, 0));

		JPanel commPortPanel = new JPanel();
		serialPortPanel.add(commPortPanel);

		refreshPortButton = new JButton("Refresh Port List");
		refreshPortButton.setBorder(null);
		refreshPortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findPorts();
			}
		});

		commPortCombobox = new JComboBox();
		commPortCombobox.setEnabled(false);
		commPortPanel.setLayout(new GridLayout(1, 3, 0, 0));
		commPortPanel.add(refreshPortButton);
		commPortPanel.add(commPortCombobox);

		commPortCombobox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				portSelectedHandler();
			}
		});

		disconnectButton = new JButton("Disconnect");
		disconnectButton.setBorder(null);
		disconnectButton.setEnabled(false);
		disconnectButton.setForeground(Color.BLACK);
		commPortPanel.add(disconnectButton);
		disconnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeSerialPort();
			}
		});

		getModuleIDPanel = new JPanel();
		serialPortPanel.add(getModuleIDPanel);
		getModuleIDPanel.setLayout(new GridLayout(0, 1, 0, 0));

		getModuleIDButton = new JButton("Get Module Information");
		getModuleIDButton.setEnabled(false);
		getModuleIDPanel.add(getModuleIDButton);

		getModuleIDButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getModuleInfoButtonHandler();
			}
		});

		JPanel serialNumberPanel = new JPanel();
		serialNumberPanel.setBorder(null);
		serialPortPanel.add(serialNumberPanel);

		moduleSerialNumberLabel = new JLabel("Module Serial Number:");
		moduleSerialNumberLabel.setBorder(null);
		serialNumberPanel.add(moduleSerialNumberLabel);

		JPanel moduleInfoPanel = new JPanel();
		serialPortPanel.add(moduleInfoPanel);
		moduleInfoPanel.setLayout(new GridLayout(0, 2, 0, 0));

		hardwareIDLabel = new JLabel("Module Hardware ID:");
		hardwareIDLabel.setBorder(null);
		moduleInfoPanel.add(hardwareIDLabel);

		firmwareIDLabel = new JLabel("Module Firmware ID:");
		firmwareIDLabel.setBorder(null);
		moduleInfoPanel.add(firmwareIDLabel);

		generalStatusLabel = new JLabel("Please Select Port to Begin");
		generalStatusLabel.setForeground(Color.BLUE);
		generalStatusLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		generalStatusLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
		generalStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		serialPortPanel.add(generalStatusLabel);

		JPanel mainPanelContainer = new JPanel();
		contentPanel.add(mainPanelContainer);
		mainPanelContainer.setLayout(new GridLayout(0, 1, 0, 0));

		mainTabbedPanel = new JTabbedPane(JTabbedPane.TOP);
		mainTabbedPanel.setPreferredSize(new Dimension(500, 400));
		mainPanelContainer.add(mainTabbedPanel);

		mainTabbedPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				initDataFields();
			}
		});


		JPanel readPanel = new JPanel();
		readPanel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		mainTabbedPanel.addTab("Read Mode", null, readPanel, null);
		readPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		fileNamePanel = new JPanel();
		fileNamePanel.setPreferredSize(new Dimension(500, 150));
		readPanel.add(fileNamePanel);
		fileNamePanel.setLayout(new GridLayout(0, 1, 0, 0));

		startReadButtonPanel = new JPanel();
		fileNamePanel.add(startReadButtonPanel);
		startReadButtonPanel.setLayout(new GridLayout(0, 1, 0, 0));

		readDataButton = new JButton("Read Data from Module");
		readDataButton.setEnabled(false);
		startReadButtonPanel.add(readDataButton);
		readDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				readButtonHandler();
			}
		});

		fileNameModifierPanel = new JPanel();
		fileNamePanel.add(fileNameModifierPanel);
		fileNameModifierPanel.setLayout(new GridLayout(1, 0, 0, 0));

		prefixTextField = new JTextField();
		prefixTextField.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "File Name Prefix", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		fileNameModifierPanel.add(prefixTextField);
		prefixTextField.setColumns(10);

		suffixTextField = new JTextField();
		suffixTextField.setBorder(new TitledBorder(null, "File Name Suffix", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		fileNameModifierPanel.add(suffixTextField);
		suffixTextField.setColumns(10);

		fileLocationPanel = new JPanel();
		fileNamePanel.add(fileLocationPanel);
		fileLocationPanel.setLayout(new BoxLayout(fileLocationPanel, BoxLayout.X_AXIS));

		fileNameTextField = new JTextField();
		fileNameTextField.setMaximumSize(new Dimension(350, 50));
		fileNameTextField.setBorder(new TitledBorder(null, "File Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		fileLocationPanel.add(fileNameTextField);
		fileNameTextField.setColumns(10);

		browseButton = new JButton("Browse");
		browseButton.setMaximumSize(new Dimension(150, 50));
		browseButton.setPreferredSize(new Dimension(81, 35));
		fileLocationPanel.add(browseButton);

		browseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				browseButtonHandler();
			}
		});

		paramPanel = new JPanel();
		paramPanel.setPreferredSize(new Dimension(500, 200));
		readPanel.add(paramPanel);
		paramPanel.setLayout(new GridLayout(0, 2, 0, 0));

		numTestsTextFieldRead = new JTextField();
		numTestsTextFieldRead.setEditable(false);
		numTestsTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Number Of Tests", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(numTestsTextFieldRead);
		numTestsTextFieldRead.setColumns(10);

		testLengthTextFieldRead = new JTextField();
		testLengthTextFieldRead.setEditable(false);
		testLengthTextFieldRead.setColumns(10);
		testLengthTextFieldRead.setBorder(new TitledBorder(null, "Test Length", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		paramPanel.add(testLengthTextFieldRead);

		accelGyroSampleRateTextFieldRead = new JTextField();
		accelGyroSampleRateTextFieldRead.setEditable(false);
		accelGyroSampleRateTextFieldRead.setColumns(10);
		accelGyroSampleRateTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accel/Gyro Sample Rate", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(accelGyroSampleRateTextFieldRead);

		magSampleRateTextFieldRead = new JTextField();
		magSampleRateTextFieldRead.setEditable(false);
		magSampleRateTextFieldRead.setColumns(10);
		magSampleRateTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Mag Sample Rate", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(magSampleRateTextFieldRead);

		accelSensitivityTextFieldRead = new JTextField();
		accelSensitivityTextFieldRead.setEditable(false);
		accelSensitivityTextFieldRead.setColumns(10);
		accelSensitivityTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accelerometer Sensitivity (G)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(accelSensitivityTextFieldRead);

		gyroSensitivityTextFieldRead = new JTextField();
		gyroSensitivityTextFieldRead.setEditable(false);
		gyroSensitivityTextFieldRead.setColumns(10);
		gyroSensitivityTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gyroscope Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(gyroSensitivityTextFieldRead);

		accelFilterTextFieldRead = new JTextField();
		accelFilterTextFieldRead.setEditable(false);
		accelFilterTextFieldRead.setColumns(10);
		accelFilterTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accelerometer Filter (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(accelFilterTextFieldRead);

		gyroFilterTextFieldRead = new JTextField();
		gyroFilterTextFieldRead.setEditable(false);
		gyroFilterTextFieldRead.setColumns(10);
		gyroFilterTextFieldRead.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gyroscope Filter (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		paramPanel.add(gyroFilterTextFieldRead);

		JPanel configurationPanel = new JPanel();
		configurationPanel.setPreferredSize(new Dimension(500, 1000));
		configurationPanel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		mainTabbedPanel.addTab("Configurations", null, configurationPanel, null);
		configurationPanel.setLayout(new GridLayout(0, 2, 0, 0));

		timedTestCheckbox = new JCheckBox("Timed Test");
		timedTestCheckbox.setSelected(true);
		timedTestCheckbox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(timedTestCheckbox);

		timedTestCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateDataFields();
			}
		});

		delayAfterStartCheckbox = new JCheckBox("Delay After Start");
		delayAfterStartCheckbox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(delayAfterStartCheckbox);

		delayAfterStartCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateDataFields();
			}
		});

		manualCalibrationCheckbox = new JCheckBox("Manual Calibration");
		manualCalibrationCheckbox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(manualCalibrationCheckbox);

		manualCalibrationCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateDataFields();
			}
		});

		getCurrentConfigsButton = new JButton("Get Current Configurations");
		getCurrentConfigsButton.setEnabled(false);
		getCurrentConfigsButton.setBorder(null);
		getCurrentConfigsButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(getCurrentConfigsButton);

		accelGyroSampleRateTextField = new JTextField();
		accelGyroSampleRateTextField.setText("960");
		accelGyroSampleRateTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelGyroSampleRateTextField.setColumns(10);
		accelGyroSampleRateTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accel/Gyro Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(accelGyroSampleRateTextField);

		magSampleRateTextField = new JTextField();
		magSampleRateTextField.setText("96");
		magSampleRateTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		magSampleRateTextField.setColumns(10);
		magSampleRateTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Magnetometer Sample Rate (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(magSampleRateTextField);

		accelSensitivityCombobox = new JComboBox();
		accelSensitivityCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelSensitivityCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accelerometer Sensitivity (G)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(accelSensitivityCombobox);

		gyroSensitivityCombobox = new JComboBox();
		gyroSensitivityCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		gyroSensitivityCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gyroscope Sensitivity (dps)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(gyroSensitivityCombobox);

		accelFilterCombobox = new JComboBox();
		accelFilterCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		accelFilterCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Accelerometer Filter (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(accelFilterCombobox);

		gyroFilterCombobox = new JComboBox();
		gyroFilterCombobox.setFont(new Font("Tahoma", Font.PLAIN, 13));
		gyroFilterCombobox.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gyroscope Filter (Hz)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(gyroFilterCombobox);

		timer0TickThreshTextField = new JTextField();
		timer0TickThreshTextField.setText("3689");
		timer0TickThreshTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		timer0TickThreshTextField.setEditable(false);
		timer0TickThreshTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Timer0 Tick Threshold", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(timer0TickThreshTextField);
		timer0TickThreshTextField.setColumns(10);

		delayAfterStartTextField = new JTextField();
		delayAfterStartTextField.setText("0");
		delayAfterStartTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		delayAfterStartTextField.setEditable(false);
		delayAfterStartTextField.setColumns(10);
		delayAfterStartTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Delay After Start (Microseconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(delayAfterStartTextField);

		testLengthTextField = new JTextField();
		testLengthTextField.setText("25");
		testLengthTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		testLengthTextField.setColumns(10);
		testLengthTextField.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED, null, null), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Test Duration (Seconds)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		configurationPanel.add(testLengthTextField);

		writeConfigsButton = new JButton("Write Configurations");
		writeConfigsButton.setEnabled(false);
		writeConfigsButton.setBorder(null);
		writeConfigsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				writeButtonHandler();
			}
		});
		writeConfigsButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
		configurationPanel.add(writeConfigsButton);
		
		erasePanel = new JPanel();
		mainTabbedPanel.addTab("Erase", null, erasePanel, null);
		erasePanel.setLayout(new GridLayout(1, 0, 0, 0));
		
		panel = new JPanel();
		erasePanel.add(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		bulkEraseButton = new JButton("Bulk Erase");
		panel.add(bulkEraseButton);
		
		sectorEraseButton = new JButton("Sector Erase");
		panel.add(sectorEraseButton);

		JPanel calibrationPanel = new JPanel();
		mainTabbedPanel.addTab("Calibration", null, calibrationPanel, null);

		JPanel progressPanel = new JPanel();
		contentPanel.add(progressPanel);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(500, 20));
		progressPanel.add(progressBar);

		JPanel copyrightPanel = new JPanel();
		contentPanel.add(copyrightPanel);
		copyrightPanel.setLayout(new GridLayout(1, 0, 0, 0));

		JLabel copyrightLabel = new JLabel("Copyright nth Solutions LLC. 2018");
		copyrightPanel.add(copyrightLabel);


		frameInitialized = true;
	}

}
