package suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(
{
    server.TestServer.class,
    server.TestWorker.class,
    client.TestClient.class
})
public class TestSuite {

    public static final long SETUP_TIME    = 20; // ms
    public static final long ACCEPT_TIME   =  5; // ms
    public static final long TRANSFER_TIME =  5; // ms
}
