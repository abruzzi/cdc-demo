import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class PackMultipleProviderTest {

    @Rule
    public PactProviderRule leaveServiceProvider = new PactProviderRule("leave_service_provider", "localhost", 8080, this);

    @Rule
    public PactProviderRule projectServiceProvider = new PactProviderRule("project_service_provider", "localhost", 8090, this);

    @Pact(provider="leave_service_provider", consumer="test_consumer")
    public PactFragment createFragmentForLeaveService(PactDslWithProvider builder) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parsed = simpleDateFormat.parse("2016-08-12");

        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody()
                .stringType("type", "SICK_LEAVE")
                .date("from", "yyyy-MM-dd", parsed)
                .date("to", "yyyy-MM-dd", parsed)
                .integerType("hours", 8);

        return builder
                .given("leave service")
                .uponReceiving("Leave Service")
                .path("/leaves/11046")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(pactDslJsonBody)
                .toFragment();
    }

    @Pact(provider="project_service_provider", consumer="test_consumer")
    public PactFragment createFragmentForProjectService(PactDslWithProvider builder) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parsed = simpleDateFormat.parse("2016-08-30");
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody()
                .stringType("project-id", "004c97")
                .date("created-at", "yyyy-MM-dd", parsed);

        return builder
                .given("project service")
                .uponReceiving("Project Service")
                .path("/projects/11046")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(pactDslJsonBody)
                .toFragment();
    }

    @Test
    @PactVerification(value = {"leave_service_provider", "project_service_provider"})
    public void runTest() throws IOException {
        Map<String, Object> leaveResponse = new HashMap<>();
        leaveResponse.put("type", "SICK_LEAVE");
        leaveResponse.put("from", "2016-08-12");
        leaveResponse.put("to", "2016-08-12");
        leaveResponse.put("hours", 8);

        Map<String, Object> projectResponse = new HashMap<>();
        projectResponse.put("project-id", "004c97");
        projectResponse.put("created-at", "2016-08-30");

        assertEquals(new ConsumerClient("http://localhost:8080").getAsMap("/leaves/11046", null), leaveResponse);
        assertEquals(new ConsumerClient("http://localhost:8090").getAsMap("/projects/11046", null), projectResponse);
    }
}
