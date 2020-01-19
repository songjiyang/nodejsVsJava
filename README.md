

 环境 | -
---|---
测试工具 | ab
主机     |k8s集群
框架|  nodejs和express, java和Springboot



## 搭建Rancher

[AWS官方搭建文档](https://rancher.com/docs/rancher/v2.x/en/quick-start-guide/deployment/quickstart-manual-setup/), 使用手动安装的方式, 加入1台当做k8s master和worker, 开启prometheus监控

## 测试三种API

- HelloWorld，返回HelloWorld JSON
- 计算任务，使用sleep阻塞线程来模拟计算任务
- 数据库查询，列出MongoDB中100个问卷

## 准备代码和脚本

https://github.com/songjiyang/nodejsVsJava

## 测试过程

批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
1|Node|hello|10 | 1000 |1000m | 1024Mi |382.90
1|Java|hello|10 | 1000 |1000m | 1024Mi |219.34
1|Node|compute|10 | 1000 |1000m | 1024Mi |2.08
1|Java|compute|10 | 1000 |1000m | 1024Mi |18.06
1|Node|listQuestionaire|10 | 1000 |1000m | 1024Mi |305.89
1|Java|listQuestionaire|10 | 1000 |1000m | 1024Mi |115.83
2|Node|hello|10 | 1000 |1000m | 1024Mi |471.50
2|Java|hello|10 | 1000 |1000m | 1024Mi |250.43
2|Node|compute|10 | 1000 |1000m | 1024Mi |2.01
2|Java|compute|10 | 1000 |1000m | 1024Mi |19.36
2|Node|listQuestionaire|10 | 1000 |1000m | 1024Mi |359.50
2|Java|listQuestionaire|10 | 1000 |1000m | 1024Mi |32.24
3|Node|hello|100 | 1000 |1000m | 1024Mi |1142.79
3|Java|hello|100 | 1000 |1000m | 1024Mi |548.92
3|Node|compute|100 | 1000 |1000m | 1024Mi |1.95
3|Java|compute|100 | 1000 |1000m | 1024Mi |135.53
3|Node|listQuestionaire|100 | 1000 |1000m | 1024Mi |449.48
3|Java|listQuestionaire|100 | 1000 |1000m | 1024Mi |47.61
### Pod 内存使用情况
![Pod 内存使用率](http://pic.sjoe.top/blog/51579000359_.pic_hd.jpg)
### Pod CPU使用情况
![Pod Cpu使用率](http://pic.sjoe.top/blog/41579000343_.pic_hd.jpg)
### Node 情况
![Node](http://pic.sjoe.top/blog/61579000514_.pic_hd.jpg)

![柱状图](http://pic.sjoe.top/blog/111579080102_.pic_hd.jpg)
- 总体上是Node在IO任务上更占优势，Java在CPU任务上更占优势
- 期间发现使用sleep来模拟cpu任务不太妥，而且使用while阻塞Node的方式不太合适，因为这样区分不了哪种计算能力强一些，所以将sleep换成求10000以内的素数，相同的算法
- 在此看到JAVA的内存占用比较高，Java开启过多线程会占用大量的内存，而大博提出新的Tomcat已经使用了NIO，不会开启过多的线程，于是在网上查询了两篇文章，找到了Tomcat的NIO和BIO的不同之处[tomcat nio与bio技术对比](https://blog.csdn.net/jone_lu/article/details/49208611), [深度解读Tomcat中的NIO模型](https://www.jianshu.com/p/76ff17bc6dea), BIO是一个请求一个线程，NIO是一个处理线程池，请求复用这些线程, 当并发请求数在这个线程池最大线程数范围内的话内存还是会增长，当大于最大线程数就会保持不变。SpringBoot的Tomcat的最大线程数是200，最大连接数是10000。

批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
4|Node|hello|100 | 1000 |1000m | 1024Mi |879.48
4|Java|hello|100 | 1000 |1000m | 1024Mi |514.65
4|Node|compute|100 | 1000 |1000m | 1024Mi |741.92
4|Java|compute|100 | 1000 |1000m | 1024Mi |366.72
4|Node|listQuestionaire|100 | 1000 |1000m | 1024Mi |18.93
4|Java|listQuestionaire|100 | 1000 |1000m | 1024Mi |13.94

![柱状图](http://pic.sjoe.top/blog/101579416841_.pic_hd.jpg)

- 这里看到node的计算能力竟然超过了java，推测可能试因为在1个CPU的情况的java多线程会不断切换导致，下面尝试使用2个CPU, 并且Node开启pm2启动两个实例
- 查询Mongo数据库的能力相比上面差很多，而且还会出现超时的情况,这里不清楚原因，可能和mongo数据库有关，下面尝试使用内网数据再做比较
- 这里还发现一个问题是java的内存使用限制1G但还是会超过1G, 看到一篇文章说道java8的jvm默认会使用宿主机的内存，[文章](https://blog.csdn.net/Jerry_Pan1990/article/details/101773313)
, 同时也修复此问题, 查看grafana的pod内存使用率略高于1G， 而日志却因为`java.lang.OutOfMemoryError`报错
![Pod监控](http://pic.sjoe.top/blog/101579076671_.pic_hd.jpg)


批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
5|Node(pm2 2实例)|hello|100 | 1000 |2000m | 5000Mi |1316.67
5|Java|hello|100 | 1000 |2000m | 5000Mi |968.10
5|Node(pm2)|compute|100 | 1000 |2000m | 5000Mi |745.89
5|Java|compute|100 | 1000 |2000m | 5000Mi |705.91
5|Node(pm2)|listQuestionaire|100 | 1000 |2000m | 5000Mi |635.41
5|Java|listQuestionaire|100 | 1000 |2000m | 5000Mi |17.70
-|-|-|- | - |- | - |-
6|Node(pm2)|hello|100 | 1000 |2000m | 5000Mi |1416.57
6|Java|hello|100 | 1000 |2000m | 5000Mi |1666.86
6|Node(pm2)|compute|100 | 1000 |2000m | 5000Mi |825.94
6|Java|compute|100 | 1000 |2000m | 5000Mi |991.21
6|Node(pm2)|listQuestionaire|100 | 1000 |2000m | 5000Mi |691.20
6|Java|listQuestionaire|100 | 1000 |2000m | 5000Mi |21.88
-|-|-|- | - |- | - |-
7|Node(pm2)|hello|100 | 1000 |2000m | 5000Mi |1485.31
7|Java|hello|100 | 1000 |2000m | 5000Mi |1717.17
7|Node(pm2)|compute|100 | 1000 |2000m | 5000Mi |846.70
7|Java|compute|100 | 1000 |2000m | 5000Mi |1258.35
7|Node(pm2)|listQuestionaire|100 | 1000 |2000m | 5000Mi |727.67
7|Java|listQuestionaire|100 | 1000 |2000m | 5000Mi |24.25
-|-|-|- | - |- | - |-
8|Node(pm2)|hello|100 | 1000 |2000m | 5000Mi |1494.97
8|Java|hello|100 | 1000 |2000m | 5000Mi |1696.09
8|Node(pm2)|compute|100 | 1000 |2000m | 5000Mi |895.95
8|Java|compute|100 | 1000 |2000m | 5000Mi |1088.73
8|Node(pm2)|listQuestionaire|100 | 1000 |2000m | 5000Mi |684.71
8|Java|listQuestionaire|100 | 1000 |2000m | 5000Mi |25.32

![pod cpu](http://pic.sjoe.top/blog/121579166017_.pic_hd.jpg)
![pod mem](http://pic.sjoe.top/blog/131579166053_.pic_hd.jpg)
![cluster](http://pic.sjoe.top/blog/141579166079_.pic_hd.jpg)
![柱状图](http://pic.sjoe.top/blog/111579417273_.pic.jpg)
- 每个Pod使用2个CPU，5G内存，压测发现Java的性能会随着压测测试慢慢变好，计算能力先小于Node,然后大于Node, io远不如Node
- 从监控来看Java无论CPU还是内存都比Node使用的多
- 下面尝试加大并发和请求量来测试1000并发和10000请求量


批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
9|Node(pm2 2实例)|hello|1000 | 10000 |2000m | 5000Mi |2168.73
9|Java|hello|1000 | 10000 |2000m | 5000Mi |3491.00
9|Node(pm2)|compute|1000 | 10000 |2000m | 5000Mi |997.14
9|Java|compute|1000 | 10000 |2000m | 5000Mi |1479.55
9|Node(pm2)|listQuestionaire|1000 | 10000 |2000m | 5000Mi |984.65
9|Java|listQuestionaire|1000 | 10000 |2000m | 5000Mi |20.82
-|-|-|- | - |- | - |-
10|Node(pm2 2实例)|hello|1000 | 10000 |2000m | 5000Mi |1844.12
10|Java|hello|1000 | 10000 |2000m | 5000Mi |2361.83
10|Node(pm2)|compute|1000 | 10000 |2000m | 5000Mi |1007.12
10|Java|compute|1000 | 10000 |2000m | 5000Mi |1551.75
10|Node(pm2)|listQuestionaire|1000 | 10000 |2000m | 5000Mi |960.80
10|Java|listQuestionaire|1000 | 10000 |2000m | 5000Mi |21.02

![Pod](http://pic.sjoe.top/blog/151579167972_.pic_hd.jpg)
![柱状图](http://pic.sjoe.top/blog/121579417657_.pic.jpg)
- 可以看到在高并发下Java的hello接口和compute能力都和Node拉开了距离， 而IO能力远不及Node
- 从监控来看JAVA对CPU的使用达到500%，说明JAVA还需要更多的CPU, 而内存已经稳定到了4G多，正如之前所说，达到Tomcat线程池最大连接数之后内存不在增长
- 下面将CPU增加，然后再进行测试

批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
11|Node(pm2 4实例)|hello|1000 | 10000 |3500m | 5000Mi |2659.27
11|Java|hello|1000 | 10000 |3500m | 5000Mi |4424.37
11|Node(pm2)|compute|1000 | 10000 |3500m | 5000Mi |1501.16
11|Java|compute|1000 | 10000 |3500m | 5000Mi |1957.03
11|Node(pm2)|listQuestionaire|1000 | 10000 |3500m | 5000Mi |1289.51
11|Java|listQuestionaire|1000 | 10000 |3500m | 5000Mi |41.84
-|-|-|- | - |- | - |-
12|Node(pm2 4实例)|hello|1000 | 10000 |3500m | 5000Mi |3069.04
12|Java|hello|1000 | 10000 |3500m | 5000Mi |3885.61
12|Node(pm2)|compute|1000 | 10000 |3500m | 5000Mi |1535.47
12|Java|compute|1000 | 10000 |3500m | 5000Mi |1734.61
12|Node(pm2)|listQuestionaire|1000 | 10000 |3500m | 5000Mi |1386.83
12|Java|listQuestionaire|1000 | 10000 |3500m | 5000Mi |41.91

![Pod](http://pic.sjoe.top/blog/161579174583_.pic_hd.jpg)
![柱状图](http://pic.sjoe.top/blog/131579417946_.pic.jpg)

- 从数据可以看到，Cpu的增加使Node和Java的吞吐量都变高了，hello和compute还是比Node好一点，IO任务Java基本是无法通过各种方式来提高性能的
- 从监控可以看出，Java对Cpu还是有很高的需求，说明在大并发下，java需要不断切换线程来处理Tomcat最大线程池数量的那些请求
- 下面将Pod的数量变为2，这样并发将减少一半，但需要多一倍的系统资源

批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
13|Node(pm2 4实例)|hello|1000 | 10000 |3500m | 5000Mi |5436.20
13|Java|hello|1000 | 10000 |3500m | 5000Mi |4843.66
13|Node(pm2)|compute|1000 | 10000 |3500m | 5000Mi |2960.31
13|Java|compute|1000 | 10000 |3500m | 5000Mi |2994.94
13|Node(pm2)|listQuestionaire|1000 | 10000 |3500m | 5000Mi |2594.91
13|Java|listQuestionaire|1000 | 10000 |3500m | 5000Mi |98.45
-|-|-|- | - |- | - |-
14|Node(pm2 4实例)|hello|1000 | 10000 |3500m | 5000Mi |5011.54
14|Java|hello|1000 | 10000 |3500m | 5000Mi |6102.00
14|Node(pm2)|compute|1000 | 10000 |3500m | 5000Mi |3188.70
14|Java|compute|1000 | 10000 |3500m | 5000Mi |2980.22
14|Node(pm2)|listQuestionaire|1000 | 10000 |3500m | 5000Mi |2649.99
14|Java|listQuestionaire|1000 | 10000 |3500m | 5000Mi |98.62

![Pod](http://pic.sjoe.top/blog/171579178248_.pic_hd.jpg)
![柱状图](http://pic.sjoe.top/blog/141579418401_.pic.jpg)
- 可以看出，将资源增加了一倍，吞吐量大致翻了一倍， 但此时一个Pod的并发是500
- 当并发减少时，Node和Java的hello和compute性能相当
- 从监控图看到java的单个Pod Cpu使用率已经降到了250%， 但两台JavaPod使用的内存确一多一少，因为前面有nginx ingress controller转发，应该是使用轮询算法来做负载均衡的，这个问题要再确认一下
- 再次将Pod数量调回1，并将并发增加到2000，测试在更高并发下的性能

批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
15|Node(pm2 4实例)|hello|2000 | 10000 |3500m | 5000Mi |2953.52
15|Java|hello|2000 | 10000 |3500m | 5000Mi |3225.54
15|Node(pm2)|compute|2000 | 10000 |3500m | 5000Mi |1620.88
15|Java|compute|2000 | 10000 |3500m | 5000Mi |1929.59
15|Node(pm2)|listQuestionaire|2000 | 10000 |3500m | 5000Mi |1239.97
15|Java|listQuestionaire|2000 | 10000 |3500m | 5000Mi |42.91
-|-|-|- | - |- | - |-
16|Node(pm2 4实例)|hello|2000 | 10000 |3500m | 5000Mi |2739.51
16|Java|hello|2000 | 10000 |3500m | 5000Mi |3406.18
16|Node(pm2)|compute|2000 | 10000 |3500m | 5000Mi |1558.05
16|Java|compute|2000 | 10000 |3500m | 5000Mi |1758.92
16|Node(pm2)|listQuestionaire|2000 | 10000 |3500m | 5000Mi |1433.39
16|Java|listQuestionaire|2000 | 10000 |3500m | 5000Mi |42.32

![柱状图](http://pic.sjoe.top/blog/151579419393_.pic.jpg)

- 这一次测试和12和13批次的测试不同点在并发，一个是1000，一个是2000，但测试结构和监控几乎是一样的，说明在这样的情况下，即使增加并发，多余的请求也是排队进行处理，达到了瓶颈
- 为了能够支持更高的并发量，下面的测试将Springboot将增加内置tomcat的最大线程数改为500,[依据在此](https://www.dazhuanlan.com/2019/09/29/5d9039ec6b9a4/)

批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
17|Node(pm2 4实例)|hello|2000 | 10000 |3500m | 5000Mi |1632.13
17|Java|hello|2000 | 10000 |3500m | 5000Mi |5405.41
17|Node(pm2)|compute|2000 | 10000 |3500m | 5000Mi |1152.02
17|Java|compute|2000 | 10000 |3500m | 5000Mi |2369.59
17|Node(pm2)|listQuestionaire|2000 | 10000 |3500m | 5000Mi |720.37
17|Java|listQuestionaire|2000 | 10000 |3500m | 5000Mi |46.44
-|-|-|- | - |- | - |-
18|Node(pm2 4实例)|hello|2000 | 10000 |3500m | 5000Mi |1771.90
18|Java|hello|2000 | 10000 |3500m | 5000Mi |3946.24
18|Node(pm2)|compute|2000 | 10000 |3500m | 5000Mi |1182.58
18|Java|compute|2000 | 10000 |3500m | 5000Mi |2226.49
18|Node(pm2)|listQuestionaire|2000 | 10000 |3500m | 5000Mi |823.09
18|Java|listQuestionaire|2000 | 10000 |3500m | 5000Mi |43.37

![Pod](http://pic.sjoe.top/blog/181579241971_.pic_hd.jpg)
![柱状图](http://pic.sjoe.top/blog/161579419901_.pic.jpg)

- 增加线程数使java的性能有所提升，但Node的的处理能力都有所下降(很奇怪),尤其是IO能力，在调整线程的过程中，java称报过mongo连接数太多的错，因为Node和java连一个mongo数据库，推测可能是mongo连接数不够影响node的性能, 后来发现不是这个问题，因为pod会落在ingress所在的pod会有所影响，改进，使用另外一个Node的ingress

批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
19|Node(pm2 4实例)|hello|2000 | 10000 |3500m | 5000Mi |2991.55
19|Java|hello|2000 | 10000 |3500m | 5000Mi |3779.44
19|Node(pm2)|compute|2000 | 10000 |3500m | 5000Mi |1641.51
19|Java|compute|2000 | 10000 |3500m | 5000Mi |2454.05
19|Node(pm2)|listQuestionaire|2000 | 10000 |3500m | 5000Mi |1512.09
19|Java|listQuestionaire|2000 | 10000 |3500m | 5000Mi |43.19

![柱状图](http://pic.sjoe.top/blog/171579420049_.pic.jpg)
- 恢复至原水平
- 增加并发至5000测试

批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
20|Node(pm2 4实例)|hello|2000 | 10000 |3500m | 5000Mi |2934.10
20|Java|hello|2000 | 10000 |3500m | 5000Mi |1196.26
20|Node(pm2)|compute|2000 | 10000 |3500m | 5000Mi |1270.70
20|Java|compute|2000 | 10000 |3500m | 5000Mi |564.87
20|Node(pm2)|listQuestionaire|2000 | 10000 |3500m | 5000Mi |1429.42
20|Java|listQuestionaire|2000 | 10000 |3500m | 5000Mi |失败
 
- 增加到5000并发之后，java listQuestionaire测试完成不了，Client端会主动断开，服务端报`org.apache.catalina.connector.ClientAbortException: java.io.IOException: Broken pipe`这样的错，推测Tomcat在这么高并发下有什么问题，因为和Node对比，排除了ab测试工具和nginx ingress controller的问题
- 除此之外也有大约有20%的失败返回，之前只有较少的错误返回
- 对于如何对Springboot， tomcat, jvm, node, nginx, 甚至数据库, k8s资源配置等合理的调优和使用，需要在实际业务场景中进行测试，在此不再研究


# 结论
- 整体上来看，Java在CPU密集任务上面性能稍好于与Node， Node在IO密集任务上面性能远好与Java
- Java对CPU和内存的需求高于Node， 随着CPU和内存的增加，Java的提升高于Node(从监控和8批次来看)
- 随着并发的增加，没达到吞吐量瓶颈之前，Java的吞吐量增长高于Node(根据批次8和批次10)
- java的随着访问次数变多，性能会略有提升，猜测是Jvm的JIT优化(根据8批次)
- 对于更高的并发，以及更好的系统资源下哪个更好一点需要进一步测试

至于如何选择，从性能方面来看，要看业务代码主要是用来做什么，但我们常用的后台应该涉及的IO更多一点，数据库的增删改查也是查询最多，从这方面考虑推荐用Node，从其他角度来看，Java有强类型，良好的语言规范等等


# 追加

后来在查看为什么java会比Node的io这么慢时，发现自己犯了一个严重的错误，java的查询mongo没有带projection, 就是filed.include那块代码，导致java从mongodb取数据时，远比Node取的数据多，所以io性能不如java,下面将补充对listQuestionaire重新测试， 条件以测试结尾时的条件，单个Pod, Tomcat 500个最大线程，pm2 4个实例
```
        Query query = new Query();
        Field field = query.fields();
        field.include("_id");
        field.include("suggestion_id");
        field.include("user_id");
        field.include("openid");
        query.limit(100);
        List<Answer> answers = template.find(query, Answer.class);
```
批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
21|Node(pm2 4实例)|hello|1000 | 10000 |3500m | 5000Mi |1561.58
21|Java|hello|1000 | 10000 |3500m | 5000Mi |2513.87
21|Node(pm2)|compute|1000 | 10000 |3500m | 5000Mi |3137.15
21|Java|compute|1000 | 10000 |3500m | 5000Mi |3963.56
21|Node(pm2)|listQuestionaire|1000 | 10000 |3500m | 5000Mi |1375.73
21|Java|listQuestionaire|1000 | 10000 |3500m | 5000Mi |1728.49
-|-|-|- | - |- | - |-
22|Node(pm2 4实例)|hello|1000 | 10000 |3500m | 5000Mi |3603.90
22|Java|hello|1000 | 10000 |3500m | 5000Mi |3179.34
22|Node(pm2)|compute|1000 | 10000 |3500m | 5000Mi |1666.97
22|Java|compute|1000 | 10000 |3500m | 5000Mi |2540.11
22|Node(pm2)|listQuestionaire|1000 | 10000 |3500m | 5000Mi |1558.75
22|Java|listQuestionaire|1000 | 10000 |3500m | 5000Mi |1705.15
-|-|-|- | - |- | - |-
23|Node(pm2 4实例)|hello|1000 | 10000 |3500m | 5000Mi |1684.95
23|Java|hello|1000 | 10000 |3500m | 5000Mi |2452.82
23|Node(pm2)|compute|1000 | 10000 |3500m | 5000Mi |4045.12
23|Java|compute|1000 | 10000 |3500m | 5000Mi |6526.49
23|Node(pm2)|listQuestionaire|1000 | 10000 |3500m | 5000Mi |1601.06
23|Java|listQuestionaire|1000 | 10000 |3500m | 5000Mi |1669.40

![Pod](http://pic.sjoe.top/blog/181579426741_.pic_hd.jpg)
![柱状图](http://pic.sjoe.top/blog/191579432911_.pic.jpg)

- 并发改为1000情况下，可以看到java在三个接口都和Node持平，甚至胜于Node
- JavaCpu的使用低于Node的使用，内存高于Node的使用
- 增加到2000并发

批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
24|Node(pm2 4实例)|hello|2000 | 10000 |3500m | 5000Mi |3504.76
24|Java|hello|2000 | 10000 |3500m | 5000Mi |3968.59
24|Node(pm2)|compute|2000 | 10000 |3500m | 5000Mi |1358.74
24|Java|compute|2000 | 10000 |3500m | 5000Mi |2230.48
24|Node(pm2)|listQuestionaire|2000 | 10000 |3500m | 5000Mi |1413.38
24|Java|listQuestionaire|2000 | 10000 |3500m | 5000Mi |1655.60
-|-|-|- | - |- | - |-
25|Node(pm2 4实例)|hello|2000 | 10000 |3500m | 5000Mi |2930.40
25|Java|hello|2000 | 10000 |3500m | 5000Mi |3929.74
25|Node(pm2)|compute|2000 | 10000 |3500m | 5000Mi |1607.64
25|Java|compute|2000 | 10000 |3500m | 5000Mi |2094.14
25|Node(pm2)|listQuestionaire|2000 | 10000 |3500m | 5000Mi |1529.01
25|Java|listQuestionaire|2000 | 10000 |3500m | 5000Mi |1689.42

![柱状图](http://pic.sjoe.top/blog/201579433163_.pic.jpg)
- 在并发为2000情况下java都略胜于Node, 监控和之前一致
- 增加到5000并发

批次|语言|api|并发量 | 请求数| cpu请求和限制|内存请求和限制 | 吞吐量
---|---|---|---|---|---|---|---
26|Node(pm2 4实例)|hello|5000 | 10000 |3500m | 5000Mi |3775.58
26|Java|hello|5000 | 10000 |3500m | 5000Mi |4239.99
26|Node(pm2)|compute|5000 | 10000 |3500m | 5000Mi |1752.90
26|Java|compute|5000 | 10000 |3500m | 5000Mi |2177.67
26|Node(pm2)|listQuestionaire|5000 | 10000 |3500m | 5000Mi |1728.88
26|Java|listQuestionaire|5000 | 10000 |3500m | 5000Mi |1735.59

- 还是差不多各有20%的错误响应，这次的不做比较

# 二次总结
因为个人的失误，差点导致对本次压测结果产生错误的结论，通过对上述的追加测试，可以得出Java在各个方面都有着良好的性能，都和Node持平或胜于Node。
# 参考

[1] [不同编程语言Web压测](https://github.com/bopeng87/yunqi-frontend/invitation)

[2] [Node和Java压测](https://www.tandemseven.com/blog/performance-java-vs-node/)


[3] [tomcat nio与bio技术对比](https://blog.csdn.net/jone_lu/article/details/49208611)

[4] [深度解读Tomcat中的NIO模型](https://www.jianshu.com/p/76ff17bc6dea)