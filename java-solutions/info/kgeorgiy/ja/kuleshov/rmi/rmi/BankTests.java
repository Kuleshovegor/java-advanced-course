package info.kgeorgiy.ja.kuleshov.rmi.rmi;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({BankTest.class})
public class BankTests {
    public static void main(final String[] args) {
        final Result result = new JUnitCore().run(BankTests.class);
        if (result.wasSuccessful()) {
            System.out.printf("OK for BankTest in %dms", result.getRunTime());
            System.exit(0);
        }
        for (final Failure failure : result.getFailures()) {
            System.err.println("FAIL");
            System.err.println("Test " + failure.getDescription().getMethodName() + " failed: " + failure.getMessage());
            if (failure.getException() != null) {
                failure.getException().printStackTrace();
            }
            System.exit(1);
        }
    }
}
