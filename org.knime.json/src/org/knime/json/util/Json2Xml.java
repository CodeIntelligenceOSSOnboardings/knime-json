/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   7 Nov. 2014 (Gabor): created
 */
package org.knime.json.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.impl.util.Base64;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Helper class to convert Jackson {@link JsonNode} to XML {@link Document}s.
 *
 * @author Gabor Bakos
 * @since 2.11
 */
public class Json2Xml {
    /**
     * Some settings for {@link Json2Xml}.
     */
    public static class Json2XmlSettings {
        /**
         *
         */
        private String m_rootName;

        /**
         *
         */
        private String m_primitiveArrayItem;

        /**
         *
         */
        private String m_namespace;

        /**
         *
         */
        private String m_array;

        /**
         *
         */
        private String m_null;

        /**
         *
         */
        private String m_binary;

        /**
         *
         */
        private String m_text;

        /**
         *
         */
        private String m_real;

        /**
         *
         */
        private String m_int;

        /**
         *
         */
        private String m_bool;

        /**
         * {@code null} means we do not convert any keys to text.
         */
        private String m_textKey = "#text";

        private Map<JsonPrimitiveTypes, String> m_prefixes = new EnumMap<>(JsonPrimitiveTypes.class);

        /**
         *
         */
        public Json2XmlSettings() {
            this("root", "item", null, "Array", "null", "Binary", "Text", "Int", "Real", "Bool", "#text");
        }

        /**
         * @param rootName
         * @param primitiveArrayItem
         * @param namespace
         * @param array
         * @param nullPrefix
         * @param binary
         * @param text
         * @param real
         * @param intPrefix
         * @param bool
         * @param textKey
         */
        public Json2XmlSettings(final String rootName, final String primitiveArrayItem, final String namespace,
            final String array, final String nullPrefix, final String binary, final String text, final String real,
            final String intPrefix, final String bool, final String textKey) {
            this.m_rootName = rootName;
            this.m_primitiveArrayItem = primitiveArrayItem;
            this.m_namespace = namespace;
            this.m_array = array;
            this.m_null = nullPrefix;
            this.m_binary = binary;
            this.m_text = text;
            this.m_real = real;
            this.m_int = intPrefix;
            this.m_bool = bool;
            this.m_textKey = textKey;
            m_prefixes.put(JsonPrimitiveTypes.BINARY, m_binary);
            m_prefixes.put(JsonPrimitiveTypes.BOOLEAN, m_bool);
            m_prefixes.put(JsonPrimitiveTypes.FLOAT, m_real);
            m_prefixes.put(JsonPrimitiveTypes.INT, m_int);
            m_prefixes.put(JsonPrimitiveTypes.NULL, m_null);
            m_prefixes.put(JsonPrimitiveTypes.TEXT, m_text);
        }

        /**
         * @return the rootName
         */
        public final String getRootName() {
            return m_rootName;
        }

        /**
         * @param rootName the rootName to set
         */
        public final void setRootName(final String rootName) {
            this.m_rootName = rootName;
        }

        /**
         * @return the namespace
         */
        public final String getNamespace() {
            return m_namespace;
        }

        /**
         * @param namespace the namespace to set
         */
        public final void setNamespace(final String namespace) {
            this.m_namespace = namespace;
        }

        /**
         * @param array the array prefix name to set
         */
        public final void setArrayPrefix(final String array) {
            this.m_array = array;
        }

        /**
         * @param nullName the null element name to set
         */
        public final void setNull(final String nullName) {
            this.m_null = nullName;
            m_prefixes.put(JsonPrimitiveTypes.NULL, m_null);
        }

        /**
         * @param binary the binary element name to set
         */
        public final void setBinary(final String binary) {
            this.m_binary = binary;
            m_prefixes.put(JsonPrimitiveTypes.BINARY, m_binary);
        }

        /**
         * @param text the text element name to set
         */
        public final void setText(final String text) {
            this.m_text = text;
            m_prefixes.put(JsonPrimitiveTypes.TEXT, m_text);
        }

        /**
         * @param real the real element name to set
         */
        public final void setReal(final String real) {
            this.m_real = real;
            m_prefixes.put(JsonPrimitiveTypes.FLOAT, m_real);
        }

        /**
         * @param integer the integral element name to set
         */
        public final void setInt(final String integer) {
            this.m_int = integer;
            m_prefixes.put(JsonPrimitiveTypes.INT, m_int);
        }

        /**
         * @param bool the bool element name to set
         */
        public final void setBool(final String bool) {
            this.m_bool = bool;
            m_prefixes.put(JsonPrimitiveTypes.BOOLEAN, m_bool);
        }

