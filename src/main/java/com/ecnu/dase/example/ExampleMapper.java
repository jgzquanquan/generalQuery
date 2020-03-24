package com.ecnu.dase.example;

import com.ecnu.dase.service.commom.CommonRowMapper;

/**
 * @author jgz
 */
public class ExampleMapper extends CommonRowMapper {
    public ExampleMapper(CommonRowMapper parent) {
        super(parent);
        stringFields.put("year", column("example", "year"));
    }
}
