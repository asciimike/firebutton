# Reddit "Button" game, powered by Firebase
This repo demonstrates an easy (yes, easy, we promise) IoT game that connects a physical device to the internet via an Android app and [Firebase](https://www.firebase.com). You'll need a Firebase application, so if you don't have a Firebase account, [sign up for free](https://www.firebase.com/signup) right now!

This project was initially built for a tech talk entitled "Simplifying IoT with Firebase" at [Droidcon London](http://uk.droidcon.com) on 30 October. Slides for this talk are available [here](https://github.com/mcdonamp/firebutton/blob/master/Simplifying%20IoT%20with%20Firebase%20(Android).pdf).

## Hardware Needed
Phew was mostly built from parts lying around in my electronics box, but all the parts are inexpensive and redily available from your favorite online (electronics) retailer:

1. 1x Arduino Uno R3 (any will do, though you'll have to update device_filter.xml with the PID and VID)
2. 1x BlinkM I2C LED ([datasheet](https://thingm.com/fileadmin/thingm/downloads/BlinkM_datasheet.pdf))
3. 1x 4.7kOhm resistor
4  1x Pushbutton (of your favorite variety)
5. 6x long wires
6. 1x breadboard
7. 1x ping pong ball (to be used as a bulb diffuser)

Once you've got everything, wire it up like so:
![Fritzing wiring diagram for Button Game](https://github.com/mcdonamp/phew/blob/master/Device/button_game.png)

Instructions:

1. Connect Arduino Ground to Ground (BlinkM Pin 1)
2. Connect Arduino 5V Power to Power (BlinkM Pin 2)
3. Connect SCL (Arduino A5) to C (BlinkM Pin 4)
4. Connect SDA (Arduino A4) to D (BlinkM Pin 3)
5. Connect Button to Interrupt 0 (Arduino D2), add pulldown resistor, connect other side to Arduino 5V power
6. Cut a hole in the ping pong ball (careful!) and insert it over the LED

## Arduino Installation
Open the `firebutton` Arduino project in the Arduino IDE, and upload it to your device. This should set up everything you need.

The device responds to serial commands of the form `Color:RRR;GGG;BBB\n`, and will fade the LED to that RGB color.

The device produces serial commands of the form `Button:1|0\n`, which corresponds to whether the button is pressed or not.

## App Software Installation
All of the code for the Phew App lives in `FireButton`. Once you're there, simply open the project in Android Studio and run on your favorite device.

You can also [play with the App online](https://goo.gl/nJtOIY) via [Appetize.io](https://www.appetize.io).

## Questions, comments, contributions?
Contact me [@asciimike](https://www.twitter.com/asciimike) or mcdonald at firebase dot com to learn more about Firebase, IoT, iOS development, or with any burning questions you may have!
