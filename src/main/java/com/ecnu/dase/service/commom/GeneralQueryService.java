package com.ecnu.dase.service.commom;

import java.util.HashMap;
import net.sf.json.JSONObject;
import java.util.List;
import java.util.Map;

/**
 * @author jgz
 */
public abstract class GeneralQueryService {

    // 根据搜索条件，创建相应的Executor类
    protected SearchSQLExecutor exec;

    protected SearchSQLExecutor countExec;

    /**
     * 如果需要使用搜索服务，则需要通过实现这个函数完成搜索处理类的构造初始化
     *
     * @param searchProcessorBuilder
     */
    public abstract void initConditionSearch(SearchProcessorBuilder searchProcessorBuilder);

    public Map<String, Object> conditionSearch(String searchCondition,
                                               Integer page, Integer size) {
        Map<String, Object> sc = (Map<String, Object>) JSONObject
                .toBean(JSONObject.fromObject(searchCondition), Map.class);
        int actualPage = 0;
        int actualSize = Integer.MAX_VALUE;
        if (page != null) {
            actualPage = page;
            actualSize = size;
        }
        return conditionSearch(sc, actualPage, actualSize);
    }

    @SuppressWarnings("rawtypes")
    public Map<String, Object> conditionSearch(Map<String, Object> sc, int page,
                                               int size) {
        Map<String, Object> res = new HashMap<>();
        Map<String, Object> countSc = new HashMap<String, Object>(sc);
        if (size != Integer.MAX_VALUE) {
            int begin = page * size;
            int end = size;
            sc.put("begin", begin);
            sc.put("end", end);
        }

        try {
            List result = exec.execute(sc);
            res.put("items", result);
            if (result.size() > 0) {
                countSc.remove("order");
                res.put("count", countExec.execute(countSc).get(0));
            } else {
                res.put("count", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public List conditionSearch(Map<String, Object> sc) {
        return exec.execute(sc);
    }
}
