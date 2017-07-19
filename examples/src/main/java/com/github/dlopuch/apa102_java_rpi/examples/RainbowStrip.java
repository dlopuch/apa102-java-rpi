package com.github.dlopuch.apa102_java_rpi.examples;

import com.github.dlopuch.apa102_java_rpi.Apa102Output;

import java.io.IOException;
import java.util.Arrays;

/**
 * Make all the strip colors a rainbow
 */
public class RainbowStrip {
  /** Number of LED's to paint */
  public static final int NUM_LEDS = 32;

  /** How long one loop through the rainbow should take (ms) */
  public static final int ROTATION_DURATION_MS = 1000;

  /** How many pixels (LEDs) one rainbow loop takes up */
  public static final int RAINBOW_SPREAD_PX = NUM_LEDS / 2;

  static public void main(String args[]) throws Exception {

    Apa102Output.initSpi();
    Apa102Output output = new Apa102Output(NUM_LEDS);

    byte[] leds = new byte[ NUM_LEDS * 3 ];
    final boolean[] loop = new boolean[] { true };


    // Make sure we turn everything off when shutting down
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Shutting down: turning all LEDs off...");

        // Stop output thread loop
        loop[0] = false;

        // And turn all off
        byte[] allOff = new byte[ NUM_LEDS * 3 ];
        Arrays.fill(allOff, (byte) 0x00);
        try {
          output.writeStrip(allOff);
        } catch (IOException e) {
          throw new RuntimeException("ERROR turning all off", e);
        }
      }
    }));


    // Do rainbow loop
    double phi = 0;
    double phiIncrement = 16d / ROTATION_DURATION_MS;
    double pixelPhiIncrement = 1d / RAINBOW_SPREAD_PX;
    while (loop[0]) {

      double pixelPhi = phi;
      for (int i=0; i<leds.length; i += 3) {
        RainbowUtils.fillRgb(leds, i, pixelPhi);
        pixelPhi += pixelPhiIncrement;
      }

      output.writeStrip(leds);

      phi += phiIncrement;
      if (phi >= 1) {
        System.out.println("Looping back to red");
        phi = 0;
      }

      Thread.sleep(16);
    }
  }
}
