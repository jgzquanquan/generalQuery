package com.ecnu.dase.service.commom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author jgz
 */
@Service
public class SearchSQLService {
    private SearchProcessorBuilder builder;

    private JdbcTemplate jt;

    @Autowired
    public void initialize(JdbcTemplate jt) {
        this.jt = jt;
        //根据自己业务需求可以加入默认的Process初始化
        builder = new SearchProcessorBuilder(jt, null);
    }

    /**
     * 创建一个没有添加任何处理链为空的<code>SearchProcessorBuilder</code>
     * @return
     */
    public SearchProcessorBuilder build() {
        return new SearchProcessorBuilder(jt, null);
    }
}
