package com.ecnu.dase.example;

import com.ecnu.dase.service.commom.ISearchProcessor;
import com.ecnu.dase.service.commom.SearchContext;

import java.util.Map;

/**
 * @author jgz
 * @Date 2020-03-24 21:05
 */
public class LimitClauseProcessor extends ISearchProcessor {
    @Override
    protected void process(Map<String, Object> searchConditions, SearchContext context) {
        //调用之前process处理
        backwardProcess(searchConditions, context);

        if (searchConditions.containsKey("begin") && searchConditions
                .containsKey("end")) {
            context.setupLimit(
                    Integer.parseInt(searchConditions.get("begin").toString()),
                    Integer.parseInt(searchConditions.get("end").toString()));
        }
    }

    @Override
    public void generateSearchContext(SearchContext context) {
        //调用之前的process处理
        if (preProcessor != null)
            preProcessor.generateSearchContext(context);
    }
}
