package com.pjv.koranlu2.documenteditor.parser;

import android.util.Log;

import com.pjv.koranlu2.documenteditor.format.AttributeNode;
import com.pjv.koranlu2.documenteditor.format.TreeElement;
import com.pjv.koranlu2.documenteditor.format.TreeElementI;

import org.json.custom.JSONException;
import org.json.custom.JSONObject;
import org.json.custom.XML;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * Created by Luky on 11. 1. 2015.
 * Class providing XML parsing to TreeElement objects
 */
public class XMLParser {
    private static final String TAG = "XmlParser";
    private Document createdDoc;
    private File file;
    private Transformer transformer;

    public TreeElement parse() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document openedDoc = dBuilder.parse(file);

        TreeElement fakeRoot = new TreeElement(file.getName(), TreeElement.ROOT_ELEMENT);
        parseChildren(fakeRoot,openedDoc.getChildNodes());

        return fakeRoot;
    }

    private void recursiveXmlParse(Node node, TreeElementI parent) {
        if ((node.getFirstChild()!=null) && (node.getFirstChild().getNodeType() == Node.TEXT_NODE) && !node.getFirstChild().getNodeValue().replaceAll("\\s+", "").equals(""))
        {
            parent.addChild(new TreeElement(node.getFirstChild().getNodeValue().trim(),Node.TEXT_NODE));
        }

        NodeList nodeList = node.getChildNodes();
        parseChildren(parent, nodeList);
    }

    private void parseChildren(TreeElementI parent, NodeList children) {
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node currentNode = children.item(i);
            switch (currentNode.getNodeType()) {
                case Node.ELEMENT_NODE:
                    TreeElement currentElement = new TreeElement(currentNode.getNodeName(), Node.ELEMENT_NODE, currentNode.getAttributes());
                    parent.addChild(currentElement);
                    //calls this method for all the children which is Element
                    recursiveXmlParse(currentNode, currentElement);
                    break;
                case Node.COMMENT_NODE:
                    parent.addChild(new TreeElement(currentNode.getTextContent(), Node.COMMENT_NODE));
                    break;
                case Node.CDATA_SECTION_NODE:
                    parent.addChild(new TreeElement(currentNode.getTextContent(), Node.CDATA_SECTION_NODE));
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    parent.addChild(new TreeElement(currentNode.getNodeName() + " " + currentNode.getNodeValue(), Node.PROCESSING_INSTRUCTION_NODE));
                    break;
                case Node.DOCUMENT_TYPE_NODE:
                    parent.addChild(
                            new TreeElement(
                                    currentNode.getNodeName() + " " +
                                            ((((DocumentType) currentNode).getPublicId() == null) ? "SYSTEM " : ("PUBLIC " + ((DocumentType) currentNode).getSystemId() )) +
                                            ((((DocumentType) currentNode).getSystemId() == null) ? "" : (((DocumentType) currentNode).getSystemId() )),
                                    Node.DOCUMENT_TYPE_NODE));
                    break;
            }
        }
    }

    public boolean saveToFile(TreeElementI rootElement) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            createdDoc = docBuilder.newDocument();

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer(); //new transformer each time for thread safety
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //each tag on new line

            recursiveXmlCreate(rootElement.getChildList(),createdDoc);

            DOMSource sourceTree = new DOMSource(createdDoc);

           /* DEBUG SAVE to DIFFERENT File
           int lastIdx = file.getPath().lastIndexOf(".");
            lastIdx = (lastIdx!= -1) ? lastIdx : file.getPath().length();
            String newPath = file.getPath().subSequence(0,lastIdx).toString();
            Log.v(TAG, "Saving to path " + newPath);
            StreamResult result = new StreamResult(new File(newPath + "_new.xml"));*/
            StreamResult result = new StreamResult(file);

            transformer.transform(sourceTree, result);
            return true;
        } catch (ParserConfigurationException | TransformerException e) {
            Log.e(TAG,"Save error",e);
            return false;
        }
    }

    public boolean exportToJson(TreeElementI rootElement) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            createdDoc = docBuilder.newDocument();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer(); //new transformer each time for thread safety
            recursiveXmlCreate(rootElement.getChildList(),createdDoc);
            DOMSource sourceTree = new DOMSource(createdDoc);

            int lastIdx = (file.getPath().lastIndexOf(".")!=-1) ? file.getPath().lastIndexOf(".") : file.getPath().length();
            System.out.println(lastIdx);
            String newPath = file.getPath().subSequence(0,lastIdx).toString();
            Log.v(TAG, "Saving to path " + newPath);

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            transformer.transform(sourceTree, result);

            JSONObject jsonObject = XML.toJSONObject(writer.toString());

            PrintWriter out = new PrintWriter(newPath + ".json");
            out.write(jsonObject.toString(5));
            out.close();
            return !out.checkError();
        } catch (ParserConfigurationException | TransformerException | JSONException | FileNotFoundException e) {
            Log.e(TAG, "Export error", e);
            return false;
        }
    }


    private void recursiveXmlCreate(ArrayList<TreeElementI> childList, Node parent) {
    for (int i = childList.size() - 1; i >= 0; i--) {
        TreeElementI currentNode = childList.get(i);
        switch (currentNode.getNodeType()) {
            case Node.ELEMENT_NODE:
                Element newNode = createdDoc.createElement(currentNode.getNodeTitle());
                for (AttributeNode attributeNode : currentNode.getAttributes()) {
                    newNode.setAttribute(attributeNode.getNodeName(), attributeNode.getNodeValue());
                }
                //calls this method for all the children which is Element
                recursiveXmlCreate(currentNode.getChildList(), newNode);
                parent.appendChild(newNode);
                break;
            case Node.TEXT_NODE:
                parent.appendChild(createdDoc.createTextNode(currentNode.getNodeTitle()));
                //Log.v(TAG,"TEXT NODE");
                break;
            case Node.COMMENT_NODE:
                Node commentNode = createdDoc.createComment(currentNode.getNodeTitle());
                parent.appendChild(commentNode);
                // Log.v(TAG, "COMMENT NODE");
                break;
            case Node.CDATA_SECTION_NODE:
                Node cdataNode = createdDoc.createCDATASection(currentNode.getNodeTitle());
                parent.appendChild(cdataNode);
                // Log.v(TAG, "CDATA NODE");
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                int idx = currentNode.getNodeTitle().contains(" ") ? currentNode.getNodeTitle().indexOf(" ") : 0;
                String target = currentNode.getNodeTitle().substring(0, idx);
                Node procNode = createdDoc.createProcessingInstruction(target, currentNode.getNodeTitle().substring(idx));
                parent.appendChild(procNode);
                break;
            case Node.DOCUMENT_TYPE_NODE:
                DOMImplementation domImpl = createdDoc.getImplementation();
                String textContent = currentNode.getNodeTitle();
                try {
                    DocumentType doctype = domImpl.createDocumentType("doctype",
                            textContent.substring(
                                    textContent.substring(textContent.indexOf(' '))
                                            .indexOf(' '),
                                    textContent.lastIndexOf(' ')),
                            textContent.substring(currentNode.getNodeTitle().trim().lastIndexOf(' ') + 1));
                    if (currentNode.getNodeTitle().contains("SYSTEM"))
                        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
                    else
                        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
                } catch (IndexOutOfBoundsException ex) {
                    Log.i(TAG, "Doctype error, ignoring",ex);
                }
        }
    }

}


    public void setFile(File file) {
        this.file = file;
    }

}
