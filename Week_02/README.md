# 第三课作业实践

1、使用 GCLogAnalysis.java 自己演练一遍串行/并行/CMS/G1的案例。

实践环境：

CPU i7-6700HQ四核8线程；16G内存；操作系统Windows10；JDK 8；

实践内容：

1.  在串行GC、并行GC、CMS GC和G1GC模拟OOM，使用如下的命令：

    java -XX:+UseSerialGC -Xmx128m -XX:+PrintGCDetails -XX:+PrintGCDateStamps
    GCLogAnalysis

    java -XX:+UseParallelGC -Xmx128m -XX:+PrintGCDetails -XX:+PrintGCDateStamps
    GCLogAnalysis

    java -XX:+UseConcMarkSweepGC -Xmx128m -XX:+PrintGCDetails
    -XX:+PrintGCDateStamps GCLogAnalysis

    java -XX:+UseG1GC -XX:+PrintGC -Xmx128m -XX:+PrintGCDateStamps GCLogAnalysis

    结果如下：

    所有的GC方式都出现了OOM现象。并且都是在出现多次YoungGC后，然后持续发生FullGC直到出现OOM。连续FullGC发生过程，可以观察到最后几次FullGC，堆上的内存基本都已完全占满，而且相邻的FullGC时间间隔几乎就是前一次的FullGC的处理时间，推测此时程序可能每次分配对象都行需要进行FullGC，服务接近停止。

2.  在串行GC、并行GC、CMS GC和G1GC的各自GC使用方式下分别使用 512M,1G,2G,4G,观察
    GC 信息

    各种GC条件下，各配置下发生GC情况的说明：

|        | 512M                                                                                                                                                                  | 1G                                                                                                                                                                         | 2G                                                                                                                                            | 4G                                                                           |
|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| 串行GC | 连续19次YoungGC后出现两次FullGC。每次GC的耗时基本上都在10-40ms之间。                                                                                                  | 连续13次YoungGC。GC的耗时在平稳保持40-60ms之间后，最后三次变为11-14ms。                                                                                                    | 发生5次YoungGC，耗时在70ms-100ms之间。                                                                                                        | 两次YoungGC，耗时分别是126ms和156ms                                          |
| 并行GC | 起始有11次YoungGC然后是FullGC与YoungGC交替进行（大约有6次轮替），每次FullGC之间有2-4次连续的YoungGC，最后是连续7次FullGC。每次YoungGC耗时                             | 起始12次YoungGC后出现一次FullGC，然后又是10次YoungGC，接着一次FullGC，然后6次YoungGC直到程序退出。YoungGC都在20ms以内，大部分在10ms以内，而FullGC都在40ms                  | 出现11次YoungGC，每次YoungGC耗时都在20-30ms。                                                                                                 | 出现3次YoungGC，耗时分别为33ms，44ms和36ms。                                 |
| CMS GC | 先出现5次YoungGC（ParNew），然后是CMS GC进行，这样交叠的进行，CMS GC之间的ParNew进行的YoungGC次数会减少，有时会出现CMS GC连续执行。ParNew一共进行21次，基本在40ms以内 | 先出现5次YoungGC（ParNew），然后是CMS GC进行，这样交叠的进行，最后基本上CMS GC在执行，简或有一次ParNew执行。ParNew一共进行12次，基本在50ms以内                             | 出现6次YoungGC（ParNew），然后是1次CMS GC。ParNew的耗时，头两次为30ms左右，之后均为80ms左右。                                                 | 出现6次YoungGC（ParNew）。ParNew的耗时，头两次为30ms左右，之后均为80ms左右。 |
| G1GC   | 发生6次年轻代模式转移暂停后，交替发生并发标记。每次并发标记之间基本上是稳定的1-3次年轻代模式转移暂停。共发生70次年轻代模式转移暂停和34次并发标记。                    | 发生5次年轻代模式转移暂停后，交替发生并发标记阶 每次并发标记阶之间基本上是稳定的2-4次年轻代模式转移暂停。共发生22次年轻态GC和7次并发标记。年轻代模式转移暂停基本在10ms之内 | 发生12次年轻代模式转移暂停，在第11次和第12次之间发生一次并发标记。年轻代模式转移暂停，耗时逐渐由10ms以下变为20ms。并发标记的各阶段均在4ms以内 | 发生12次年轻代模式转移暂停，基本在10-20ms之间                                |

>   各种GC条件下，各配置下产生对象的个数：

|        | 512M  | 1G    | 2G    | 4G    |
|--------|-------|-------|-------|-------|
| 串行GC | 11249 | 13060 | 11123 | 8398  |
| 并行GC | 9978  | 16094 | 16416 | 14077 |
| CMS GC | 11056 | 13801 | 12483 | 12278 |
| G1GC   | 11306 | 13408 | 12847 | 14569 |