        /**
         * @return the textKey
         */
        public final String getTextKey() {
            return m_textKey;
        }

        /**
         * @param textKey the m_textKey to set
         */
        public final void setTextKey(final String textKey) {
            this.m_textKey = textKey;
        }

        /**
         * @return the primitiveArrayItem
         */
        public String getPrimitiveArrayItem() {
            return m_primitiveArrayItem;
        }

        /**
         * @param primitiveArrayItem the primitiveArrayItem to set
         */
        public void setPrimitiveArrayItem(final String primitiveArrayItem) {
            this.m_primitiveArrayItem = primitiveArrayItem;
        }

        /**
         * @param type The type for the prefix name.
         * @return The set prefix name.
         */
        public String prefix(final JsonPrimitiveTypes type) {
            return m_prefixes.get(type);
        }

        /**
         * @param type The type for the namespace.
         * @return The namespace (default).
         */
        public String namespace(final JsonPrimitiveTypes type) {
            return type.getDefaultNamespace();
        }
    }

    /**
     *
     */
    public static final String LIST_NAMESPACE = "http://www.w3.org/2001/XMLSchema/list";

    /**
     *
     */
    public static final String STRING_NAMESPACE = "http://www.w3.org/2001/XMLSchema/string";

    /**
     *
     */
    public static final String NULL_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    /**
     *
     */
    public static final String INTEGER_NAMESPACE = "http://www.w3.org/2001/XMLSchema/integer";

    /**
     *
     */
    public static final String DECIMAL_NAMESPACE = "http://www.w3.org/2001/XMLSchema/decimal";

    /**
     *
     */
    public static final String BOOLEAN_NAMESPACE = "http://www.w3.org/2001/XMLSchema/boolean";

    /**
     *
     */
    public static final String BINARY_NAMESPACE = "http://www.w3.org/2001/XMLSchema/binary";

    private boolean m_looseTypeInfo = false;

    private Json2XmlSettings m_settings = new Json2XmlSettings("root", "item", null, "Array", "null", "Binary", "Text",
        "Real", "Int", "Bool", "#text");

    /**
     *
     */
    public Json2Xml() {
    }

    /**
     * @param settings The settings to use.
     *
     */
    public Json2Xml(final Json2XmlSettings settings) {
        this();
        m_settings = settings;
    }

    /**
     * Attributes of
     */
    public enum Options {
        /**
         * Loose the type information when we just use text for all types, else we use namespaces.
         */
        looseTypeInfo,
        /** Create object with parent key for each array element if possible without loosing ordering information. */
        UseParentKeyWhenPossible;
    }

