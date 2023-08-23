package com.crystalx;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public class MessageListener implements SerialPortMessageListener
{
   @Override
   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }

   @Override
   public byte[] getMessageDelimiter() { return new byte[] { (byte)'\r'}; }

   @Override
   public boolean delimiterIndicatesEndOfMessage() { return true; }

   @Override
   public void serialEvent(SerialPortEvent event)
   {
	    byte[] data = event.getReceivedData();
	    String msg = new String(data).trim();
	    if(ControlPanel.isSampling()) {
	    	String[] sample = msg.split("@");
		    double dataRed = Double.parseDouble(sample[0]);
		    double dataBlue = Double.parseDouble(sample[1]);
		    ControlPanel.setData(dataRed, dataBlue);
		    //ControlPanel.setText("dataA: " + dataA + " dataB: " + dataB + " msg: " + msg);
	    } else {
		    ControlPanel.setText(msg);
	    }
   }
}
