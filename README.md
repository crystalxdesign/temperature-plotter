# Temperature Plotter

This project was designed as a cheap and simple means of logging accurate temperature measurements with graphical display for use by school science classes or home learning. The project is free to use but please let me know how you use it. I provide two clients which read and record the data. One is written in Java and is intended to run on a Windows or Linux PC; the other is an Android phone app. Both clients connect to the same custom hardware via Bluetooth classic. 

## Temperature probe

- 1 x [ESP32 module](https://www.amazon.co.uk/AZDelivery-NodeMcu-CP2102-Development-including/dp/B071P98VTG/ref=sr_1_1_sspa?keywords=esp32&qid=1692983560&sr=8-1-spons&sp_csd=d2lkZ2V0TmFtZT1zcF9hdGY&th=1)
- 2 x [DS1820 temperature probes](https://thepihut.com/products/waterproof-ds18b20-digital-temperature-sensor-extras)
- 1 x 4.7k ohm resistor
- 1 x USB lead (compatible with ESP32 module)

This custom hardware is very simple, and is based around two DS1820 temperature probes, an ESP32 with USB cable and a 4.7k resistor. The ESP32 provides all the control, connectivity and power regulation required. The USB cable is required for the 5V supply and also provides a direct serial data connection. If the probe is to be remote from the client (not powered from a USB port of a PC) then a 5V battery pack will need to be connected to the USB cable. The software for the ESP32 module can be found [here](https://github.com/crystalxdesign/temperature-plotter/blob/master/Probe_Sketches/ESP32_DualThermoPlotterServer.ino) and requires the [Arduino IDE](https://www.arduino.cc/en/software) to program it. The BluetoothSerial library will be installed as part of the ESP32 Board addition to the Arduino IDE. Details of how you can install the ESP32 Board can be found [here](https://randomnerdtutorials.com/installing-the-esp32-board-in-arduino-ide-windows-instructions/). Before the Temperature Plotter can be used for the first time the Temperature probe must be powered up and the PC or Android device paired to the Bluetooth server of the Temperature probe. You will see the Bluetooth server listed as "Temperature probe" when it boots.
  
![](https://github.com/crystalxdesign/temperature-plotter/blob/master/ESP32TemperatureProbe.jpg)

## Java Client

This provides the most complete set of functionalities of the two clients. It depends on jfreechart (from jfree) to display the temperature data, and jSerialComm (from fazecast) to provide the serial connection over the USB cable or via Bluetooth. The code is Java and is supplied as an Eclipse Maven project. The two DS1820 probes each provide a trace on the graph, distinguished by blue and red traces. Before starting, the appropriate com port needs to be selected from the "Module Serial Port" drop-down list. Both Bluetooth and USB Serial connections show in the same fashion. If the manual option is chosen then once the "Start" button is clicked the sampling will continue until the "Stop" button is clicked. With the automatic mode you can select up to 1000 samples which will continue at the rate selected (once per second maximum) until complete.

![](https://github.com/crystalxdesign/temperature-plotter/blob/master/Animation.gif)

Captured traces can be saved by clicking the "Save Graph" button and giving it a file name. Traces are saved in CSV format and can be viewed later by use of the "Load Graph" button. You can add a graph title to describe the trace which is also retained in the saved file. The example directory contains two traces I have made which can be loaded and viewed. Right-clicking on the graph area also allows you to
>**Save** the trace as a PNG file by selecting "Save as>PNG"  
>**Copy** the trace image to the PC clipboard by selecting "Copy"  
>**Print** the trace image by selecting "Print"  

Below is an example of a printed trace from one of the examples:
![](https://github.com/crystalxdesign/temperature-plotter/blob/master/print.png)

The trace was made by placing the temperature probe in a domestic upright freezer. The probe was powered via a 5V battery pack connected to its USB cable. The probe was connected via Bluetooth to an external PC running the java client. The Blue probe was placed on the top shelf and the Red Probe on the bottom. We can see that once the door was closed (t=0), the top shelf cooled more slowly than the lower, and it took 12 minutes until their temperatures converged at a uniform -22<sup>o</sup>C.

## Android Client

This provides most of the key functionality of the Java Client but only saves one trace at a time, to save another means to overwrite the first. The MIT App Inventor (https://ai2.appinventor.mit.edu/) is used to build the client and the code is all in the android_client_app directory.
