/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import org.h2.tools.TriggerAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Utility class for updating the `updated_at` column in an H2 database.
 * This class is specifically required for H2 databases, which do not support
 * automatic timestamp updates natively through SQL or configuration.
 * <p>
 * In more advanced databases such as PostgreSQL, this utility class is not
 * necessary as PostgreSQL supports automatic updates of timestamp columns
 * using triggers or the `DEFAULT` keyword with expressions.
 */
@SuppressWarnings("unused")
public class UpdateTimestampTrigger extends TriggerAdapter {
    @Override
    public void fire(final Connection conn, final ResultSet oldRow, final ResultSet newRow) throws SQLException {
        newRow.updateTimestamp("updated_at", new Timestamp(System.currentTimeMillis()));
    }
}
