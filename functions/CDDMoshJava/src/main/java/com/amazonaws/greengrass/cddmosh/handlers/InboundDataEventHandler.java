package com.amazonaws.greengrass.cddmosh.handlers;

import com.amazonaws.greengrass.cddmosh.data.Topics;
import com.amazonaws.greengrass.cddmosh.events.DatagramSocketCreatedEvent;
import com.amazonaws.greengrass.cddmosh.events.InboundDataEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.timmattison.greengrass.cdd.events.GreengrassLambdaEvent;
import com.timmattison.greengrass.cdd.handlers.interfaces.GreengrassLambdaEventHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class InboundDataEventHandler implements GreengrassLambdaEventHandler {
    private final EventBus eventBus;
    private final Topics topics;
    private final Map<Integer, DatagramSocket> datagramSocketMap = new HashMap<>();

    @Override
    public boolean isTopicExpected(String topic) {
        return topic.startsWith(topics.getDataBaselineTopic());
    }

    @Override
    public void execute(GreengrassLambdaEvent greengrassLambdaEvent) {
        if (greengrassLambdaEvent.getInput() == null) {
            log.error("No input");
            return;
        }

        if (!greengrassLambdaEvent.getTopic().isPresent()) {
            log.error("No topic");
            return;
        }

        String topic = greengrassLambdaEvent.getTopic().get();

        if (topic.startsWith(topics.getDataServerPrefix())) {
            // Ignore server messages
            return;
        }

        if (!topic.startsWith(topics.getDataClientPrefix())) {
            log.error("Unexpected inbound topic [" + topic + "]");
            return;
        }

        Optional<byte[]> optionalBinaryInput = greengrassLambdaEvent.getBinaryInput();

        if (!optionalBinaryInput.isPresent()) {
            log.error("No binary input");
            return;
        }

        byte[] binaryInput = optionalBinaryInput.get();

        int port = Integer.parseInt(topic.substring(topic.lastIndexOf('/') + 1));

        InboundDataEvent.InboundDataEventBuilder inboundDataEventBuilder = InboundDataEvent.builder();

        inboundDataEventBuilder.port(port);
        inboundDataEventBuilder.data(binaryInput);

        eventBus.post(inboundDataEventBuilder.build());
    }

    @Subscribe
    public void dataEvent(InboundDataEvent inboundDataEvent) {
        if (!datagramSocketMap.containsKey(inboundDataEvent.getPort())) {
            log.info("Message on port we're not listening on [" + inboundDataEvent.getPort());
            return;
        }

//        log.info("Sending datagram on socket...");
        DatagramSocket datagramSocket = datagramSocketMap.get(inboundDataEvent.getPort());
        datagramSocket.send(Buffer.buffer(inboundDataEvent.getData()), inboundDataEvent.getPort(), "127.0.0.1", s -> {
        });
    }

    @Subscribe
    public void datagramSocketCreatedEvent(DatagramSocketCreatedEvent datagramSocketCreatedEvent) {
        datagramSocketMap.put(datagramSocketCreatedEvent.getSendPort(), datagramSocketCreatedEvent.getDatagramSocket());
    }
}