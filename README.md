# apa102-java-rpi
Raspi/Odroid SPI driver lib for APA-102's, using [Pi4j](http://pi4j.com/) to do all the hard SPI control stuff.

To use:

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

See [RainbowStrip](https://github.com/dlopuch/apa102-java-rpi/blob/master/examples/src/main/java/com/github/dlopuch/apa102_java_rpi/examples/RainbowStrip.java) for a full example.

# Compile Jar
This project is configured to make a fatjar so all dependencies are included in a single executable.

To compile the `library` module and get a jar to use in your project:

```
./gradlew library:shadowJar

cp library/build/libs/apa102-java-rpi-lib-1.0-SNAPSHOT-all.jar my/destination/project
```

# Smoketest
You can compile the `examples` project and get an executable jar that makes the obligatory rainbow fade.

## Raspi Hardware
First, wire up your APA-102 and raspi like so:
  - `+` --> power supply `+5V`, **NOT** your raspi! (Your raspi can't source that much current, give your APA-102 a dedicated +5 source)
  - `CLK` --> raspi `SCLK`
  - `DAT` --> raspi `MOSI`
  - `GND` --> power supply `GND` *AND* raspi `GND` -- make a common ground

Note: The Raspberry Pi is a 3.3V device, while the APA-102's are 5V devices. You CAN connect the 3.3V **signal** lines directly
to the 5V APA's, and it will generally work if the wire or strip runs aren't so long that they cause enough of a voltage
drop that drops the 3.3V signal below the signaling threshold.

The real way of doing this though is to use a level shifter like a 74AHCT125 or 74AHC125 for both the `SCLK` and `MOSI`
lines.  For a small piece of art, you can drive the signal directly.

## Raspi Config

Make sure you have SPI enabled on your raspi:
  - `sudo raspi-config`
  - "Interfacing Options" > "SPI" >  "Enable SPI"
  - Exit and reboot

Since this is java, you will also need a JDK.

## Software

Compile the `examples/` project:

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

