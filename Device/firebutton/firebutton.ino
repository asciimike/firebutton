#include <Wire.h>

#define BLINKM_ADDRESS 0x09
#define BUTTON_PIN 2

void setup() {
  // Enable serial
  Serial.begin(9600);
  
  // Turn off blinkm loop
  Wire.begin();
  Wire.beginTransmission(BLINKM_ADDRESS);
  Wire.write('o');
  Wire.endTransmission();

  // Enable button interrupt
  attachInterrupt(digitalPinToInterrupt(BUTTON_PIN), buttonPress, RISING);
}

void loop() {
  // Parse "Color:RRR;GGG;BBB\n"
  if (Serial.available() > 0) {
    String str = Serial.readStringUntil('\n');
    if (str.charAt(0) == 'C') {
      int colon = str.indexOf(':');
      int firstSemicolon = str.indexOf(';');
      int secondSemicolon = str.indexOf(';', firstSemicolon + 1);
      int red = str.substring(colon + 1,firstSemicolon).toInt();
      int green = str.substring(firstSemicolon + 1, secondSemicolon).toInt();
      int blue = str.substring(secondSemicolon + 1).toInt();
      sendRGB(red, green, blue);
    }
  }
}

void buttonPress() {
  // Debounce button press
  static unsigned long last_interrupt_time = 0;
  unsigned long interrupt_time = millis();
  if (interrupt_time - last_interrupt_time > 200) {
    // Send out "Button:0|1\n"
       int val = digitalRead(2);
       String stringVal =  String(val, DEC);
       String printString = String("Button:" + stringVal + "\n");
       Serial.print(printString);
  }
  last_interrupt_time = interrupt_time;
}

void sendRGB(int r, int g, int b) {
  // Fade to RGB color
  Wire.beginTransmission(BLINKM_ADDRESS);
  Wire.write('c');
  Wire.write(r);
  Wire.write(g);
  Wire.write(b);
  Wire.endTransmission();
}

