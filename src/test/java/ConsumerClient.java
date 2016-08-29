import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsumerClient{
    private static final String TESTREQHEADER = "testreqheader";
    private static final String TESTREQHEADERVALUE = "testreqheadervalue";
    private String url;

    public ConsumerClient(String url) {
        this.url = url;
    }

    public Map getAsMap(String path, String queryString) throws IOException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(url).setPath(path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if (StringUtils.isNotEmpty(queryString)) {
            uriBuilder.setParameters(parseQueryString(queryString));
        }
        return jsonToMap(Request.Get(uriBuilder.toString())
                .addHeader(TESTREQHEADER, TESTREQHEADERVALUE)
                .execute().returnContent().asString());
    }

    private List<NameValuePair> parseQueryString(String queryString) {
        return Arrays.asList(queryString.split("&")).stream().map(s -> s.split("="))
                .map(p -> new BasicNameValuePair(p[0], p[1])).collect(Collectors.toList());
    }

    private HashMap jsonToMap(String respBody) throws IOException {
        if (respBody.isEmpty()) {
            return new HashMap();
        }
        return new ObjectMapper().readValue(respBody, HashMap.class);
    }

}