package org.knime.json.fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.core.data.DataCellDataInput;
import org.junit.jupiter.api.BeforeAll;
import java.io.StringReader;
import org.xml.sax.InputSource;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataInput;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.osgi.framework.BundleContext;
import org.mockito.Mockito;
import org.knime.ext.textprocessing.util.TermDocumentDeSerializationUtil;
import org.junit.jupiter.api.BeforeAll;

public class Fuzzer {
    static InternalPlatform internalPlatformMock;
    static BundleContext bundleContextMock;

    @BeforeAll
    static void setUp() {

    }

    @FuzzTest
    void myFuzzTest(FuzzedDataProvider data) {
        String m_inputJson = data.consumeRemainingAsString();
        InputStream inputStream = new ByteArrayInputStream(m_inputJson.getBytes(Charset.forName("UTF-8")));

        internalPlatformMock = Mockito.mock(InternalPlatform.class);
        bundleContextMock = Mockito.mock(BundleContext.class);
        Mockito.when(internalPlatformMock.getBundleContext()).thenReturn(bundleContextMock);
        try {
            TermDocumentDeSerializationUtil.deserializeDocument(inputStream);
        } catch (IOException e) {
        }


//        DataInput input = new DataInputStream(new ByteArrayInputStream(data.consumeRemainingAsBytes()));
//        DocumentCell dc = new DocumentCell();
//        dc.DocumentSerializer.deserialize((DataCellDataInput)input);
    }
}
