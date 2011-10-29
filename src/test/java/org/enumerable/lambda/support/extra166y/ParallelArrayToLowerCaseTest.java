package org.enumerable.lambda.support.extra166y;

import static extra166y.ParallelLongArray.*;
import static org.enumerable.lambda.Parameters.*;
import static org.enumerable.lambda.support.extra166y.LambdaOps.*;
import static org.junit.Assert.*;

import org.junit.Test;

import extra166y.ParallelArray;
import extra166y.Ops.Op;

/*
 * This test was adapted from the 'Examples from Jacques DeFarge' on the Java
 * Concurrency Wiki at
 * http://artisans-serverintellect-com.si-eioswww6.com/default.asp?W7
 */
public class ParallelArrayToLowerCaseTest {
    @Test
    public void parallelToLowerCase() {
        final String[] list = new String[] { "AB", "CD", "EF", "GH", "MN", "RM", "JP", "LS", "QP", "TX", "ST",
                "SZ", "AA", "PQ", "RS", "RM", "JP", "LS", "QP", "TX", "ST", "SZ", "AA", "PQ", "RS", "RM", "JP",
                "LS", "QP", "TX", "ST", "SZ", "AA", "PQ", "RS", "RM", "JP", "LS", "QP", "TX", "CO", "BA", "MP",
                "AM", "FF" };

        ParallelArray<String> lambdaPArray = ParallelArray.createFromCopy(list, defaultExecutor());
        ParallelArray<String> pArray = ParallelArray.createFromCopy(list, defaultExecutor());

        class ToLowerMapping implements Op<String, String> {
            public String op(String input) {
                return input.toLowerCase();
            }
        }
        ToLowerMapping toLowerMapping = new ToLowerMapping();
        pArray.replaceWithMapping(toLowerMapping);

        Op<String, String> op = op(s, s.toLowerCase());
        lambdaPArray.replaceWithMapping(op);

        assertEquals(pArray.asList(), lambdaPArray.asList());
    }
}
