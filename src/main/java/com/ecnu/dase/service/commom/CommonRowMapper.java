package com.ecnu.dase.service.commom;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 根据查询结果，抽取相应字段，转换为Map结构的对象
 * @author jgz
 */
@SuppressWarnings("rawtypes")
public abstract class CommonRowMapper implements RowMapper {

    CommonRowMapper parent;

    protected SimpleDateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    protected Map<String, String> stringFields = new HashMap<String, String>();

    protected Map<String, String> shortFields = new HashMap<String, String>();

    protected Map<String, String> longFields = new HashMap<String, String>();

    protected Map<String, String> dateFields = new HashMap<String, String>();

    protected Map<String, String> timestampFields = new HashMap<String, String>();

    protected Map<String, String> boolFields = new HashMap<String, String>();

    protected Map<String, String> booleanFields = new HashMap<String, String>();

    protected Map<String, String> doubleFields = new HashMap<String, String>();

    protected Map<String, String> byteFields = new HashMap<String, String>();

    protected Map<String, String> intFields = new HashMap<String, String>();

    public CommonRowMapper(CommonRowMapper parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> ret = new HashMap<String, Object>();
        if (parent != null)
            ret = (Map<String, Object>) parent.mapRow(rs, rowNum);

        processStringField(ret, rs);
        processLongField(ret, rs);
        processDateField(ret, rs);
        processTimestampField(ret, rs);
        processBooleanField(ret, rs);
        processBoolField(ret, rs);
        processDoubleField(ret, rs);
        processByteField(ret, rs);
        processShortField(ret, rs);
        processIntegerField(ret, rs);

        return ret;
    }

    public void processDateField(Map<String, Object> data, ResultSet set) {
        dateFields.forEach((key, value) -> {
            try {
                if (set.getDate(value.toString()) != null)
                    data.put(key, formatter
                            .format((set.getDate(value.toString()).getTime())));
                else
                    data.put(key, "");
            } catch (SQLException ex) {
            }
        });
    }

    public void processTimestampField(Map<String, Object> data, ResultSet set) {
        timestampFields.forEach((key, value) -> {
            try {
                if (set.getTimestamp(value.toString()) != null)
                    data.put(key, formatter
                            .format((set.getTimestamp(value.toString())
                                    .getTime())));
            } catch (SQLException ex) {
            }
        });
    }

    public void processBooleanField(Map<String, Object> data, ResultSet set) {
        boolFields.forEach((key, value) -> {
            try {
                data.put(key, set.getBoolean(value.toString()) ? "是" : "否");
            } catch (SQLException ex) {
            }
        });
    }

    public void processBoolField(Map<String, Object> data, ResultSet set) {
        booleanFields.forEach((key, value) -> {
            try {
                data.put(key, set.getBoolean(value.toString()) ? true : false);
            } catch (SQLException ex) {
            }
        });
    }

    public void processStringField(Map<String, Object> data, ResultSet set) {
        stringFields.forEach((key, value) -> {
            try {
                String val = set.getString(value.toString());
                if (!set.wasNull())
                    data.put(key, val);
            } catch (SQLException ex) {
            }
        });
    }

    public void processShortField(Map<String, Object> data, ResultSet set) {
        shortFields.forEach((key, value) -> {
            try {
                short val = set.getShort(value.toString());
                if (!set.wasNull())
                    data.put(key, val);
            } catch (SQLException ex) {
            }
        });
    }

    public void processLongField(Map<String, Object> data, ResultSet set) {
        longFields.forEach((key, value) -> {
            try {
                long val = set.getLong(value.toString());
                if (!set.wasNull())
                    data.put(key, val);
            } catch (SQLException ex) {
            }
        });
    }

    public void processDoubleField(Map<String, Object> data, ResultSet set) {
        doubleFields.forEach((key, value) -> {
            try {
                double val = set.getDouble(value.toString());
                if (!set.wasNull())
                    data.put(key, val);
            } catch (SQLException ex) {
            }
        });
    }

    public void processByteField(Map<String, Object> data, ResultSet set) {
        byteFields.forEach((key, value) -> {
            try {
                byte val = set.getByte(value.toString());
                if (!set.wasNull())
                    data.put(key, val);
            } catch (SQLException ex) {
            }
        });
    }

    public void processIntegerField(Map<String, Object> data, ResultSet set) {
        intFields.forEach((key, value) -> {
            try {
                int val = set.getInt(value.toString());
                if (!set.wasNull())
                    data.put(key, val);
            } catch (SQLException ex) {
            }
        });
    }

    public static String column(String table, String column) {
        return String.format("%s.%s", table, column);
    }
}
