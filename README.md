# generalSearch
## 简介
该项目是基于Spring JDBC的通用综合查询组件，使用builder模式加入处理的Process。该组件能够有效复用重复代码，提高团队开发效率
## 使用说明
首先得在Service层实现一个Service并且继承抽象类GeneralQueryService
然后实现抽象方法initConditionSearch，该方法主要用于加入处理器
```java
@Service
public class ExampleService extends GeneralQueryService {

    /**
     * 加入自己定义的处理器到searchProcessorBuilder中
     * 然后根据构造好的构造器生成执行器
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
```
在controller中注入ExampleService，调用其conditionSearch方法
```java
@RestController
public class ExampleController {

    private final String EXAMPLE_API = "/api/example";

    @Autowired
    private ExampleService exampleService;

    @RequestMapping(value = EXAMPLE_API, method = GET)
    @ResponseBody
    public Map<String, Object> search(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "15") Integer size,
            @RequestParam String condition) {
        Map<String, Object> res = new HashMap<>();
        res= exampleService.conditionSearch(condition, page, size);
        return res;
    }
}
```
ExampleProcess继承了抽象类ISearchProcessor，需要实现两个抽象方法，process方法主要
用于解析搜索条件。generateSearchContext主要是生成固定的是SQL（不需要根据条件生成的）
```java

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
```

将example表中的year字段放到对应HashMap的year字段中
```java
public class ExampleMapper extends CommonRowMapper {
    public ExampleMapper(CommonRowMapper parent) {
        super(parent);
        stringFields.put("year", column("example", "year"));
    }
}
```

## 类的用途
### SearchContext
主要使用Set，Map，List等集合类存储拼接SQL所需的条件，需要join的表，orderBy， groupBy的条件等。
### ISearchProcessor
这是一个抽象类，自定义的处理器需要继承该类，并且实现其中的process和generateSearchContext方法。
process方法主要处理查询条件将其where条件存到searchContex的相关字段，generateSearchContext
则在将固定的一些SQL字段先存好。

```java
public abstract class ISearchProcessor {
    //查询链的上一个处理器
    protected ISearchProcessor preProcessor;

    /**
     * 调用处理链的前一个对象的处理方法
     *
     * @param searchConditions
     * @param context
     */
    public void backwardProcess(Map<String, Object> searchConditions,
                                SearchContext context) {
        if (preProcessor != null)
            preProcessor.process(searchConditions, context);
    }

    /**
     * 处理searchConditions中的条件,存储到context中
     * 继承该抽象process的方法必须实现这个方法
     * @param searchConditions
     * @param context
     */
    protected abstract void process(Map<String, Object> searchConditions, SearchContext context);


    public void setPreProcessor(ISearchProcessor preProcessor) {
        this.preProcessor = preProcessor;
    }

    /**
     * 根据当前处理类需要涉及到的SQL操作，设置静态的<code>SearchContext</code>
     *
     * @param context
     */
    public abstract void generateSearchContext(SearchContext context);


    /**
     * 根据当前处理类需要涉及到的SQL操作，设置静态的<code>SearchContext</code>，例如：设置需要join的表，需要选择的字段等等。
     * 此处静态的涵义是指那些不需要根据搜索条件动态设置的查询操作，例如可能某些查询条件没有出现，就可以不链接某张表，这时就不需要
     * 在当前函数中设置该表的链接操作，而是在process函数中动态设置
     * 这个在一开始编译运行的时候就被执行注入生成了
     */
    public SearchContext getStaticSearchContext() {
        SearchContext ret = new SearchContext();
        generateSearchContext(ret);
        return ret;
    }

}
```

### CommonRowMapper
该类是一个抽象类并且实现了RowMapper接口，主要用于将JDBCTemplate query()方法返回的ResultSet
处理成HashMap

### SearchProcessorBuilder
使用builder模式构造搜索条件的处理链，addProcessor方法则是加入自己编写的处理不同模块的处理器
create方法则将该builder作为参数创建出SearchSQLExecutor对象返回。
```java
public class SearchProcessorBuilder {

    ISearchProcessor processor = null;

    JdbcTemplate jt;

    public SearchProcessorBuilder(SearchProcessorBuilder other) {
        this.processor = other.processor;
        this.jt = other.jt;
    }

    public SearchProcessorBuilder(JdbcTemplate jt,
                                  ISearchProcessor processor) {
        this.jt = jt;
        this.processor = processor;
    }

    public SearchProcessorBuilder addProcessor(
            ISearchProcessor newProcessor) {
        newProcessor.setPreProcessor(processor);
        this.processor = newProcessor;
        return this;
    }

    public SearchSQLExecutor create() {
        return new SearchSQLExecutor(this);
    }

}
```

