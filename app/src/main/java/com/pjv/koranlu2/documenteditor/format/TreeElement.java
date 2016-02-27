package com.pjv.koranlu2.documenteditor.format;

import android.widget.Toast;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.ArrayList;

/**
 * Created by Luky on 11. 1. 2015.
 * Based on Jiberta http://stackoverflow.com/questions/18717155/expandablelistview-like-treeview-android
 */
public class TreeElement implements TreeElementI {
    public static final short ROOT_ELEMENT = -1;
    private String nodeTitle;
    private boolean hasParent;
    private boolean hasChild;
    private TreeElementI parent;
    private int level;
    private ArrayList<TreeElementI> childList;
    private boolean expanded;
    private short nodeType;
    private ArrayList<AttributeNode> attributeNodes = new ArrayList<>();

    public TreeElement(String nodeTitle) {
        this.childList = new ArrayList<>();
        this.nodeTitle = nodeTitle;
        this.level = 0;
        this.hasParent = true;
        this.hasChild = false;
        this.parent = null;
    }

    public TreeElement(String nodeTitle, short nodeType) {
        this.childList = new ArrayList<>();
        this.nodeTitle = nodeTitle;
        this.level = 0;
        this.hasParent = true;
        this.hasChild = false;
        this.parent = null;
        this.nodeType = nodeType;
    }

    public TreeElement(String nodeTitle, short nodeType, NamedNodeMap attributeNodes) {
        this.childList = new ArrayList<>();
        this.nodeTitle = nodeTitle;
        this.level = 0;
        this.hasParent = true;
        this.hasChild = false;
        this.parent = null;
        this.nodeType = nodeType;
        for (int i = 0; i < attributeNodes.getLength(); i++) {
            this.attributeNodes.add(new AttributeNode(attributeNodes.item(i).getNodeName(), attributeNodes.item(i).getNodeValue()));
        }
    }

    public TreeElement(String nodeTitle, boolean hasParent, boolean hasChild, TreeElement parent, int level, boolean expanded) {
        this.childList = new ArrayList<>();
        this.nodeTitle = nodeTitle;
        this.hasParent = hasParent;
        this.hasChild = hasChild;
        this.parent = parent;
        if (parent != null) {
            this.parent.getChildList().add(this);
        }
        this.level = level;
        this.expanded = expanded;
    }

    @Override
    public void addChild(TreeElementI child) {
        this.getChildList().add(child);
        this.hasParent = true;
        this.hasChild = true;
        child.setParent(this);
        child.setLevel(this.getLevel() + 1);
    }

    @Override
    public void addFirstChild(TreeElementI child) {
        this.getChildList().add(0,child);
        this.hasParent = true;
        this.hasChild = true;
        child.setParent(this);
        child.setLevel(this.getLevel() + 1);
    }


    @Override
    public void deleteChild(TreeElementI childToDelete) {
        childList.remove(childToDelete);
        if (childList.size()==0)  {
            this.hasChild = false;
            setExpanded(false);
        }
    }

    @Override
    public String getNodeTitle() {
        return this.nodeTitle;
    }

    @Override
    public void setNodeTitle(String nodeTitle) {
        this.nodeTitle = nodeTitle;
    }

    @Override
    public boolean hasParent() {
        return this.hasParent;
    }

    @Override
    public boolean hasChild() {
        return this.hasChild;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public boolean isExpanded() {
        return this.expanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public ArrayList<TreeElementI> getChildList() {
        return this.childList;
    }

    @Override
    public TreeElementI getParent() {
        return this.parent;
    }

    @Override
    public void setParent(TreeElementI parent) {
        this.parent = parent;
        this.hasParent = true;
    }

    public short getNodeType() {
        return nodeType;
    }

    @Override
    public void addAttribute(Node node) {
        this.attributeNodes.add(new AttributeNode(node.getNodeName(),node.getNodeValue()));
    }

    @Override
    public ArrayList<AttributeNode> getAttributes() {
        return this.attributeNodes;
    }

    @Override
    public void update(TreeElementI updatedElement) {
        this.nodeTitle = updatedElement.getNodeTitle();
        this.attributeNodes = updatedElement.getAttributes();
    }

    @Override
    public boolean hasText() {
        for (TreeElementI element : childList) {
            if (element.getNodeType() == Node.TEXT_NODE)
                return true;
        }
        return false;
    }

}