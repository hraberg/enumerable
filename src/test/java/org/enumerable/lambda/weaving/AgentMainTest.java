package org.enumerable.lambda.weaving;

import org.enumerable.lambda.enumerable.EnumerableRegressionTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class AgentMainTest {
    @Test
    public void canAttachAgentToRunningProcess() {
        assertFalse("The agent should not be running", LambdaLoader.isEnabled());
        LambdaAgentAttach.attachAgent();
        new EnumerableRegressionTest().regression();
    }
}
