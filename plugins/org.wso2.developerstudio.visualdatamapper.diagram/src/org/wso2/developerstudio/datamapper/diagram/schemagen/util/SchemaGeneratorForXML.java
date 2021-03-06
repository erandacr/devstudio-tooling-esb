/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.developerstudio.datamapper.diagram.schemagen.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;


public class SchemaGeneratorForXML extends SchemaGeneratorForJSON implements ISchemaGenerator {
	private static final String TEMP_AVRO_GEN_LOCATION = "tempXSDGenLocation";
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	private static final String TEMP_OUTPUT = System.getProperty(JAVA_IO_TMPDIR) + File.separator + TEMP_AVRO_GEN_LOCATION;
	private static final String CONTENT_TAG = "#@content";
	protected static final String AT_PREFIX = "@";
	protected static final String DOLLLAR_AT_PREFIX = "$@";
	protected static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";
	protected static final String XSI_TYPE = "type";
	
	
	@Override
	public String getSchemaContent(String content) throws IOException {

		JSONObject xmlJSONObj;
		try {
			xmlJSONObj = XML.toJSONObject(content);
		} catch (JSONException e) {
			throw new IOException(e.getMessage());
		}

		return super.getSchemaContent(xmlJSONObj.toString());
	}


	/*
	private File generateXSDfromXML(String filePath) throws IOException, XmlException {
		
		File outputDirectory = new File(TEMP_OUTPUT);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}
		File outputFile = new File(TEMP_OUTPUT  + File.separator + "temp.xsd");
		if (outputFile.exists()) {
			outputFile.delete();
		}
		
	    Inst2XsdOptions options = new Inst2XsdOptions();
        options.setDesign(Inst2XsdOptions.DESIGN_RUSSIAN_DOLL);
        options.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        options.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        Reader[] xmlInstance = {new BufferedReader(new FileReader(filePath))};
        SchemaDocument[] xsds = org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd.inst2xsd(xmlInstance, options);
		if(xsds != null && xsds.length > 0) {
			xsds[0].save(outputFile, new  XmlOptions().setSavePrettyPrint());
		}
		 
		return outputFile;
	}
	*/

	@Override
	public String getSchemaResourcePath(String filePath) throws IOException {
		String entireFileText = FileUtils.readFileToString(new File(filePath));
		entireFileText = replaceAttributesWithElements(entireFileText);
		return getSchemaContent(entireFileText);
	}

	private String replaceAttributesWithElements(String entireFileText) throws IOException {
		try {
			OMElement element = AXIOMUtil.stringToOM(entireFileText);
			OMFactory factory = OMAbstractFactory.getOMFactory();
			Map<String,String> namespaces = new HashMap<>();
			element = traverseChildrenAndReplaceAttributes(element, factory,namespaces);
			for (Map.Entry<String, String> entry : namespaces.entrySet())
			{
				String prefix = entry.getKey();
				String uri = entry.getValue();
				element.declareNamespace(uri, prefix);
			}
            
			entireFileText = element.toString();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
		return entireFileText;
	}

	private OMElement traverseChildrenAndReplaceAttributes(OMElement element, OMFactory factory, Map<String, String> namespaces) {

		
		Iterator attributeIterator = element.getAllAttributes();
		List<OMAttribute> removeList = new ArrayList<OMAttribute>();
		List<OMElement> addList = new ArrayList<OMElement>();
		String modifiedElementName = null;
			
		while (attributeIterator.hasNext()) {
			OMAttribute atttrib = (OMAttribute) attributeIterator.next();
			OMElement attributeElement = null;
			//If the attribute is an element handler append the prefix to the name
			if(atttrib.getNamespace() != null){
				QName qName = atttrib.getQName();
				//construct a modified element name when we have xsi:type attribute in the element. 
				if (XSI_NAMESPACE_URI.equals(qName.getNamespaceURI()) && XSI_TYPE.equals(qName.getLocalPart())) {
					modifiedElementName = element.getQName().getLocalPart() + "_" + DOLLLAR_AT_PREFIX
							+ qName.getPrefix() + ":" + qName.getLocalPart() + "_" + atttrib.getAttributeValue();
				}
				String prefix = atttrib.getNamespace().getPrefix();
				attributeElement = factory.createOMElement(AT_PREFIX+ prefix+":"+atttrib.getLocalName(), null);
			}else{
				//remove attribute and instead add a element with @ infront
				// eg <person age="30"></person> will be replaced by <person><@age>30</@age></person>
				attributeElement = factory.createOMElement(AT_PREFIX+ atttrib.getLocalName(), null);
			}
			
			OMText attributeValue = factory.createOMText(atttrib.getAttributeValue());
			attributeElement.addChild(attributeValue);
			// add to list and later remove
			addList.add(attributeElement);
			removeList.add(atttrib);
		}
		
		for (OMAttribute arttribute : removeList ) {
			element.removeAttribute(arttribute);
		}

		if (addList.size() > 0) {
			//remove the inner text and replace it with <#@content></#@content>
			//eg <city listed="true">colombo</city> will be replaced by <city><@listed>true</@listed><#@content>colombo</#@content></city>
			String elementText = element.getText();
			if (!elementText.isEmpty()) {
				element.setText("");
				OMElement contentElement = factory.createOMElement(CONTENT_TAG, null);
				OMText contentValue = factory.createOMText(elementText);
				contentElement.addChild(contentValue);
				element.addChild(contentElement);
			}
			for (OMElement attrElement: addList ) {
				element.addChild(attrElement);
			}
		}
		
	
		Iterator elementIterator = element.getChildren();
		while (elementIterator.hasNext()) {
			OMNode child = (OMNode) elementIterator.next();
			if (child instanceof OMElement) {
				traverseChildrenAndReplaceAttributes((OMElement) child, factory,namespaces);
			}
		}
		
		//Iterate over namespaces and add to a map
		Iterator iter = element.getAllDeclaredNamespaces();
        while (iter.hasNext()) {
            OMNamespace ns = (OMNamespace) iter.next();
            String prefix = ns.getPrefix();
            String uri = ns.getNamespaceURI();
            if(!namespaces.containsKey(prefix)){
            	namespaces.put(prefix, uri);
            }
        }
		
		if(modifiedElementName != null){
			element.setLocalName(modifiedElementName);
		}
		
		return element;
	}
}
