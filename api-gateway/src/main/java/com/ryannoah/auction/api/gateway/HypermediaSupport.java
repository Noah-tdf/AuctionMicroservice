package com.ryannoah.auction.api.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HypermediaSupport {

    private final ObjectMapper objectMapper;

    public HypermediaSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode addLinks(JsonNode body, Map<String, String> links) {
        ObjectNode objectNode = body.deepCopy();
        ObjectNode linksNode = objectMapper.createObjectNode();
        links.forEach((rel, href) -> linksNode.putObject(rel).put("href", href));
        objectNode.set("_links", linksNode);
        return objectNode;
    }

    public JsonNode wrapCollection(ArrayNode items, Map<String, String> links) {
        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.set("items", items);
        ObjectNode linksNode = objectMapper.createObjectNode();
        links.forEach((rel, href) -> linksNode.putObject(rel).put("href", href));
        wrapper.set("_links", linksNode);
        return wrapper;
    }
}
