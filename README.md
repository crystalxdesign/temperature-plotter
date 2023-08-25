# Temperature Plotter

This project was designed as a cheap and simple means of logging accurate temperature measurements and graphical displays for school science or home learning. I provide two clients which read and record the data. One is written in Java and is intended to run on a Windows or Linux PC; the other is an Android phone app. Both clients connect to the same custom hardware via Bluetooth classic. 

## Temperature probe

This custom hardware is very simple, and is based around two DS1820 temperature probes, an ESP32 with USB cable and a 4.7k resistor. The ESP32 provides all the control, connectivity and power regulation required. The USB cable is required for the 5V supply and also provides a direct serial data connection. If the probe is to be remote from the client (not powered from a USB port of a PC) then a 5V battery pack will need to be connected to the USB cable. The software for the ESP32 is to be found as [ESP32_DualThermoPlotterServer.ino](https://github.com/crystalxdesign/temperature-plotter/blob/master/ESP32_DualThermoPlotterServer/ESP32_DualThermoPlotterServer.ino) and requires the Arduino IDE to program it with the approriate libraries (BluetoothSerial, OneWire and DallasTemperature) installed. Before the Dual Temperature Plotter can be used for the first time the Temperature probe must be powered up and the PC or Android device paired to the Bluetooth server of the Temperature probe. You will see the Bluetooth server listed as "Temperature probe" when it boots.  
![](https://github.com/crystalxdesign/temperature-plotter/blob/master/ESP32TemperatureProbe.jpg)

## Java Client

This provides the most complete set of functionalities of the two clients. It depends on jfreechart (from jfree) to display the temperature data, and jSerialComm (from fazecast) to provide the serial connection over the USB cable or via Bluetooth. The code is Java and is supplied as an Eclipse Maven project. The two DS1820 probes each provide a trace on the graph, distinguished by blue and red. Before starting, the appropriate com port needs to be selected from the "Module Serial Port" drop-down list. Both Bluetooth and USB Serial connections show in the same fashion. If the manual option is chosen then once the "Start" button is clicked the sampling will continue until the "Stop" button is clicked. With the automatic mode you can select up to 1000 samples which will continue at the rate selected (once per second maximum) until complete.

![](https://github.com/crystalxdesign/temperature-plotter/blob/master/Animation.gif)

Captured traces can be saved by clicking the "Save Graph" button and giving it a file name. Traces are saved in CSV format and can be viewed later by use of the "Load Graph" button. You can add a graph title to describe the trace which is also retained in the saved file. The example directory contains two traces I have made which can be loaded and viewed. Right-clicking on the graph area also allows you to
>**Save** the trace as a PNG file by selecting "Save as>PNG"  
>**Copy** the trace image to the PC clipboard by selecting "Copy"  
>**Print** the trace image by selecting "Print"  

Below is an example of a printed trace from one of thje examples:
![](https://github.com/crystalxdesign/temperature-plotter/blob/master/print.png)

## Android Client

This provides most of the key functionality of the Java Client but only saves one trace at a time, to save another means to overwrite the first. The MIT App Inventor (https://ai2.appinventor.mit.edu/) is used to build the client and the code is all in the android_client_app directory.
