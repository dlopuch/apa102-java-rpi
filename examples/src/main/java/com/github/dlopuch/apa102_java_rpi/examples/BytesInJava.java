package com.github.dlopuch.apa102_java_rpi.examples;

/**
 * The byte datatype in java is signed -- still 8 bits, but values are -128 - 127.
 *
 * This makes working with bytes a bit tricky.
 *
 * Read this source and then run it to go through some of the pitfalls of working with bytes in java.
 *
 * Easiest path forward is to work with int's and just cast into bytes, eg
 *   int myInt = 130;
 *   byte myByte = (byte) myInt;
 *   System.out.println("But watch out evaluating the value.  This does NOT print out 130: " + myByte);
 *
 */
public class BytesInJava {
  static public void main(String args[]) throws Exception {
    byte byteVar;


    byteVar = 0b111_1111;
    System.out.println("Using java binary notation, you can cram only 7 bits into a bit. 0b111_1111 = " + byteVar);

    // 8 bits doesn't work:
//    byteVar = 0b1000_0000;
    System.out.println("byteVar = 0b1000_0000 doesn't work because that evals to 128 (try it: " + (0b1000_0000) +
        "), which since java works with signed numbers, goes beyond 8 bits.");

    System.out.println("");

    System.out.println("Okay, so since it's signed, you can just specify the 8th bit by adding a negative sign, right? " +
        "So 0b1000_0001 (ie -127) should be written as -0b000_0001, right? Lets see: ");
    byteVar = -0b000_0001;
    System.out.println("\t" + byteVar);
    System.out.println("Nope. Java interprets '-0b000_0001' as 'negative of 1', so the underlying binary representation is:");
    System.out.println("\t" + Integer.toBinaryString(byteVar));

    System.out.println("");

    System.out.println("You can, however, FORCE 0b1000_0000 into a byte with a byte-cast. Try it: " + ( (byte) 0b1000_0000) );
    System.out.println("This works because any 0bXXXX... is internally a 32-bit int, and the byte-cast just chops " +
        "off all but the last 8 bits.  Note that the value is -128, not 128 though");

    System.out.println("");

    System.out.println("So, to create your byte arrays, you can work in twos-complement natively:");
    for (byte i=120; i > 0 || i <= -120; i++) { // note loop condition assumes rollover... 127 + 1 = -128
      System.out.println("\ti: " + i + "\tbinary: " + Integer.toBinaryString(i));
    }

    System.out.println("");

    System.out.println("Or you can work in familiar ints and rely on the typecast to get the right number (but note " +
        "that then evaluating the value of the underlying bytes is fraught with perils):");
    for (int i=120; i < 135; i++) {
      byteVar = (byte) i;
      System.out.println("\tint i: " + i + "\t byte i: " + byteVar + "\tbinary: " + Integer.toBinaryString(byteVar));
    }

    System.out.println("");

    System.out.println("Easiest path forward is to work with int's and just cast into bytes, eg byte myByte = (byte) 130");
    int myInt = 130;
    byte myByte = (byte) myInt;
    System.out.println("But watch out evaluating the value.  This does NOT evaluate to 130: " + myByte);
  }
}
