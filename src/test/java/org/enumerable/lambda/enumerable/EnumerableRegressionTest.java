package org.enumerable.lambda.enumerable;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.lang.System.getProperty;
import static org.junit.Assert.assertEquals;

public class EnumerableRegressionTest {
    String expected = ""
            + "Country: malaysia\n"
            + "Country: thailand\n"
            + "Country: india\n"
            + "Country: people's republic of china\n"
            + "{people's republic of china=3, thailand=1, india=2, malaysia=0}\n"
            + "[MALAYSIA, THAILAND, INDIA, PEOPLE'S REPUBLIC OF CHINA]\n"
            + "[malaysia, malaysiathailand, malaysiathailandindia, malaysiathailandindiapeople's republic of china]\n"
            + "malaysiathailandindiapeople's republic of china\n"
            + "[5, 1, 8, 4, 6, 3, 10, 2, 7, 9]\n"
            + "[25, 1, 64, 16, 36, 9, 100, 4, 49, 81]\n"
            + "[10, 9, 8, 7, 6, 5, 4, 3, 2, 1]\n"
            + "[india, malaysia, thailand, people's republic of china]\n"
            + "[5, 1, 3, 7, 9]\n"
            + "[2.23606797749979, 1.0, 2.8284271247461903, 2.0, 2.449489742783178, 1.7320508075688772, 3.1622776601683795, 1.4142135623730951, 2.6457513110645907, 3.0]\n"
            + "[hello, hello, hello, hello, hello, hello, hello, hello, hello, hello]\n" + "[1, 4, 3, 2]\n" + "8\n"
            + "-1\n" + "true\n" + "true\n" + "3\n" + "[[8, 4, 6, 10, 2], [5, 1, 3, 7, 9]]\n" + "55\n" + "3628800\n"
            + "people's republic of china\n" + "5, 1, 8, 4, 6, 3, 10, 2, 7, 9\n" + "7\n" + "-3\n" + "-6\n" + "55\n"
            + "[1, 0, 1]\n" + "[275, 55, 165, 385, 495]\n" + "6.283185307179586\n" + "running...\n"
            + "[8, 4, 11, 7, 9, 6, 13, 5, 10, 12]\n" + "[11, 7, 14, 10, 12, 9, 16, 8, 13, 15]\n" + "2\n";

    @Test
    public void regression() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new EnumerableExample().example(new PrintStream(out));
        assertEquals(expected, out.toString().replaceAll(getProperty("line.separator"), "\n"));
    }
}
