package com.github.dlopuch.apa102_java_rpi;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

import java.io.IOException;

/**
 * SPI output for APA102 LEDs on a raspi (or similar, eg odroid)
 */
public class Apa102Output {

  private static final int BYTES_PER_LED = 4;

  private static byte[] START_FRAME = new byte[] {0x00, 0x00, 0x00, 0x00};
  private static SpiDevice spi = null;

  private byte[] ledBuffer;
  private int i_firstLedFrame;
  private int i_endFrame;
  private int numLeds;
  private ColorOrder colorOrder;

  /**
   * Init the SPI controller using default bindings (15 MHz)
   * @throws IOException If can't open SPI (not a raspi or not running sudo)
   */
  public static void initSpi() throws IOException {
    initSpi(
        SpiChannel.CS0,
        15700000, // rounds down to 15.6..., compare to SpiDevice.DEFAULT_SPI_SPEED
        SpiDevice.DEFAULT_SPI_MODE
    );
  }

  /**
   * Init the SPI controller using Pi4J bindings
   * @param spiChannel See https://www.raspberrypi.org/documentation/hardware/raspberrypi/spi/README.md
   * @param spiSpeed speed in hz, see https://www.raspberrypi.org/documentation/hardware/raspberrypi/spi/README.md
   * @param spiMode see https://www.raspberrypi.org/documentation/hardware/raspberrypi/spi/README.md
   * @throws IOException If can't open SPI (not a raspi or not running sudo)
   */
  public static void initSpi(SpiChannel spiChannel, int spiSpeed, SpiMode spiMode) throws IOException {
    spi = SpiFactory.getInstance(spiChannel, spiSpeed, spiMode);
  }

  /**
   * This library's {@link #writeStrip(byte[])} assumes colors are passed to this lib as RGB triplets, but
   * APA-102's expect the LED colors to be sent in a different order.
   *
   * A ColorOrder instance defines the expected color orders in your APA-102 LED data frames.  Your hardware may vary.
   */
  public interface ColorOrder {
    /** @return 0-2, expected position of red byte */
    int getRed();

    /** @return 0-2, expected position of green byte */
    int getGreen();

    /** @return 0-2, expected position of blue byte */
    int getBlue();
  }

  /**
   * Common APA-102 {@link ColorOrder} configurations
   */
  public enum ColorConfig implements ColorOrder {
    /** Default, how datasheet specifies it */
    BGR(2, 1, 0),

    /** straight RGB ordering */
    RGB(0, 1, 2);

    private int r;
    private int g;
    private int b;

    ColorConfig(int r, int g, int b) {
      this.r = r;
      this.g = g;
      this.b = b;
    }

    @Override
    public int getRed() {
      return r;
    }

    @Override
    public int getGreen() {
      return g;
    }

    @Override
    public int getBlue() {
      return b;
    }
  }

  public Apa102Output(int numLeds) {
    this(numLeds, ColorConfig.BGR);
  }

  public Apa102Output(int numLeds, ColorOrder colorConfig) {
    if (spi == null) {
      throw new RuntimeException("Call .initSpi() before constructing new output!");
    }

    this.colorOrder = colorConfig;

    this.numLeds = numLeds;
    i_firstLedFrame = 4; // 4 bytes for start frame
    i_endFrame = i_firstLedFrame + numLeds * BYTES_PER_LED;

    ledBuffer = new byte[
        4 + // start frame

        numLeds * BYTES_PER_LED +

        // end frame: see https://cpldcpu.com/2014/11/30/understanding-the-apa102-superled/
        // or summary: https://hackaday.com/2014/12/09/digging-into-the-apa102-serial-led-protocol/
        (int) Math.ceil((double) numLeds / 2d / 8d)
    ];

    // init each LED start byte
    for (int i=i_firstLedFrame; i < i_endFrame; i += BYTES_PER_LED) {
      ledBuffer[i] = (byte) 0xFF;
    }

    // end frame: pack extra 1's (see https://cpldcpu.com/2014/11/30/understanding-the-apa102-superled )
    for (int i=i_endFrame; i < ledBuffer.length; i++) {
      ledBuffer[i] = (byte) 0xFF;
    }

  }


  /**
   * Write a set of colors to the SPI output
   * @param rgbTriplets RGB color specs.  [0] is led0-R, [1] is led0-G, [2] is led0-B, [3] is led1-R, etc.
   *                    Length must be 3 * numLeds defined at construction.
   *                    Note that bytes in java are signed -- 0b1000_0000 is java value -127.
   * @throws IOException If error writing
   */
  public void writeStrip(byte[] rgbTriplets) throws IOException {
    if (rgbTriplets.length != numLeds * 3) {
      throw new RuntimeException("Invalid rgbTriplets.  Expected " + numLeds + " LEDs, or length " +
          (numLeds * 3));
    }

    int colorI = 0;
    for (int i=i_firstLedFrame; i < i_endFrame; i += BYTES_PER_LED) {
      // ledBuffer[ i + 0 ] is the (pre-initialized) 0xFF starter
      ledBuffer[ i + 1 + colorOrder.getRed()   ] = rgbTriplets[ colorI++ ];
      ledBuffer[ i + 1 + colorOrder.getGreen() ] = rgbTriplets[ colorI++ ];
      ledBuffer[ i + 1 + colorOrder.getBlue()  ] = rgbTriplets[ colorI++ ];
    }

    // Simple case: all in one write
    if (ledBuffer.length < SpiDevice.MAX_SUPPORTED_BYTES) {
      spi.write(ledBuffer);
      return;
    }

    // Multiple writes:
    int startI = 0;
    while (startI <= ledBuffer.length) {
      spi.write(ledBuffer, startI, SpiDevice.MAX_SUPPORTED_BYTES);

      startI += SpiDevice.MAX_SUPPORTED_BYTES;
    }

  }

}
