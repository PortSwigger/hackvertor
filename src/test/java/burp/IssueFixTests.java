package burp;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.hv.Convertors;
import burp.hv.HackvertorExtension;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IssueFixTests extends BaseHackvertorTest {

    @BeforeEach
    void enableCodeExecution() throws Exception {
        HackvertorExtension.generalSettings.setBoolean("codeExecutionTagsEnabled", true);
    }

    private static HttpRequest mockRequest(String body) {
        return (HttpRequest) Proxy.newProxyInstance(
                HttpRequest.class.getClassLoader(),
                new Class[]{HttpRequest.class},
                (p, method, args) -> {
                    switch (method.getName()) {
                        case "bodyToString":
                            return body;
                        case "toString":
                            return "POST / HTTP/1.1\r\nHost: test\r\n\r\n" + body;
                        default:
                            return null;
                    }
                });
    }

    @Test
    void issue158_contextBodyProcessesNestedTags() {
        HackvertorExtension.hackvertor = hackvertor;
        hackvertor.setRequest(mockRequest("<@base64>secret</@base64>"));

        String key = HackvertorExtension.tagCodeExecutionKey;
        String result = hackvertor.convert("<@context_body('" + key + "')/>", hackvertor);

        assertEquals("c2VjcmV0", result);
    }

    @Test
    void issue161_pythonExposesRequestAndResponse() {
        HackvertorExtension.hackvertor = hackvertor;
        HttpRequest req = mockRequest("hello world");
        hackvertor.setRequest(req);

        String key = HackvertorExtension.tagCodeExecutionKey;
        String code = "output = 'has-request=' + str(request is not None) + ',has-response=' + str(response is None)";
        String result = Convertors.python(new HashMap<>(), "ignored", code, key, null, new JSONArray(), hackvertor);

        assertEquals("has-request=True,has-response=True", result);
    }

    @Test
    void issue162_payloadProcessorUsesUtf8() {
        burp.hv.HackvertorPayloadProcessor processor =
                new burp.hv.HackvertorPayloadProcessor(hackvertor, "test", "unicode_escapes");

        byte[] utf8Input = "ŠČ".getBytes(StandardCharsets.UTF_8);
        byte[] output = processor.processPayload(utf8Input, utf8Input, utf8Input);

        assertNotNull(output);
        String outputStr = new String(output, StandardCharsets.UTF_8);
        assertEquals("\\u0160\\u010C", outputStr);
    }

    @Test
    void issue164_base64DecodePreservesUtf8Korean() {
        String korean = "안녕하세요";
        String base64 = Base64.getEncoder().encodeToString(korean.getBytes(StandardCharsets.UTF_8));
        String input = "<@d_base64>" + base64 + "</@d_base64>";

        String result = hackvertor.convert(input, hackvertor);

        assertEquals(korean, result);
    }
}
