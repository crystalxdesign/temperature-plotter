#include <OneWire.h>
#include <DallasTemperature.h>

// Data wire is plugged TO GPIO 4
#define ONE_WIRE_BUS 2

// Setup a oneWire instance to communicate with any OneWire devices (not just Maxim/Dallas temperature ICs)
OneWire oneWire(ONE_WIRE_BUS);

// Pass our oneWire reference to Dallas Temperature. 
DallasTemperature sensors(&oneWire);

// Number of temperature devices found
int numberOfDevices;

// We'll use this variable to store a found device address
DeviceAddress blueDeviceAddress; 
DeviceAddress redDeviceAddress; 

void setup(){
  // start serial port
  Serial.begin(115200);
  Serial.flush();
  //Serial.setRxFIFOFull(1);
  //Serial.onReceive(onReceiveFunction, false);
  
  // Start up the library
  sensors.begin();
  
  // Grab a count of devices on the 
  numberOfDevices = sensors.getDeviceCount();
  
  // locate devices on the bus
  Serial.print("Locating devices...");
  Serial.print("Found ");
  Serial.print(numberOfDevices, DEC);
  Serial.println(" probes.");
  if(numberOfDevices != 2){
    Serial.println("WARNING: All probes not found");
    return;
  }
  // Search the wire for address
  if(sensors.getAddress(redDeviceAddress, 0)){
    Serial.print("Found RED Probe with address: ");
    printAddress(redDeviceAddress);
    Serial.println();
  }
  if(sensors.getAddress(blueDeviceAddress, 1)){
    Serial.print("Found BLUE Probe with address: ");
    printAddress(blueDeviceAddress);
    Serial.println();
  }
}

void onReceiveFunction(void){ 
  while(Serial.available() > 0){
    Serial.read();
  }
  Serial.flush();
  sensors.requestTemperatures(); // Send the command to get temperatures
  String output = "";
  float redTemp= 0.0;
  float blueTemp= 0.0;
  redTemp = sensors.getTempC(redDeviceAddress);
  blueTemp = sensors.getTempC(blueDeviceAddress);
  Serial.print(redTemp);
  Serial.print("@");
  Serial.print(blueTemp);
  Serial.println("@");
}

// function to print a device address
void printAddress(DeviceAddress deviceAddress) {
  for (uint8_t i = 0; i < 8; i++){
    if (deviceAddress[i] < 16) Serial.print("0");
      Serial.print(deviceAddress[i], HEX);
  }
}

void loop(){
  if(Serial.available() > 0){
    onReceiveFunction();
  }
  delay(10);
}