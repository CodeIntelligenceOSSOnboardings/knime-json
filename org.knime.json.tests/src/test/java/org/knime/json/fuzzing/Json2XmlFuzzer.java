package org.knime.json.fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.io.IOException;
import java.io.StringWriter;
import java.io.StringReader;
import org.xml.sax.InputSource;
import java.util.ArrayList;
import java.util.List;
import org.knime.json.util.Json2Xml;
import org.knime.json.util.Xml2Json;
import org.knime.json.util.Json2Xml.Json2XmlSettings;
import org.knime.json.util.Json2Xml.Options;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.xml.sax.SAXException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import java.util.Optional;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataValue;
import org.knime.core.data.convert.datacell.JavaToDataCellConverter;
import org.knime.core.data.convert.datacell.JavaToDataCellConverterFactory;
import org.knime.core.data.convert.datacell.JavaToDataCellConverterRegistry;
import org.knime.core.data.convert.java.DataCellToJavaConverter;
import org.knime.core.data.convert.java.DataCellToJavaConverterFactory;
import org.knime.core.data.convert.java.DataCellToJavaConverterRegistry;
import org.knime.core.node.ExecutionContext;
import org.knime.core.util.JsonUtil;
import org.knime.core.data.json.JacksonConversions;
import org.knime.core.data.json.JSONCell;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.osgi.framework.BundleContext;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.mockito.Mockito;

public class Json2XmlFuzzer {
    static Json2Xml m_converter;
    static Xml2Json m_converter2;
    static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    static DocumentBuilder builder;
    static JacksonConversions m_conversions;

    static InternalPlatform internalPlatformMock;

    @BeforeAll
    static void setUp() throws Exception {
        m_converter = new Json2Xml();

        m_converter2 = new Xml2Json().setTranslateComments(true);
        m_converter2.setSimpleAttributes(false);
        builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new MyErrorHandler());

        m_conversions = JacksonConversions.getInstance();
    }

    @FuzzTest
    void myFuzzTest(FuzzedDataProvider data) {
        String str = data.consumeString(200);
        String m_inputJson = data.consumeRemainingAsString();

        try {
//            JsonNode json = new ObjectMapper().readTree(m_inputJson);
//            Document doc = m_converter.toXml(json);
//            TransformerFactory tf = TransformerFactory.newInstance();
//            Transformer transformer = tf.newTransformer();
//            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//            StringWriter writer = new StringWriter();
//            transformer.transform(new DOMSource(doc), new StreamResult(writer));
//            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");

//            Json2XmlSettings settings = new Json2XmlSettings();
//            settings.setNamespace(str);
//            Json2Xml json2Xml = Json2Xml.createWithUseParentKeyWhenPossible(settings);
//            json2Xml.setOptions(new Options[0]);
//            m_converter.toXml(new TextNode(m_inputJson));

            Document doc = builder.parse( new InputSource( new StringReader( m_inputJson ) ) );
            m_converter2.toJson(doc);
        } catch (IOException | SAXException e) { // TransformerException
        }

//        internalPlatformMock = Mockito.mock(InternalPlatform.class);
//        Mockito.when(internalPlatformMock.getOS()).thenReturn("osgi.os");
//        JsonObject obj = JsonUtil.getProvider().createObjectBuilder().add(str, m_inputJson).build();
//        Optional<JavaToDataCellConverterFactory<JsonObject>> factory = JavaToDataCellConverterRegistry.getInstance().getConverterFactories(JsonObject.class, JSONCell.TYPE).stream().findFirst();
//        JavaToDataCellConverter<JsonObject> converter = factory.get().create((ExecutionContext)null);
//        try {
//            DataCell cell = converter.convert(obj);
//        } catch (Exception e) {
//        }
//
//        JsonValue input = JsonUtil.getProvider().createObjectBuilder().add(str, m_inputJson).build();
//        JsonNode node = m_conversions.toJackson(input);


    }
}

class MyErrorHandler implements ErrorHandler {
    public void error(SAXParseException e) {}
    public void fatalError(SAXParseException e) {}
    public void warning(SAXParseException e) {}
}