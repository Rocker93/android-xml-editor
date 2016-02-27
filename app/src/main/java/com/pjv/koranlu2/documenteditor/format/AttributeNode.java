package com.pjv.koranlu2.documenteditor.format;

import java.io.Serializable;

/**
 * Created by Luky on 14. 1. 2015.
 * Attribute Node representation
 */
public class AttributeNode implements Serializable {
    private String nodeName;
    private String nodeValue;

    public AttributeNode(String nodeName, String nodeValue) {
        this.nodeName = nodeName;
        this.nodeValue = nodeValue;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeValue() {
        return nodeValue;
    }

    public void setNodeValue(String nodeValue) {
        this.nodeValue = nodeValue;
    }

    @Override
    public String toString() {
        return nodeName + "=\"" + nodeValue + '\"';
    }
}
