package com.ecnu.dase.example;

import com.ecnu.dase.service.commom.ISearchProcessor;
import com.ecnu.dase.service.commom.SearchContext;

import java.util.Map;

/**
 * @author jgz
 * @Date 2020-03-24 21:05
 */
public class ExampleProcess extends ISearchProcessor {
    @Override
    protected void process(Map<String, Object> searchConditions, SearchContext context) {
        backwardProcess(searchConditions, context);
        if (searchConditions.containsKey("year")){
            context.stringEqual("example", "year", searchConditions.get("year").toString());
        }
    }

    @Override
    public void generateSearchContext(SearchContext context) {
        //调用之前的process处理
        if (preProcessor != null){
            preProcessor.generateSearchContext(context);
        }else {
            //如果没有之前的处理链则代表其为主表
            context.setPrimaryTable("example");
        }
        context.setMapper(new ExampleMapper(context.getMapper()));
    }
}
