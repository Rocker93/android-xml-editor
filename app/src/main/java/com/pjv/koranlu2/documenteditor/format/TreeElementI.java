package com.pjv.koranlu2.documenteditor.format;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Luky, based on Jilberta - 11. 11. 2013.
 * http://stackoverflow.com/questions/18717155/expandablelistview-like-treeview-android
 */
public interface TreeElementI extends Serializable {
    /**
     * Inserts child to the end of element's children
     * @param child
     */
    public void addChild(TreeElementI child);

    /**
     * Inserts child to the beginning of element's children
     * @param child
     */
    void addFirstChild(TreeElementI child);

    /**
     * Deletes given TreeElement from children, does nothing if child doesn't exist
     * @param childToDelete
     */
    public void deleteChild(TreeElementI childToDelete);

    /**
     * Creates and adds attribute as a child from given DOM Node
     * @param node W3C DOM Node
     */
    public void addAttribute(Node node);

    /**
     * @return attributes of current element, empty list if empty
     */
    public ArrayList<AttributeNode> getAttributes();

    /**
     * Updates current element with data from given element
     * @param updatedElement element with new data to be inserted
     */
    public void update(TreeElementI updatedElement);

    /**
     * Tests current node for text node existence
     * @return true for element with text content nodes, false otherwise
     */
    public boolean hasText();
    public String getNodeTitle();
    public void setNodeTitle(String outlineTitle);
    public boolean hasParent();
    public boolean hasChild();
    public int getLevel();
    public void setLevel(int level);
    public boolean isExpanded();
    public void setExpanded(boolean expanded);
    public ArrayList<TreeElementI> getChildList();
    public TreeElementI getParent();
    public void setParent(TreeElementI parent);
    public short getNodeType();
}