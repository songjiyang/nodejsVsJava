package com.lemonbox.stress.controller;

import com.lemonbox.stress.Answer;
import com.lemonbox.stress.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@RestController
@RequestMapping("/api/v1")
public class StressController {

    private static Logger logger = LoggerFactory.getLogger(StressController.class);
    @Autowired
    MongoTemplate template;

    @RequestMapping(value = "/hello")
    public CommonResult hello() {
        return CommonResult.success("hello world");
    }

    @RequestMapping(value = "/compute")
    public CommonResult compute() throws InterruptedException {
//        int sleepTime = new Random().nextInt(2);
//        Thread.sleep(sleepTime * 1000);
        prime(10000);
        return CommonResult.success("done");
    }

    public void prime(int n){
        int num=0;
        int j;
        boolean sgin;
        for (int i = 2; i <= n; i++) {
            if(i % 2 == 0 && i != 2  )  continue; //偶数和1排除

            sgin= true;
            for (j = 2; j <= Math.sqrt(i) ; j++) {
                if (i % j == 0) {
                    sgin = false;
                    break;
                }
            }

            //打印
            if (sgin) {
                num++;
                /* System.out.println(""+i);*/
            }
        }
        logger.info(n+"以内的素数有"+num+"个");
    }

    @RequestMapping(value = "/listQuestionaire")
    public CommonResult listQuesionaire(){
        Query query = new Query();
        query.limit(100);
        List<Answer> answers = template.find(query, Answer.class);

        return CommonResult.success(answers);
    }

    @RequestMapping(value = "/systemInfo")
    public CommonResult SystemInfo(){
        Runtime run = Runtime.getRuntime();

        long max = run.maxMemory()/(1024*1024);

        long total = run.totalMemory()/(1024*1024);

        long free = run.freeMemory()/(1024*1024);

        long usable = max - total + free;
        Map<String, Long> m = new HashMap<>();
        m.put("最大内存(MB) = ", max);
        m.put("已分配内存(MB) = ", total);
        m.put("已分配内存中的剩余空间(MB) = ", free);
        m.put("最大可用内存(MB) = ", usable);

        return CommonResult.success(m);
    }
}