    /**
     * Converts a {@link JsonNode} {@code node} to an XML {@link Document}.
     *
     * @param node A Jackson {@link JsonNode}.
     * @return The converted XML {@link Document}.
     * @throws ParserConfigurationException
     * @throws DOMException
     * @throws IOException
     */
    //TODO Exception normalization
    public Document toXml(final JsonNode node) throws ParserConfigurationException, DOMException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();
        doc.setStrictErrorChecking(false);
        EnumSet<JsonPrimitiveTypes> types = EnumSet.noneOf(JsonPrimitiveTypes.class);
        if (node.isArray() && node.size() == 0 && !m_looseTypeInfo) {
            doc.appendChild(m_settings.m_array == null ? doc.createElement(m_settings.m_rootName) : doc.createElementNS(LIST_NAMESPACE, m_settings.m_array + ":" + m_settings.m_rootName));
            doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + m_settings.m_array, LIST_NAMESPACE);
            setRootNamespace(doc.getDocumentElement());
            return doc;
        }
        doc.appendChild(m_settings.m_namespace == null ? createElement(doc, m_settings.m_rootName) : doc
            .createElementNS(m_settings.m_namespace, m_settings.m_rootName));
        doc.setDocumentURI(m_settings.m_namespace);
        Element root = doc.getDocumentElement();
        setRootNamespace(root);
        for (JsonPrimitiveTypes type : JsonPrimitiveTypes.values()) {
            root.setAttributeNS("http://www.w3.org/2000/xmlns/","xmlns:" + m_settings.prefix(type), m_settings.namespace(type));
        }
        create(null, null, node, doc.getDocumentElement(), types);
        for (JsonPrimitiveTypes type : EnumSet.complementOf(types)) {
            root.removeAttribute("xmlns:" + m_settings.prefix(type));
        }
        return doc;
    }

    /**
     * @param root
     */
    protected void setRootNamespace(final Element root) {
        if (m_settings.m_namespace != null) {
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:", m_settings.m_namespace);
        }
    }

    //    /**
    //     * @param origKey
    //     * @param parent
    //     * @param node
    //     * @param parentElement
    //     * @param types
    //     */
    //    private Element createWithParentKey(final String origKey, final JsonNode parent, final JsonNode node, final Element parentElement,
    //        final Set<JsonPrimitiveTypes> types) throws DOMException, IOException {
    //        if (node.isMissingNode()) {
    //            return parentElement;
    //        }
    //        Document doc = parentElement.getOwnerDocument();
    //        if (origKey == null) {
    //            return createNoKey(parent, node, parentElement, types);
    //        }
    //        //We have parent key, so we are within an object
    //        if (node.isArray()) {
    //            for (JsonNode jsonNode : node) {
    //                Element elem = doc.createElement(origKey);
    //                if (jsonNode.isObject()) {
    //                    parentElement.appendChild(createNoKey(node, jsonNode, elem, types));
    //                } else {
    //                    parentElement.appendChild(elem);
    //                    fix(jsonNode, elem, types);
    //                }
    //            }
    //            return parentElement;
    //        }
    //        if (node.isObject()) {
    //            Element elem = doc.createElement(origKey);
    //            parentElement.appendChild(elem);
    //            createSubObject(elem, (ObjectNode)node, types);
    //            return parentElement;
    //        }
    //        assert m_useParentKey : node;
    //        return fixValueNode(node, parentElement, types);
    //    }

    /**
     * @param node
     * @param element
     * @param types
     * @return
     */
    protected Element fixValueNode(final JsonNode node, final Element element, final Set<JsonPrimitiveTypes> types) {
        if (node.isValueNode()) {
            element.setTextContent(node.asText());
            if (!m_looseTypeInfo) {
                if (node.isBinary()) {
                    element.setPrefix(m_settings.m_binary);
                    types.add(JsonPrimitiveTypes.BINARY);
                } else if (node.isIntegralNumber()) {
                    element.setPrefix(m_settings.m_int);
                    types.add(JsonPrimitiveTypes.INT);
                } else if (node.isFloatingPointNumber()) {
                    element.setPrefix(m_settings.m_real);
                    types.add(JsonPrimitiveTypes.FLOAT);
                } else if (node.isBoolean()) {
                    element.setPrefix(m_settings.m_bool);
                    types.add(JsonPrimitiveTypes.BOOLEAN);
                } else if (node.isNull()) {
                    element.setPrefix(m_settings.m_null);
                    types.add(JsonPrimitiveTypes.NULL);
                } else if (node.isTextual()) {
                    //Wrong heuristic
                    //                    if (Base64.decode(element.getTextContent().getBytes(Charset.forName("UTF-8"))) == null) {
                    element.setPrefix(m_settings.m_text);
                    types.add(JsonPrimitiveTypes.TEXT);
                    //                    } else {
                    //                        element.setPrefix(m_settings.m_binary);
                    //                        types.add(JsonPrimitiveTypes.BINARY);
                    //                    }
                }
            }
            return element;
        }
        throw new IllegalStateException("Should not reach this! " + node);
    }

    /**
     * @param node
     * @param jsonNode
     * @param elem
     * @param types
     */
    protected void fix(final JsonNode child, final Element elem, final Set<JsonPrimitiveTypes> types) {
        if (child.isMissingNode()) {
            return;
        }
        fixValueNode(child, elem, types);
    }

    /**
     * @param origKey The parent's key that led to this call.
     * @param parent The parent node.
     * @param node The current node to convert.
     * @param parentElement The current element to transform or append attributes/children.
     * @param types The types used and should be declared as prefixes.
     * @return The created element.
     * @throws IOException
     * @throws DOMException
     */
    protected Element create(final String origKey, final JsonNode parent, final JsonNode node,
        final Element parentElement, final Set<JsonPrimitiveTypes> types) throws DOMException, IOException {
        if (node.isMissingNode()) {
            return parentElement;
        }
        Document doc = parentElement.getOwnerDocument();
        if (origKey == null) {
            return createNoKey(parent, node, parentElement, types);
        }
        //We have parent key, so we are within an object
        if (node.isArray()) {
            boolean hasValue = true;//hasValue(node)/* || conflictInAttributes((ArrayNode)node)*/;
            for (JsonNode jsonNode : node) {
                Element elem = createElement(doc, origKey);
                if (jsonNode.isObject()) {
                    if (hasValue) {
                        Element child = createItem(jsonNode, elem, hasValue, types);
                        if (!parentElement.hasChildNodes()) {
                            parentElement.appendChild(elem);
                        }
                        //                        elem.appendChild(child);
                        parentElement.getFirstChild().appendChild(child);
                    } else {
                        Element child = createNoKey(node, jsonNode, elem, types);
                        if (!parentElement.hasChildNodes()) {
                            parentElement.appendChild(elem);
                        }
                        if (child != parentElement.getFirstChild()) {
                            parentElement.getFirstChild().appendChild(child);
                        }
                    }
                } else {
                    parentElement.appendChild(createItem(jsonNode, elem, hasValue, types));
                }
            }
            return parentElement;
        }
        if (node.isObject()) {
            Element elem = createElement(doc, origKey);
            parentElement.appendChild(elem);
            createSubObject(elem, (ObjectNode)node, types);
            return parentElement;
        }
        if (node.isValueNode()) {
            parentElement.setTextContent(node.asText());
            return parentElement;
        }
        throw new IllegalStateException("Should not reach this! " + node);
    }

    /**
     * @param parent
     * @param node
     * @param parentElement
     * @param types
     * @return
     * @throws IOException
     */
    protected Element createNoKey(final JsonNode parent, final JsonNode node, final Element parentElement,
        final Set<JsonPrimitiveTypes> types) throws IOException {
        Document doc = parentElement.getOwnerDocument();
        //we are in the root, or in an array
        assert parent == null || parent.isArray() : parent;
        if (node.isValueNode()) {
            Element element = createItem(node, parentElement, false, types);
            parentElement.appendChild(element);
            return element;
        }
        if (node.isArray()) {
            boolean hasValue = hasValue(node);
            for (JsonNode child : node) {
                if (child.isObject() || child.isArray()) {
                    parentElement.appendChild(createItem(child, createElement(doc, m_settings.m_primitiveArrayItem),
                        hasValue, types));
                } else {
                    Element arrayItem = createItem(child, parentElement, hasValue, types);
                    parentElement.appendChild(arrayItem);
                }
            }
            return parentElement;
        }
        if (node.isObject()) {
            if (parent == null) {
                //First object
                return createObjectWithoutParent((ObjectNode)node, parentElement, types);
            }
            boolean hasValue = hasValue(parent);
            boolean conflictWithinAttributes = hasValue || conflictInAttributes((ArrayNode)parent);
            //object within array
            return createItem(node, parentElement, conflictWithinAttributes, types);
        }
        //We already handled the missing case and object.
        assert false : node;
        throw new IllegalStateException("Should not reach this! " + node);
    }

    /**
     * @param parent
     * @return
     */
    private boolean conflictInAttributes(final ArrayNode parent) {
        assert !hasValue(parent) : parent;
        boolean ret = false;
        if (parent.size() == 0) {
            return false;
        }
        Map<String, JsonNode> values;
        Iterator<JsonNode> it = parent.iterator();
        JsonNode obj = it.next();
        assert obj.isObject();
        if (obj instanceof ObjectNode) {
            ObjectNode on = (ObjectNode)obj;
            values = possibleAttributeTypesAndValues(on);
        } else {
            return true;
        }
        for (; it.hasNext();) {
            JsonNode next = it.next();
            if (!(next instanceof ObjectNode)) {
                return true;
            }
            Map<String, JsonNode> attributeTypes = possibleAttributeTypesAndValues((ObjectNode)next);
            Iterator<Entry<String, JsonNode>> it1 = values.entrySet().iterator();
            Iterator<Entry<String, JsonNode>> it2 = attributeTypes.entrySet().iterator();
            for (; it1.hasNext() && it2.hasNext();) {
                Entry<String, JsonNode> next1 = it1.next(), next2 = it2.next();
                if (!Objects.equals(next1.getKey(), next2.getKey())) {
                    return true;
                }
                if (!Objects.equals(next1.getValue(), next2.getValue())) {
                    return true;
                }
            }
            if (it1.hasNext() != it2.hasNext()) {
                return true;
            }
        }
        return ret;
    }

    /**
     * @param on
     * @return
     */
    protected Map<String, JsonNode> possibleAttributeTypesAndValues(final ObjectNode on) {
        Map<String, JsonNode> values = new LinkedHashMap<>();
        for (Iterator<Entry<String, JsonNode>> objIt = on.fields(); objIt.hasNext();) {
            Entry<String, JsonNode> keyValue = objIt.next();
            if (keyValue.getValue().isValueNode()) {
                values.put(keyValue.getKey(), keyValue.getValue());
            }
        }
        return values;
    }

    /**
     * @param parent
     * @return
     */
    private static boolean hasValue(final JsonNode parent) {
        boolean ret = false;
        for (JsonNode child : parent) {
            ret |= child.isValueNode();
        }
        return ret;
    }

    /**
     * @param parent
     * @return whether {@code parent} contains key with {@link #getTextKey()}.
     */
    protected boolean hasText(final ObjectNode parent) {
        String textKey = getTextKey();
        boolean ret = false;
        for (Iterator<String> it = parent.fieldNames(); it.hasNext();) {
            ret |= it.next().equals(textKey);
        }
        return ret;
    }

    /**
     * @param elem
     * @param fields
     * @param types
     * @return
     * @throws IOException
     * @throws DOMException
     */
    protected Element createSubObject(final Element elem, final ObjectNode objectNode,
        final Set<JsonPrimitiveTypes> types) throws DOMException, IOException {
        for (Iterator<Entry<String, JsonNode>> fields = objectNode.fields(); fields.hasNext();) {
            Entry<String, JsonNode> entry = fields.next();
            JsonNode node = entry.getValue();
            if (node.isValueNode()) {
                addValueAsAttribute(elem, entry, types);
            } else if (node.isObject()) {
                Element object = create(entry.getKey(), objectNode, node, elem, types);
                if (object == elem) {
                    continue;
                }
                elem.appendChild(object);
            } else if (node.isArray()) {
                Document document = elem.getOwnerDocument();
                Element elemBase = document.createElement(entry.getKey());
                elem.appendChild(elemBase);
                for (JsonNode jsonNode : node) {
                    //                    Element element = document.createElement(m_settings.m_primitiveArrayItem);
                    //                    elemBase.appendChild(element);
                    final Element created = create(null, node, jsonNode, elemBase, types);
                    if (created == elemBase) {
                        continue;
                    }
                    elemBase.appendChild(created);
                }
            }
        }
        return elem;
    }

    /**
     * @param node
     * @param element
     * @param types
     * @return
     * @throws IOException
     * @throws DOMException
     */
    protected Element createObjectWithoutParent(final ObjectNode node, final Element element,
        final Set<JsonPrimitiveTypes> types) throws DOMException, IOException {
        boolean hasTextKey = hasText(node);
        for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
            Entry<String, JsonNode> entry = it.next();
            JsonNode value = entry.getValue();
            if (value.isValueNode() && !hasTextKey) {
                addValueAsAttribute(element, entry, types);
            } else if (hasTextKey && value.isValueNode()) {
                if (entry.getKey().equals(getTextKey())) {
                    setTextContent(element, entry, types);
                } else {
                    addValueAsAttribute(element, entry, types);
//                    Document doc = element.getOwnerDocument();
//                    Element elem = createElement(doc, removeInvalidChars(entry.getKey()));
//                    element.appendChild(elem);
//                    create(entry.getKey(), node, value, elem, types);
                }
            } else if (value.isObject() || value.isArray()) {
                create(entry.getKey(), node, value, element, types);
            } else {
                assert false : value;
            }
        }
        return element;
    }

    /**
     * @param element
     * @param entry
     * @param types
     */
    protected void addValueAsAttribute(final Element element, final Entry<String, JsonNode> entry,
        final Set<JsonPrimitiveTypes> types) {
        JsonNode v = entry.getValue();
        if (v.isValueNode()) {
            String val = v.asText();
            String key = entry.getKey();
            if (key.equals(getTextKey())) {
                setTextContent(element, entry, types);
                return;
            }
            key = removeInvalidChars(key);
            if (m_looseTypeInfo) {
                element.setAttribute(key, val);
            } else if (v.isIntegralNumber()) {
                types.add(JsonPrimitiveTypes.INT);
                element.setAttribute(m_settings.m_int + ":" + key, val);
            } else if (v.isFloatingPointNumber()) {
                types.add(JsonPrimitiveTypes.FLOAT);
                element.setAttribute(m_settings.m_real + ":" + key, val);
            } else if (v.isBinary()) {
                types.add(JsonPrimitiveTypes.BINARY);
                element.setAttribute(m_settings.m_binary + ":" + key, val);
            } else if (v.isTextual()) {
                //This is a wrong heuristic, for example "text" is recognized as binary
                //                if (Base64.decode(val.getBytes(Charset.forName("UTF-8"))) == null) {
                types.add(JsonPrimitiveTypes.TEXT);
                element.setAttribute(m_settings.m_text + ":" + key, val);
                //                } else {
                //                types.add(JsonPrimitiveTypes.BINARY);
                //                element.setAttribute(m_settings.m_binary + ":" + key, val);
                //                }
            } else if (v.isNull()) {
                types.add(JsonPrimitiveTypes.NULL);
                element.setAttribute(m_settings.m_null + ":" + key, "");
            } else if (v.isBoolean()) {
                types.add(JsonPrimitiveTypes.BOOLEAN);
                element.setAttribute(m_settings.m_bool + ":" + key, val);
            } else {
                assert false : entry;
            }
        } else {
            assert false : v;
        }
    }

    /**
     * @param element
     * @param entry
     * @param types
     * @param val
     */
    protected void setTextContent(final Element element, final Entry<String, JsonNode> entry,
        final Set<JsonPrimitiveTypes> types) {
        JsonNode v = entry.getValue();
        String val = v.asText();
        element.appendChild(element.getOwnerDocument().createTextNode(val));
        if (!m_looseTypeInfo) {
            if (v.isIntegralNumber()) {
                types.add(JsonPrimitiveTypes.INT);
                element.setPrefix(m_settings.m_int);
            } else if (v.isFloatingPointNumber()) {
                types.add(JsonPrimitiveTypes.FLOAT);
                element.setPrefix(m_settings.m_real);
            } else if (v.isTextual()) {
                types.add(JsonPrimitiveTypes.TEXT);
                element.setPrefix(m_settings.m_text);
            } else if (v.isNull()) {
                types.add(JsonPrimitiveTypes.NULL);
                element.setPrefix(m_settings.m_null);
            } else if (v.isBinary()) {
                types.add(JsonPrimitiveTypes.BINARY);
                element.setPrefix(m_settings.m_binary);
            } else if (v.isBoolean()) {
                types.add(JsonPrimitiveTypes.BOOLEAN);
                element.setPrefix(m_settings.m_bool);
            } else {
                assert false : entry;
            }
        }
    }

    /**
     * @param key
     * @return
     */
    private static String removeInvalidChars(final String key) {
        return key.replaceAll("[^\\w]", "");
    }

    /**
     * @param node
     * @param element
     * @param forceItemElement
     * @param types
     * @return
     * @throws IOException
     * @throws DOMException
     */
    protected Element createItem(final JsonNode node, final Element element, final boolean forceItemElement,
        final Set<JsonPrimitiveTypes> types) throws DOMException, IOException {
        Document doc = element.getOwnerDocument();
        if (node.isValueNode()) {//We are inside an array, origKey should be null
            if (node.isBoolean()) {
                return createElementWithContent(m_settings.m_bool, JsonPrimitiveTypes.BOOLEAN,
                    Boolean.toString(node.asBoolean()), doc, types);
            }
            if (node.isIntegralNumber()) {
                return createElementWithContent(m_settings.m_int, JsonPrimitiveTypes.INT, node.bigIntegerValue()
                    .toString(), doc, types);
            }
            if (node.isFloatingPointNumber()) {
                return createElementWithContent(m_settings.m_real, JsonPrimitiveTypes.FLOAT,
                    Double.toString(node.asDouble()), doc, types);
            } else if (node.isBinary()) {
                return createElementWithContent(m_settings.m_binary, JsonPrimitiveTypes.BINARY,
                    new String(Base64.encode(node.binaryValue()), Charset.forName("UTF-8")), doc, types);
            } else if (node.isTextual()) {
                return createElementWithContent(m_settings.m_text, JsonPrimitiveTypes.TEXT, node.textValue(), doc,
                    types);
            } else if (node.isNull()) {
                return createElementWithContent(m_settings.m_null, JsonPrimitiveTypes.NULL, "", doc, types);
            } else {
                assert false : node;
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                Element arrayItem = createElement(doc, m_settings.m_primitiveArrayItem);
                Element elem = create(null, node, item, arrayItem, types);
                element.appendChild(elem);
            }
            return element;
        } else if (node.isObject()) {
            if (forceItemElement) {
                Element elem = createElement(doc, m_settings.m_primitiveArrayItem);
                return createObjectWithoutParent((ObjectNode)node, elem, types);
            }
            return createObjectWithoutParent((ObjectNode)node, element, types);
            //            Element arrayItem = doc.createElement(m_primitiveArrayItem);
            //            for (final Iterator<Entry<String, JsonNode>>it = node.fields(); it.hasNext();) {
            //                Entry<String, JsonNode> entry = it.next();
            //                arrayItem.appendChild(create(entry.getKey(), node, entry.getValue(), arrayItem, types));
            //            }
        }
        assert false : node;
        return null;
    }

    /**
     * @param prefix
     * @param type
     * @param content
     * @param doc
     * @param types
     * @return
     */
    private Element createElementWithContent(final String prefix, final JsonPrimitiveTypes type, final String content,
        final Document doc, final Set<JsonPrimitiveTypes> types) {
        String elementName = elementName(prefix, types, type);
        Element elem = doc.createElementNS(m_looseTypeInfo ? getNamespace() : type.getDefaultNamespace(), elementName);
        elem.setTextContent(content);
        return elem;
    }

    /**
     * @param prefix
     * @param ret
     * @param type
     * @return
     */
    private String elementName(final String prefix, final Set<JsonPrimitiveTypes> ret, final JsonPrimitiveTypes type) {
        if (m_looseTypeInfo) {
            return m_settings.m_primitiveArrayItem;
        }
        ret.add(type);
        return prefix == null || prefix.isEmpty() ? m_settings.m_primitiveArrayItem : prefix + ":"
            + m_settings.m_primitiveArrayItem;
    }

    /**
     * @return the looseTypeInfo
     */
    public final boolean isLooseTypeInfo() {
        return m_looseTypeInfo;
    }

    /**
     * @param looseTypeInfo the looseTypeInfo to set
     */
    public final void setLooseTypeInfo(final boolean looseTypeInfo) {
        this.m_looseTypeInfo = looseTypeInfo;
    }

    /**
     * @return the rootName
     */
    public final String getRootName() {
        return m_settings.m_rootName;
    }

    /**
     * @param rootName the rootName to set
     */
    public final void setRootName(final String rootName) {
        this.m_settings.m_rootName = rootName;
    }

    /**
     * @return the namespace
     */
    public final String getNamespace() {
        return m_settings.m_namespace;
    }

    /**
     * @param namespace the namespace to set
     */
    public final void setNamespace(final String namespace) {
        this.m_settings.m_namespace = namespace;
    }

    /**
     * @param array the array prefix name to set
     */
    public final void setArrayPrefix(final String array) {
        this.m_settings.m_array = array;
    }

    /**
     * @param nullName the null element name to set
     */
    public final void setNull(final String nullName) {
        this.m_settings.m_null = nullName;
    }

    /**
     * @param binary the binary element name to set
     */
    public final void setBinary(final String binary) {
        this.m_settings.m_binary = binary;
    }

    /**
     * @param text the text element name to set
     */
    public final void setText(final String text) {
        this.m_settings.m_text = text;
    }

    /**
     * @param real the real element name to set
     */
    public final void setReal(final String real) {
        this.m_settings.m_real = real;
    }

    /**
     * @param integer the integral element name to set
     */
    public final void setInt(final String integer) {
        this.m_settings.m_int = integer;
    }

    /**
     * @param bool the bool element name to set
     */
    public final void setBool(final String bool) {
        this.m_settings.m_bool = bool;
    }

    /**
     * Sets the boolean options in a single step.
     *
     * @param os The {@link Options}.
     */
    public void setOptions(final Options... os) {
        switch (os.length) {
            case 0:
                setOptions(EnumSet.noneOf(Options.class));
                break;
            case 1:
                setOptions(EnumSet.of(os[0]));
                break;
            default:
                Options[] rest = new Options[os.length - 1];
                System.arraycopy(os, 1, rest, 0, os.length - 1);
                setOptions(EnumSet.of(os[0], rest));
                break;
        }
    }

    /**
     * Sets the boolean options in a single step.
     *
     * @param os The {@link Options}.
     */
    public void setOptions(final Set<Options> os) {
        for (Options o : Options.values()) {
            switch (o) {
                case looseTypeInfo:
                    setLooseTypeInfo(os.contains(o));
                    break;
                case UseParentKeyWhenPossible:
                    if (os.contains(o)) {
                        throw new UnsupportedOperationException("Use the factory method to create such an object.");
                    }
                default:
                    break;
            }
        }
    }

    public static Json2Xml createWithUseParentKeyWhenPossible(final Json2XmlSettings settings) {
        return new Json2Xml(settings) {
            /**
             * @param origKey The parent's key that led to this call.
             * @param parent The parent node.
             * @param node The current node to convert.
             * @param parentElement The current element to transform or append attributes/children.
             * @return The created element.
             * @throws IOException
             * @throws DOMException
             */
            @Override
            protected Element create(final String origKey, final JsonNode parent, final JsonNode node,
                final Element parentElement, final Set<JsonPrimitiveTypes> types) throws DOMException, IOException {
                if (node.isMissingNode()) {
                    return parentElement;
                }
                Document doc = parentElement.getOwnerDocument();
                if (origKey == null) {
                    return createNoKey(parent, node, parentElement, types);
                }
                //We have parent key, so we are within an object
                if (node.isArray()) {
                    boolean hasValue = hasValue(node);
                    for (JsonNode jsonNode : node) {
                        Element elem = createElement(doc, origKey);
                        if (jsonNode.isObject()) {
                            parentElement.appendChild(hasValue ? createItem(jsonNode, elem, hasValue, types)
                                : createNoKey(node, jsonNode, elem, types));
                        } else {
                            parentElement.appendChild(createNoKey(node, jsonNode, elem, types));
                        }
                    }
                    return parentElement;
                }
                if (node.isObject()) {
                    Element elem = createElement(doc, origKey);
                    parentElement.appendChild(elem);
                    createSubObject(elem, (ObjectNode)node, types);
                    return parentElement;
                }
                if (node.isValueNode()) {
                    parentElement.appendChild(doc.createTextNode(node.asText()));
                    return parentElement;
                }
                throw new IllegalStateException("Should not reach this! " + node);
            }

            /**
             * @param elem
             * @param objectNode
             * @param types
             * @return
             * @throws IOException
             * @throws DOMException
             */
            @Override
            protected Element createSubObject(final Element elem, final ObjectNode objectNode,
                final Set<JsonPrimitiveTypes> types) throws DOMException, IOException {
                boolean hasTextKey = false;
                String textKey = getTextKey();
                for (Iterator<String> nameIt = objectNode.fieldNames(); nameIt.hasNext();) {
                    String name = nameIt.next();
                    hasTextKey |= name.equals(textKey);
                }
                for (Iterator<Entry<String, JsonNode>> fields = objectNode.fields(); fields.hasNext();) {
                    Entry<String, JsonNode> entry = fields.next();
                    JsonNode node = entry.getValue();
                    if (node.isValueNode()) {
                        if (!hasTextKey) {
                            addValueAsAttribute(elem, entry, types);
                        } else {
                            if (entry.getKey().equals(textKey)) {
                                elem.appendChild(elem.getOwnerDocument().createTextNode(node.asText()));
                            } else {
                                Element object = create(entry.getKey(), objectNode, node, elem, types);
                                if (object == elem) {
                                    continue;
                                }
                                elem.appendChild(object);
                            }
                        }
                    } else if (node.isObject()) {
                        Element object = create(entry.getKey(), objectNode, node, elem, types);
                        if (object == elem) {
                            continue;
                        }
                        elem.appendChild(object);
                    } else if (node.isArray()) {
                        Document document = elem.getOwnerDocument();
                        for (JsonNode jsonNode : node) {
                            Element element = createElement(document, entry.getKey());
                            elem.appendChild(element);
                            if (jsonNode.isObject()) {
                                Element created = createObjectWithoutParent((ObjectNode)jsonNode, element, types);
                                if (created == element) {
                                    continue;
                                }
                            } else if (jsonNode.isArray()) {
                                create(null, node, jsonNode, element, types);
                            } else {
                                fix(jsonNode, element, types);
                            }
                        }
                    }
                }
                return elem;
            }

            @Override
            protected Element createNoKey(final JsonNode parent, final JsonNode node, final Element parentElement,
                final Set<JsonPrimitiveTypes> types) throws IOException {
                Document doc = parentElement.getOwnerDocument();
                //we are in the root, or in an array
                assert parent == null || parent.isArray() : parent;
                if (node.isValueNode()) {
                    Element element = createItem(node, parentElement, false, types);
                    parentElement.appendChild(element);
                    return element;
                }
                if (node.isArray()) {
                    boolean hasValue = hasValue(node);
                    for (JsonNode child : node) {
                        if (child.isObject() || child.isArray()) {
                            parentElement.appendChild(createItem(child, createElement(doc, getPrimitiveArrayItem()),
                                hasValue, types));
                        } else {
                            Element arrayItem = createItem(child, parentElement, hasValue, types);
                            parentElement.appendChild(arrayItem);
                        }
                    }
                    return parentElement;
                }
                if (node.isObject()) {
                    if (parent == null) {
                        //First object
                        return createObjectWithoutParent((ObjectNode)node, parentElement, types);
                    }
                    boolean hasValue = hasValue(parent);
                    //object within array
                    return createItem(node, parentElement, hasValue, types);
                }
                //We already handled the missing case and object.
                assert false : node;
                throw new IllegalStateException("Should not reach this! " + node);
            }
        };
    }

    /**
     * @return The key values to be translated as text.
     */
    protected String getTextKey() {
        return m_settings.getTextKey();
    }

    /**
     * @return The element name returned for array items.
     */
    protected String getPrimitiveArrayItem() {
        return m_settings.getPrimitiveArrayItem();
    }

    /**
     * @param doc The owner document.
     * @param name The name of the {@link Element}.
     * @return The {@link Element} with proper namespace.
     */
    protected Element createElement(final Document doc, final String name) {
        return doc.createElementNS(m_settings.m_namespace, name);
    }
}
