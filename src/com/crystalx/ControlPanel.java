package com.crystalx;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.fazecast.jSerialComm.SerialPort;

public class ControlPanel extends JFrame implements ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JCheckBox chk_red = new JCheckBox("Red trace");
	private static JCheckBox chk_blue = new JCheckBox("Blue trace");
	private static JCheckBox chk_auto_range = new JCheckBox("Auto Range");
	private static ControlPanel controlPanel = new ControlPanel();
	private static JComboBox<String> cbPorts = new JComboBox<String>();
	private static JComboBox<String> cbTotalSamples = new JComboBox<String>();
	private static JComboBox<String> cbSamplingRate = new JComboBox<String>();
	private static JComboBox<String> cbTimeUnits = new JComboBox<String>();
	private static JButton btnStart = new JButton("Start");
	private static JButton btnStop = new JButton("Stop");
	private static JButton btnChange = new JButton("Change");
	private static JButton btnLoadGraph = new JButton("Load Graph");
	private static JButton btnSaveGraph = new JButton("Save Graph");
	private static JButton btnSingleSample = new JButton("Single Sample");
	private static JPanel panel;
	private static JTextField txtPlotTitle = new JTextField();
	private static JLabel lblPlotTitle = new JLabel("Graph Title");
	private static JLabel lblComm = new JLabel("Module serial port");
	private static JLabel lblSamples = new JLabel("Total samples");
	private static JLabel lblRate = new JLabel("Sample Rate");
	private static JLabel lblPer = new JLabel("per");
	private static SerialPort serialPort;
	private static JTextArea txtMessage = new JTextArea();
	private static JFileChooser dataSave;
	private static JFileChooser dataLoad;
	private static JProgressBar progressBar;
	
	private static int BAUD_RATE = 115200;
	private static boolean isSampling = false;
	private static TemperatureSampler sampler;
	private static XYSeriesCollection dataset;
	private static XYSeries thermometer_RED;
	private static XYSeries thermometer_BLUE;
	private static double currentTime = 0.0;
	private static double samplePeriod = 0.0;
	private static long now;
	private static int SAMPLING_LATENCY = 550;
	private static String DELIMITER = ",";

	ControlPanel(){
		dataset = new XYSeriesCollection();
		thermometer_RED = new XYSeries("Red");
		thermometer_BLUE = new XYSeries("Blue");
		dataset.addSeries(thermometer_RED);
	    dataset.addSeries(thermometer_BLUE);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
        // if the state combobox is changed
        if (e.getSource() == cbPorts && e.getStateChange() == ItemEvent.SELECTED) {
        	try {
				if(openPort()) {
					//serialPort.writeBytes(INIT_STRING.getBytes(), INIT_STRING.length());
					setText("Module is connected");
				} else {
					setText("Module failed to connect");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
        	return;
        }
    }
	
	private void initSerialPort() throws Exception {
	  if (serialPort != null && serialPort.isOpen()) {
	    closePort();
	  }
	  serialPort = SerialPort.getCommPort((String)cbPorts.getSelectedItem());
	  serialPort.setParity(SerialPort.NO_PARITY);
	  serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
	  serialPort.setNumDataBits(8);
	  serialPort.addDataListener(new MessageListener());
	  serialPort.setBaudRate(BAUD_RATE);
	}
	
	public boolean openPort() throws Exception {
	  initSerialPort();
	  if (serialPort == null) {
	    throw new Exception("The connection wasn't initialized");
	  }
	  return serialPort.openPort();
	}
	
	public void closePort() throws Exception {
	  if (serialPort != null) {
	    serialPort.removeDataListener();
	    serialPort.closePort();
	  }
	}
	
	public static boolean isSampling() {
		return isSampling;
	}
	
	public static void setText(String txt) {
		txtMessage.append("\n"+txt);
	}
	
	private static void setStop() {
		btnSingleSample.setEnabled(true);
		btnStart.setEnabled(true);
		btnStop.setEnabled(false);
		isSampling = false;
	}
	
	private static void setStart() {
		btnSingleSample.setEnabled(false);
		btnStart.setEnabled(false);
		btnStop.setEnabled(true);
		isSampling = true;
		thermometer_RED.clear();
		thermometer_BLUE.clear();
		progressBar.setValue(0);
		txtMessage.setText("Sampling ...");
	}
	
	public static XYSeriesCollection getData() {
		return dataset;
	}
	
	public static void setData(double dataRed, double dataBlue) {
		sampler.interrupt();
		if(samplePeriod == 0.0) {
			setText("Red Probe: "+dataRed+"C, Blue Probe: "+dataBlue+"C\n");
		} else {
			thermometer_RED.add(currentTime, dataRed);
			thermometer_BLUE.add(currentTime, dataBlue);
			currentTime = currentTime + samplePeriod;
		}
	}

	private static void processsSampling() {
		int noOfSamples = cbTotalSamples.getSelectedIndex() * 10;
		int index = cbSamplingRate.getSelectedIndex() - 1;
		double sampleRate;
		if(index == 0)
			sampleRate = 1.0;
		else
			sampleRate = (double)index * 10.0;
		double multiplier = 0.0;
		switch(cbTimeUnits.getSelectedIndex()) {
		//case 1: multiplier=1.0; break;
		case 1: multiplier=60.0; break;
		case 2: multiplier=3600.0; break;
		}
		samplePeriod = multiplier/sampleRate;
		sampler = new TemperatureSampler();
		sampler.init(noOfSamples);
		sampler.start();
	}
	
	private static void singleSample() {
		isSampling = true;
		samplePeriod = 0.0;
		sampler = new TemperatureSampler();
		sampler.init(1);
		sampler.start();
	}
	
	public static JPanel build() {
		panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
				
		List<String> portcodes = Arrays.stream(SerialPort.getCommPorts())
	      .map(SerialPort::getSystemPortName)
	      .collect(Collectors.toList());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		dataLoad = new JFileChooser();
		dataLoad.setDialogTitle("Choose data file");
		dataLoad.setFileSelectionMode(JFileChooser.FILES_ONLY);
		btnLoadGraph.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				int returnVal = dataLoad.showOpenDialog(btnLoadGraph);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = dataLoad.getSelectedFile();
	    	        BufferedReader reader;
					try {
						reader = new BufferedReader(new FileReader(file));
						String currentLine, lineNo="0";
						thermometer_RED.clear();
						thermometer_BLUE.clear();
						currentLine = reader.readLine();
						if(currentLine != null) TemperaturePlotter.setGraphTitle(currentLine);
				        while ((currentLine = reader.readLine()) != null) {
				        	String[] data = currentLine.split(DELIMITER);
				        	double time = Double.parseDouble(data[1]);
				        	double red = Double.parseDouble(data[2]);
				        	double blue = Double.parseDouble(data[3]);
				        	if(red != -127.0) {
				        		thermometer_RED.add(new XYDataItem(time, red));
				        	}
				        	
				        	if(blue != -127.0) {
				        		thermometer_BLUE.add(new XYDataItem(time, blue));
				        	}
				        	lineNo = data[0];
				        }
		    	        reader.close();
		    	        setText(lineNo+" data points Loaded.");
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}  
		});  

		dataSave = new JFileChooser();
		dataSave.setDialogTitle("Choose data file");
		dataSave.setFileSelectionMode(JFileChooser.FILES_ONLY);
		btnSaveGraph.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				int returnVal = dataSave.showOpenDialog(btnSaveGraph);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = dataSave.getSelectedFile();
			        try {
			        	BufferedWriter output = new BufferedWriter(new FileWriter(file));
			        	output.write(TemperaturePlotter.getGraphTitle()+"\n");
						for(int x = 0; x<thermometer_RED.getItemCount(); x++) {
				        	StringBuilder sb = new StringBuilder();
							sb.append(x+1);
							sb.append(DELIMITER);
							sb.append(thermometer_RED.getX(x));
							sb.append(DELIMITER);
							sb.append(thermometer_RED.getY(x));
							sb.append(DELIMITER);
							sb.append(thermometer_BLUE.getY(x));
							sb.append("\n");
							output.write(sb.toString());
						}
						output.close();
						setText("Data Saved.");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}  
		});  

		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 0;   
        panel.add(btnLoadGraph, gbc);
		gbc.gridx = 1;
        panel.add(btnSaveGraph, gbc);
        
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 1;     
        panel.add(lblPlotTitle, gbc);
		gbc.gridx = 3;
		panel.add(btnChange, gbc);
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		panel.add(txtPlotTitle, gbc);
		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 2;     
        panel.add(lblComm, gbc);

		portcodes.add(0, "Select a serial port");
		cbPorts.setModel(new DefaultComboBoxModel(portcodes.toArray()));
		cbPorts.addItemListener(controlPanel);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 1;
		gbc.gridy = 2;  
		gbc.gridwidth = 3;
        panel.add(cbPorts, gbc);
        
        List<String> totalSamples = new ArrayList<String>();
        totalSamples.add("Manual");
        for(int x=10;x<=1000;x=x+10) {
        	totalSamples.add(Integer.toString(x));
        }
        cbTotalSamples.setModel(new DefaultComboBoxModel(totalSamples.toArray()));
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblSamples, gbc);
        gbc.gridx = 1;
        panel.add(cbTotalSamples, gbc);
        
        List<String> samplingRate = new ArrayList<String>();
        samplingRate.add("----");
        samplingRate.add("1");
        for(int x=10;x<=100;x=x+10) {
        	samplingRate.add(Integer.toString(x));
        }
        cbSamplingRate.setModel(new DefaultComboBoxModel(samplingRate.toArray()));
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(lblRate, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(cbSamplingRate, gbc);
        //gbc.gridx = 2;
        gbc.insets = new Insets(10, 70, 10, 10);
        panel.add(lblPer, gbc);
        cbTimeUnits.setModel(new DefaultComboBoxModel(new String[] {"-------", "Minute", "Hour"}));
        gbc.insets = new Insets(10, 100, 10, 10);
        panel.add(cbTimeUnits, gbc);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(chk_blue, gbc);
        gbc.gridx = 1;
        panel.add(chk_red, gbc);
        gbc.gridx = 2;
        panel.add(chk_auto_range, gbc);
        
        gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 50, 10, 10);
		gbc.gridx = 0;
        gbc.gridy = 6;    
        gbc.gridwidth = 2;
        panel.add(btnStart, gbc);
        gbc.gridx = 1;
        panel.add(btnStop, gbc);
        gbc.gridx = 2;
        panel.add(btnSingleSample, gbc);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
        gbc.gridy = 7; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 5;
		panel.add(progressBar, gbc);

		txtMessage.setColumns(25);
		txtMessage.setEditable(false);
		txtMessage.setLineWrap(true);
		txtMessage.setText("Welcome to the "+TemperaturePlotter.TOOLTITLE);
		txtMessage.setRows(10);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
        gbc.gridy = 8;    
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;

		panel.add(txtMessage, gbc);
		
		btnStart.setToolTipText("Starts a sampling session, which continues until the stop button is pressed, or the total samples are taken");
		btnStart.setEnabled(true);
		btnStart.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				if(cbSamplingRate.getSelectedIndex() == 0 || cbTimeUnits.getSelectedIndex() == 0) {
					setText("Ensure that you have set a sampling rate and time units set before running.");
					return;
				}
				setStart();
				processsSampling();
			}  
		});  

		btnStop.setToolTipText("Stops any sampling session, but is required to stop a manual session");
		btnStop.setEnabled(false);
		btnStop.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				setStop();
				sampler.interrupt();
			}  
		});  

		btnChange.setToolTipText("Set the Graph Title");
		btnChange.setEnabled(true);
		btnChange.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				TemperaturePlotter.setGraphTitle(txtPlotTitle.getText());
			}
		});
		
		btnSingleSample.setToolTipText("Read the temperature probes once.");
		btnSingleSample.setEnabled(true);
		btnSingleSample.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				singleSample();
			}
		});
		
		chk_blue.setToolTipText("Hide/show the blue trace");
		chk_blue.setSelected(true);
		chk_blue.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				if(chk_blue.isSelected()) {
					TemperaturePlotter.setSeriesVisibility(0, true);
					//dataset.addSeries(thermometer_BLUE);
				} else {
	        		if(!chk_red.isSelected()) {
	        			chk_blue.setSelected(true);
	        			return;
	        		}
	        		TemperaturePlotter.setSeriesVisibility(0, false);
	        		//dataset.removeSeries(thermometer_BLUE);
				}
			}
		});

		chk_red.setToolTipText("Hide/show the red trace");
		chk_red.setSelected(true);
		chk_red.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				if(chk_red.isSelected()) {
					TemperaturePlotter.setSeriesVisibility(1, true);
				} else {
	        		if(!chk_blue.isSelected()) {
	        			chk_red.setSelected(true);
	        			return;
	        		}
	        		TemperaturePlotter.setSeriesVisibility(1, false);
				}
			}
		});
		
		chk_auto_range.setToolTipText("Automatic temperature range");
		chk_auto_range.setSelected(false);
		chk_auto_range.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				if(chk_auto_range.isSelected()) {
					TemperaturePlotter.setRangeManual(dataset.getRangeLowerBound(false), dataset.getRangeUpperBound(false));
				} else {
					TemperaturePlotter.setRangeManual(0.0, Math.ceil(dataset.getRangeUpperBound(false)));
				}
			}
		});
		return panel;
	}
	
	private static void readThermometers() {
		serialPort.writeBytes("@".getBytes(), 1);
	}

	private static void startTimer() {
		now = (new Date()).getTime();
	}

	private static void stopTimer() {
		long _now = (new Date()).getTime();
		setText((_now - now)+"ms");
	}

	private static class TemperatureSampler extends Thread {
		int _noOfSamples = 0;
		int counter = 0;
		
		public void run() {
			if(cbPorts.getSelectedIndex() == 0) {
				setText("WARNING: you must select a valid\nserial port to connect.");
				setStop();
				return;
			}
			counter = _noOfSamples;
			int sleepPeriod = (int)(samplePeriod*1000.0);
			if(_noOfSamples != 1) {
				sleepPeriod = sleepPeriod - SAMPLING_LATENCY;
				currentTime = 0.0;
				setText("Sampling period is "+samplePeriod+" seconds");
				
				if(samplePeriod < 1.0) {
					setText("WARNING: Sample rate is too high.\nTry to sample less than once per second.");
					setStop();
					return;
				}
			}

			while(isSampling) {
				try {
					try {
						readThermometers();
						Thread.sleep(1000);
						// Should only get here if failed to be interrupted
						// by sample data returned from the module
						setText("WARNING: Serial timeout. Check the module.");
						setStop();
						Thread.currentThread().interrupt();
						} catch (InterruptedException e) {
						  if(_noOfSamples > 0) {
							  double progress;
							  if(--counter == 0) {
								  progress = 100.0;
								  setStop();
							  } else {
								  progress = (1.0-((double)counter/(double)_noOfSamples)) * 100.0;
							  }
							  progressBar.setValue((int)progress);
						  }
						}
					Thread.sleep(sleepPeriod);
				} catch (InterruptedException e) {
				  Thread.currentThread().interrupt();
				}
			}
		}
		
		public void init(int noOfSamples) {
			_noOfSamples = noOfSamples;
		}
	}
}
