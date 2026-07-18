package com.aw.common.config.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

@MappedTypes(UUID.class)
public class UuidTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        // Điểm mấu chốt: Bắt buộc dùng Types.OTHER để PostgreSQL tự convert sang kiểu native uuid
        ps.setObject(i, parameter, Types.OTHER);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toUUID(rs.getObject(columnName));
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toUUID(rs.getObject(columnIndex));
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toUUID(cs.getObject(columnIndex));
    }

    /**
     * Hàm helper để parse dữ liệu an toàn từ Database lên Java Object
     */
    private UUID toUUID(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof UUID) {
            return (UUID) val;
        }
        if (val instanceof String) {
            return UUID.fromString((String) val);
        }
        throw new IllegalArgumentException("Không thể ép kiểu dữ liệu này sang UUID: " + val.getClass());
    }
}
