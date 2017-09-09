const unsigned short BT_BAUD_RATE = 9600;
 
void setup() {
  // We will use Serial for the serial monitor
  Serial.begin(9600);
  Serial.print("Sketch: ");
  Serial.println(__FILE__);
  Serial.print("Uploaded on: ");
  Serial.println(__DATE__);
  Serial.println();

  // We will use Serial1 for the Bluetooth module
  Serial1.begin(BT_BAUD_RATE);  
  Serial.print("Bluetooth serial started at ");
  Serial.print(BT_BAUD_RATE);
  Serial.println(" baud.");
  Serial.println();
}

void loop() {
}

bool startOfUserInput = true;

/*
  SerialEvent occurs whenever a new data comes in the hardware serial RX. This
  routine is run between each time loop() runs, so using delay inside loop can
  delay response. Multiple bytes of data may be available.
*/
// This is the monitor SerialEvent
void serialEvent() {
  while (Serial.available()) {
    // Get the new byte from the monitor (input by the user)
    char c = (char)Serial.read();
    
    // Send to the Bluetooth module
    Serial1.write(c);

    // Echo the user input to the monitor
    if (startOfUserInput) {
      Serial.print("> ");
      startOfUserInput = false;
    }
    Serial.print(c);
    
    // If the incoming character was a newline
    if (c == '\n')
      startOfUserInput = true; // Prepare for next input
  }
}

// This is the Bluetooth SerialEvent
void serialEvent1() {
  while (Serial1.available()) {
    // Get the new byte from the Bluetooth module
    char c = (char)Serial1.read();
    
    // Echo to the serial monitor
    Serial.print(c);
  }
}
