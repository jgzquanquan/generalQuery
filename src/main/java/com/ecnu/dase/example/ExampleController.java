package com.ecnu.dase.example;

import org.hibernate.criterion.Example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author jgz
 */
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
