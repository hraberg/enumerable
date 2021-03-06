package org.enumerable.lambda.exception;

import static org.enumerable.lambda.exception.UncheckedException.*;
import static org.junit.Assert.*;

import java.io.FileInputStream;

import org.enumerable.lambda.exception.UncheckedException;
import org.junit.Test;

public class UncheckedExceptionTest {
    @Test(expected = UncheckedException.class)
    public void wrapsCheckedExcetion() throws Exception {
        try {
            new FileInputStream("file not found");
        } catch (Exception e) {
            UncheckedException uncheck = (UncheckedException) uncheck(e);
            assertEquals(e.getMessage(), uncheck.getMessage());
            assertEquals(e.toString(), uncheck.toString());
            assertNull(uncheck.getCause());
            throw uncheck;
        }
    }

    @Test(expected = ArithmeticException.class)
    public void doesNotWrapUncheckedException() throws Exception {
        try {
            System.out.println(2 / 0);
        } catch (Exception e) {
            RuntimeException uncheck = uncheck(e);
            assertNull(uncheck.getCause());
            throw uncheck;
        }
    }

    @Test(expected = ArithmeticException.class)
    public void dropsNestedExceptions() throws Exception {
        try {
            try {
                getClass().getMethod("throwsAnException").invoke(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            RuntimeException uncheck = uncheck(e);
            assertNull(uncheck.getCause());
            throw uncheck;
        }
    }

    public void throwsAnException() {
        System.out.println(2 / 0);
    }
}
