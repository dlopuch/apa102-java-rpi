# apa102-java-rpi
Raspi/Odroid SPI driver lib for APA-102's, using [Pi4j](http://pi4j.com/) to do all the hard SPI control stuff.

Control APA-102 LED strips directly from a Raspi without any additional hardware -- no pixelpushers, no fadecandy, etc.
(level shifters to bring the 3.3V raspi up to 5V APA102 signalling are recommended, but not necessary for small runs).

For use with Java-based LED art like Processing or [LX](https://github.com/heronarts/LX) (example [LXOutput](https://github.com/star-cats/blinky-dome/blob/master/src/main/java/com/github/starcats/blinkydome/util/Apa102RpiOutput.java))

![Rainbow Fade Demo][image-1]

To use:

Grab `dist/apa102-java-rpi-lib-1.0-all.jar` and stick it in your project.

```java
Apa102Output.initSpi();
// Could also init with non-defaults using #initSpi(SpiChannel spiChannel, int spiSpeed, SpiMode spiMode)
// Default speed is 15 Mhz

Apa102Output strip = new Apa102Output(NUM_LEDS);

byte[] ledRGBs = new byte[ NUM_LEDS * 3 ];

while (true) {
  // <fill in your ledRGBs buffer with your pattern... eg examples/RainbowStrip.java>

  strip.writeStrip(ledRGBs);
}
```

See [examples/RainbowStrip](https://github.com/dlopuch/apa102-java-rpi/blob/master/examples/src/main/java/com/github/dlopuch/apa102_java_rpi/examples/RainbowStrip.java) for a full example.

Note that `byte`'s in java are only signed -- APA102's are controlled with values 0-255, but in java bytes are valued
-127 - 127. This can make working with bytes unintuitive to the uninitiated.
See [examples/BytesInJava](https://github.com/dlopuch/apa102-java-rpi/blob/master/examples/src/main/java/com/github/dlopuch/apa102_java_rpi/examples/BytesInJava.java)
for a primer.

# Compile Jar from Source
This project is configured to make a fatjar so all dependencies are included in a single executable.

To compile the `library` module and get a jar to use in your project:

```
./gradlew library:shadowJar

cp library/build/libs/apa102-java-rpi-lib-1.0-all.jar my/destination/project
```

# Smoketest
You can compile the `examples` project and get an executable jar that makes the obligatory rainbow fade.

![Rainbow Fade Example Wiring][image-2]

## Raspi Hardware
First, wire up your APA-102 and raspi like so:
  - `+` --> power supply `+5V`, **NOT** your raspi! (Your raspi can't source that much current, give your APA-102 a dedicated +5 source)
  - `CLK` --> raspi `SCLK`
  - `DAT` --> raspi `MOSI`
  - `GND` --> power supply `GND` *AND* raspi `GND` -- make a common ground

Note: The Raspberry Pi is a 3.3V device, while the APA-102's are 5V devices. You CAN connect the 3.3V **signal** lines directly
to the 5V APA's, and it will generally work if the wire or strip runs aren't so long that they cause enough of a voltage
drop that the 3.3V signal drops below the 5V signaling threshold.

The real way of doing this though is to use a level shifter like a 74AHCT125 or 74AHC125 for both the `SCLK` and `MOSI`
lines.  For a small piece of art, you can drive the signal directly.

In a pinch, you can use a single APA-102 "dummy pixel"/repeater close to the raspi as a good-enough level shifter.

## Raspi Config

Make sure you have SPI enabled on your raspi:
  - `sudo raspi-config`
  - "Interfacing Options" > "SPI" >  "Enable SPI"
  - Exit and reboot

Since this is java, you will also need a JDK.

## Software

Grab the `dist/apa102-java-rpi-examples-1.0-all.jar` jar, or compile the `examples/` project from source:

```
./gradlew examples:shadowJar
```

Get the jar onto your raspi:

```
scp examples/build/libs/apa102-java-rpi-examples-1.0-all.jar root@raspi.local:~/
```

And run it!

```
root@raspi.local $  sudo java -jar apa102-java-rpi-examples-1.0-all.jar
```

(Note you need to run it as sudo to get access to SPI driver).

If all is well, you should be seeing some rainbow blinky-blinks!

# See Also

- APA Signaling Protocol:
  - [Summary on endframe signalling](https://hackaday.com/2014/12/09/digging-into-the-apa102-serial-led-protocol/)
  - [Endframe signalling details](https://cpldcpu.com/2014/11/30/understanding-the-apa102-superled/)
- Previous work / other libs:
  - Python driver and lots of good documentation: [tinue/APA102_Pi](https://github.com/tinue/APA102_Pi)
  - C++ driver: [leonyuhanov/rpi_apa102driver](https://github.com/leonyuhanov/rpi_apa102driver)


[image-1]:	rainbow_demo.gif
[image-2]:	rainbow_demo.png
