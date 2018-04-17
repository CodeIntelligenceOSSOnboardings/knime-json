/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   Apr 17, 2018 (Tobias Urhaug): created
 */
package org.knime.json.node.servicein;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.time.localdate.LocalDateCellFactory;
import org.knime.core.node.InvalidSettingsException;

/**
 * Enum holding all valid data types for the service in node.
 * A valid data type contains its type and a parser that parses a given object if possible.
 *
 * @author Tobias Urhaug
 */
public enum ServiceInputValidDataType {

    /**
     *
     */
    STRING(StringCell.TYPE, new ServiceInputStringParser()),
    /**
     *
     */
    INTEGER(IntCell.TYPE, new ServiceInputIntegerParser()),
    /**
     *
     */
    DOUBLE(DoubleCell.TYPE, new ServiceInputDoubleParser()),
    /**
     *
     */
    LOCAL_DATE(LocalDateCellFactory.TYPE, new ServiceInputLocalDateParser());


    private final DataType m_dataType;
    private final ServiceInputCellParser m_objectParser;

    /**
     * Constructor for the data types.
     *
     * @param m_dataType
     * @param m_objectParser
     */
    private ServiceInputValidDataType(final DataType dataType, final ServiceInputCellParser objectParser) {
        this.m_dataType = dataType;
        this.m_objectParser = objectParser;
    }

    /**
     * Returns the data type of the concrete instance.
     *
     * @return the data type of the concrete instance
     */
    public DataType getDataType() {
        return m_dataType;
    }

    /**
     * Parses the input object to a data cell of the concrete instance.
     *
     * @param cellObject
     * @return DataCell parsed data cell of the input object
     * @throws InvalidSettingsException if the input object cannot be parsed
     */
    public DataCell parseObject(final Object cellObject) throws InvalidSettingsException {
        return m_objectParser.parse(cellObject);
    }

}