### SearchSQLExecutor
根据searchContex生成相应SQL，最后excute方法将调用JDBCTemplate的query方法得到查询结果

```java
public class SearchSQLExecutor {

    final SearchProcessorBuilder builder;
    final SearchContext staticContext;
    final JdbcTemplate jt;

    SearchSQLExecutor(SearchProcessorBuilder builder) {
        this.builder = builder;
        staticContext = builder.processor.getStaticSearchContext();
        this.jt = builder.jt;
    }

    public List execute(Map<String, Object> searchConditions) {
        SearchContext context = new SearchContext();
        context.setMapper(staticContext.getMapper());
        builder.processor.process(searchConditions, context);
        return jt.query(toSQL(context), context.getMapper());
    }

    private String toSQL(SearchContext runtimeContext) {
        StringBuilder builder = new StringBuilder();

        // 构造select
        builder.append("select ");
        if (!staticContext.getAggColumns().isEmpty()
                || !runtimeContext.getAggColumns().isEmpty()) {
            builder.append(StringUtils
                    .join(CollectionUtils.union(staticContext.getAggColumns(),
                            runtimeContext.getAggColumns()), ","));

            //用以统计groupBy情况下总条目数处理
            builder.append(" from ( select ").append(staticContext.getPrimaryTable()).append(".*");

        } else {
            builder.append(
                    StringUtils.join(staticContext.selectedColumns, ","));
            if (!runtimeContext.getSelectedColumns().isEmpty()) {
                builder.append(",").append(
                        StringUtils.join(runtimeContext.selectedColumns, ","));
            }
        }

        // 构造from
        builder.append(" from ").append(staticContext.getPrimaryTable())
                .append(" ");
        builder.append(StringUtils.join(staticContext.getJoinTables(), " "));
        builder.append(" ");
        if (!runtimeContext.getJoinTables().isEmpty()) {
            builder.append(
                    StringUtils.join(runtimeContext.getJoinTables(), " "));
            builder.append(" ");
        }

        // 构造where
        builder.append(whereSQL(runtimeContext));

        if (!staticContext.getAggColumns().isEmpty()
                || !runtimeContext.getAggColumns().isEmpty()) {
            builder.append(" ) a");
        }

        return builder.toString();
    }

    /**
     * 根据被处理器处理过的条件，凭借成where语句后面的条件
     * @param runtimeContext
     * @return
     */
    private String whereSQL(SearchContext runtimeContext) {
        StringBuffer builder = new StringBuffer();
        builder.append("where ");
        boolean whereAdded = false;
        if (!staticContext.getWhere().isEmpty()) {
            if (whereAdded) {
                builder.append(" and ");
            }
            whereAdded = true;
            builder.append(StringUtils.join(staticContext.getWhere(), " and "));
        }

        if (!runtimeContext.getWhere().isEmpty()) {
            if (whereAdded) {
                builder.append(" and ");
            }
            whereAdded = true;
            builder.append(
                    StringUtils.join(runtimeContext.getWhere(), " and "));
        }

        if (!whereAdded) {
            builder.append(" 1=1 ");
        }

        Set<String> groupBySet = new HashSet<String>(
                staticContext.getGroupBy());
        List<String> groupByList = new ArrayList<>(staticContext.getGroupBy());
        for (String groupByCol : runtimeContext.getGroupBy()) {
            if (!groupBySet.contains(groupByCol)) {
                groupByList.add(groupByCol);
            }
        }
        if (!groupByList.isEmpty()) {
            builder.append(String.format(" group by %s",
                    StringUtils.join(groupByList, ",")));
        }

        Set<String> orderBySet = new HashSet<String>(
                staticContext.getOrderBy());
        List<String> orderByList = new ArrayList<>(staticContext.getOrderBy());
        for (String orderByCol : runtimeContext.getOrderBy()) {
            if (!orderBySet.contains(orderByCol)) {
                orderByList.add(orderByCol);
            }
        }
        if (!orderByList.isEmpty()) {
            builder.append(String.format(" order by %s",
                    StringUtils.join(orderByList, ",")));
        }

        if (!runtimeContext.getLimit().isEmpty()) {
            builder.append(runtimeContext.getLimit());
        }
        return builder.toString();
    }


}
```
### GeneralQueryService
定义了一个抽象方法，需要综合查询的Service可以继承这个类，然后在controller层面调用conditionSearch方法即可
```java
public abstract class GeneralQueryService {

    // 根据搜索条件，创建相应的Executor类
    protected SearchSQLExecutor exec;

    protected SearchSQLExecutor countExec;

    /**
     * 如果需要使用搜索服务，则需要通过实现这个函数完成搜索处理类的构造初始化
     *
     * @param searchSQLService
     */
    public abstract void initConditionSearch(SearchSQLService searchSQLService);

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
```

