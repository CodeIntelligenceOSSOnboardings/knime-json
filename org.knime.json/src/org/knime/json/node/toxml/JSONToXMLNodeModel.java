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
 *   14 Nov. 2014 (Gabor): created
 */
package org.knime.json.node.toxml;

import java.io.File;
import java.io.IOException;

import javax.json.JsonValue;
import javax.xml.parsers.ParserConfigurationException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.json.JSONValue;
import org.knime.core.data.json.JacksonConversions;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.json.node.util.SingleColumnReplaceOrAddNodeModel;
import org.knime.json.util.Json2Xml;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This is the model implementation of JSONToXML.
 *
 * @author Gabor Bakos
 */
public class JSONToXMLNodeModel extends SingleColumnReplaceOrAddNodeModel<JSONToXMLSettings> {
    /**
     * Constructor for the node model.
     */
    protected JSONToXMLNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // No internal state
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // No internal state
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // No internal state
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellFactory createCellFactory(final DataColumnSpec output, final int inputIndex,
        final int... otherColumns) {
        final JacksonConversions conv = JacksonConversions.getInstance();
        final Json2Xml converter = createConverter();

        //final XMLInputFactory fact = new InputFactoryProviderImpl().createInputFactory();
        //mapper.setSerializerProvider(new OutputFactoryProviderImpl().createOutputFactory());
        return new SingleCellFactory(output) {
            @Override
            public DataCell getCell(final DataRow row) {
                DataCell input = row.getCell(inputIndex);
                if (input instanceof JSONValue) {
                    JSONValue jsonValue = (JSONValue)input;
                    JsonValue json = jsonValue.getJsonValue();
                    TreeNode node = conv.toJackson(json);
                    DataCell xml = createXmlCell(node);
                    return xml;
                }
                return DataType.getMissingCell();
            }

            /**
             * @param node
             * @return
             */
            private DataCell createXmlCell(final TreeNode node) {
                try {
                    DataCell ret = XMLCellFactory.create(converter.toXml((JsonNode)node));
                    return ret;
                } catch (IllegalArgumentException | /*JsonProcessingException |*/ParserConfigurationException
                        | IOException e) {
                    return new MissingCell(e.getMessage());
                }
            }
        };
    }

    /**
     * @return
     */
    private Json2Xml createConverter() {
        Json2Xml ret = new Json2Xml();
        ret.setArrayPrefix(getSettings().getArray());
        ret.setBinary(getSettings().getBinary());
        ret.setBool(getSettings().getBoolean());
        ret.setInt(getSettings().getInteger());
        ret.setNamespace(getSettings().isSpecifyNamespace() ? getSettings().getNamespace() : null);
        ret.setNull(getSettings().getNull());
        ret.setReal(getSettings().getDecimal());
        ret.setRootName(getSettings().getRoot());
        ret.setText(getSettings().getString());
        ret.setLooseTypeInfo(getSettings().isOmitTypeInfo());
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataColumnSpec createOutputSpec(final String outputColName) {
        return new DataColumnSpecCreator(outputColName, XMLCell.TYPE).createSpec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSONToXMLSettings createSettings() {
        return createJSONToXMLSettings();
    }

    /**
     * @return
     */
    static JSONToXMLSettings createJSONToXMLSettings() {
        return new JSONToXMLSettings();
    }
}
