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
 *   Apr 9, 2018 (Tobias Urhaug): created
 */
package org.knime.core.data.json.servicetable;

import org.knime.core.node.util.CheckUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Representation of a Service Table containing Service Table Specs and Service Table Data.
 * Main function is to serve as an interface between JSON and BufferedDataTables.
 * Can be serialized/deserialized to/from json with jackson.
 *
 * @author Tobias Urhaug, KNIME GmbH, Berlin, Germany
 */
@JsonPropertyOrder({"table-spec", "table-data"})
public class ServiceTable {

    private final ServiceTableSpec m_tableSpec;
    private final ServiceTableData m_tableData;

    /**
     * Constructor for the service table.
     *
     * @param tableSpec the table spec for this table, not null
     * @param tableData the table data for this table, not null
     */
    public ServiceTable(
            @JsonProperty("table-spec") final ServiceTableSpec tableSpec,
            @JsonProperty("table-data") final ServiceTableData tableData) {
        m_tableSpec = CheckUtils.checkArgumentNotNull(tableSpec);
        m_tableData = CheckUtils.checkArgumentNotNull(tableData);
    }

    /**
     * Gets the table spec of this input.
     * @return the table spec, can not be null
     */
    @JsonProperty("table-spec")
    public ServiceTableSpec getServiceTableSpec() {
        return m_tableSpec;
    }

    /**
     * Gets the table data of this input.
     * @return the table data, can not be null
     */
    @JsonProperty("table-data")
    public ServiceTableData getServiceTableData() {
        return m_tableData;
    }

}
