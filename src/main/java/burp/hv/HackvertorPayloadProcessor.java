package burp.hv;

import burp.IIntruderPayloadProcessor;
import burp.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class HackvertorPayloadProcessor implements IIntruderPayloadProcessor {
    private final Hackvertor hackvertor;
    private final String name;
    private final String tag;

    public HackvertorPayloadProcessor(Hackvertor hackvertor, String name, String tag) {
        this.hackvertor = hackvertor;
        this.name = name;
        this.tag = tag;
    }

    public byte[] processPayload(byte[] currentPayload, byte[] originalPayload, byte[] baseValue) {
        String input = new String(currentPayload, StandardCharsets.UTF_8);
        String tagOutput;
        try {
            tagOutput = Convertors.callTag(new HashMap<>(), hackvertor.getCustomTags(), this.tag, input, new ArrayList<String>(), null);
        } catch (ParseException e) {
            return null;
        }
        return tagOutput.getBytes(StandardCharsets.UTF_8);
    }

    public String getProcessorName() {
        return this.name;
    }
}
