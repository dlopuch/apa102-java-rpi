package com.github.dlopuch.apa102_java_rpi.examples;

/**
 * Quick util to make rainbows
 */
public class RainbowUtils {

  /**
   * Like {@link #fillRgb(byte[], int, int)}, but with a 0-1 input for phi.
   * @param normalizedPhi Value from 0-1 indicating color. 0/1 is red, .333 is green, .667 is blue
   */
  static public void fillRgb(byte[] rgbBuffer, int bufferOffset, double normalizedPhi) {
    fillRgb(rgbBuffer, bufferOffset, (int) (normalizedPhi * 3 * 256));
  }

  /**
   * Calculates RGB bytes and injects them into some buffer of RGB values
   * @param rgbBuffer Array of RGB triplets
   * @param bufferOffset Entry index into buffer
   * @param phi Value from 0 - 3*256 indicating color. 0 is red, 256 is green, 2*256 is blue, 3*256 is back to red
   */
  static public void fillRgb(byte[] rgbBuffer, int bufferOffset, int phi) {
    if (phi >= 256 * 3) {
      phi %= 256 * 3;
    }

    int phase = phi % 256;

    byte red;
    byte green;
    byte blue;
    if (phi >= 0 && phi < 256) {
      red = (byte) (255 - phase);
      green = (byte) phase;
      blue = (byte) 0x00;

    } else if (phi < 256*2) {
      red = 0x00;
      green = (byte) (255 - phase);
      blue = (byte) phase;

    } else if (phi < 256 * 3){
      red = (byte) phase;
      green = 0x00;
      blue = (byte) (255 - phase);

    } else {
      red = 0x00;
      green = 0x00;
      blue = 0x00;
    }

    rgbBuffer[ bufferOffset + 0 ] = red;
    rgbBuffer[ bufferOffset + 1 ] = green;
    rgbBuffer[ bufferOffset + 2 ] = blue;
  }
}
