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