>   由于程序设计的问题，上面的数值不适合进行定量分析，但大致可以对各GC进行定性的评估。

2、使用压测工具（wrk或sb），演练gateway-server-0.0.1-SNAPSHOT.jar 示例

实践环境：

CPU i7-6700HQ四核8线程；16G内存；操作系统Windows10；JDK 8；

实践方式：使用串行GC、并行GC、CMS GC和G1GC分别在分别使用
512M,1G,2G,4G内存情况下，执行sb -u http://localhost:8088/api/hello -c 20 -N 60
，收集数据并分析结果。

各种GC在不同的内存情况下的RPS（requests/second）如下：

|        | 512M   | 1G     | 2G     | 4G     |
|--------|--------|--------|--------|--------|
| 串行GC | 5198.2 | 5315.3 | 5441   | 5237.4 |
| 并行GC | 5464.1 | 5447.8 | 5524.5 | 5370.7 |
| CMS GC | 5385   | 5434.5 | 5479   | 5366.6 |
| G1GC   | 5327.6 | 5317.2 | 5220   | 5237.5 |

3根据上述自己对于1和2的演示，写一段对于不同 GC 的总结

1.  串行GC基本上是所有GC中性能和GC停顿表现最差的，这和串行GC的运行机制相关，使用一个单独线程进行垃圾回收，使得GC停顿长，之间影响程序提供服务的时。并行GC因为在GC暂停时所有CPU内核都参与垃圾回收，总暂停的时间最短，可服务时间最长，因此产生最大的吞吐量的表现，但是也要看到并行GC的GC停顿还是对服务是有影响的，尤其是内存变大后，导致每次GC时间变长，减少了可服务时间，吞吐量下降。CMS
    GC整体表现稳定，因为是并发GC，每次需要系统暂停时间相对较少，并且因为大部分GC阶段是可以和业务线程同时进行，因此可以保障吞吐量相对稳定。G1GC整体表现稳定，虽然G1GC的GC处理是这些GC中最密集的，但是分阶段中需要暂停的部分是非常短暂的（基本上10-20ms左右），因此保障了应用的可服务时间，也就保障了吞吐量。

2.  内存对于各GC是有影响的。首先当内存由512M增加到1G和2G时，GC的吞吐量都增加了，这是因为内存更充足，更好容易分配，Full
    GC的次数也减少了。而增加到4G时，串行GC和并行GC有明显下降，这和这两种GC的机制相关。串行GC使用一个线程进行GC，当内存变多到一定程度后时，碎片化的程度会影响内存分配和垃圾回收，因此GC暂停时间会变长，直接造成可服务时间减少。并行GC和串行GC基本上一样的，但是因为是多线程并行（CPU是多核的），在提升到4G时才有明显降低。CMS
    GC采用并发的GC垃圾回收，内存对吞吐量的影响较小，但是当内存增加到4G时吞吐量有小幅下降时，应该是老年代碎片增多时导致降低吞吐量的。G1GC随着内存增加可以看到吞吐量是有小幅增加的，这是因为，内存增加会增加region的内存尺寸，可以更容易进行内存分配和垃圾回收，从而提升性能。

综上，针对串行GC，并行GC，CMS
GC和G1GC使用选择上，可以进行如下选择。如果是单核CPU使用串行GC就可以了，因为最能保障CPU使用率，而且单CPU的情况下，CMS
GC和G1GC的优势荡然无存。如果是多核CPU，在较小内存（4G以下），追求吞吐量，而且对GC暂停容忍度较高的话可以选择并行GC。如果JDK是1.8，而且内存较大（超过4G），并且希望系统暂停时间较低（比如低于100ms）选择G1GC是比较合适的。另外，如果JDK版本低于1.8，内存没有超过4G，但是希望系统暂停


# 第四课作业
写一段代码，使用HttpClient或OkHttp访问 http://localhost:8801
1. 使用HttpClient的代码

```
package client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClientDemo {

    public static void main(String[] args) {
        HttpClientDemo client = new HttpClientDemo();
        try {
            String content = client.getHttpContnet("http://localhost:8801/");
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHttpContnet(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        try (CloseableHttpResponse response = httpclient.execute(httpget)) {
            return EntityUtils.toString(response.getEntity());
        }
    }
}

```   

2. 使用OkHttp的代码
```
package client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class OkHttpClientDemo {

    public static void main(String[] args) {
        OkHttpClientDemo client = new OkHttpClientDemo();
        try {
            String content = client.getHttpContnet("http://localhost:8801/");
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHttpContnet(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
```