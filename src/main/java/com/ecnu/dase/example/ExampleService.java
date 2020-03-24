package com.ecnu.dase.example;

import com.ecnu.dase.service.commom.GeneralQueryService;
import com.ecnu.dase.service.commom.SearchProcessorBuilder;
import com.ecnu.dase.service.commom.SearchSQLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jgz
 */
@Service
public class ExampleService extends GeneralQueryService {

    /**
     * 加入自己定义的处理器到searchProcessorBuilder中
     * 然后根据构造好的构造器生成
     * @param searchSQLService
     */
    @Autowired
    public void initConditionSearch(SearchSQLService searchSQLService) {
        SearchProcessorBuilder searchProcessorBuilder = searchSQLService.build()
                .addProcessor(new ExampleProcess())
                .addProcessor(new LimitClauseProcessor());
        exec = searchProcessorBuilder.create();
    }



}
