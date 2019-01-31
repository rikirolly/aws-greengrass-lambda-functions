package com.amazonaws.greengrass.cddmosh.data;

import com.timmattison.greengrass.cdd.data.CddTopics;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Topics {
    private final CddTopics cddTopics;

    @Getter(lazy = true)
    private final String baselineTopic = cddTopics.getCddDriverTopic(this);

    @Getter(lazy = true)
    private final String inputTopic = String.join("/", getBaselineTopic(), "request");

    @Getter(lazy = true)
    private final String newSessionTopic = String.join("/", getInputTopic(), "new");

    @Getter(lazy = true)
    private final String outputTopic = String.join("/", getBaselineTopic(), "response");

    @Getter(lazy = true)
    private final String dataBaselineTopic = String.join("/", getBaselineTopic(), "data");

    public String getDataServerPrefix() {
        return String.join("/", getDataBaselineTopic(), "server");
    }

    public String getDataServerTopic(String port) {
        return String.join("/", getDataServerPrefix(), port);
    }

    public String getDataClientTopic(String port) {
        return String.join("/", getDataClientPrefix(), port);
    }

    public String getDataClientPrefix() {
        return String.join("/", getDataBaselineTopic(), "client");
    }
}