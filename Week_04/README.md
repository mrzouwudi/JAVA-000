# 作业说明
## week4 必做作业题一：思考有多少种方式，在 main 函数启动一个新线程，运行一个方法，拿到这个方法的返回值后，退出主线程？

这个题目，我分析主要是要完成异步转同步阻塞的获取，核心是两个线程的信息通知和结果的获取上。我采用了以下几种方式
1. 使用对象的wait方法，让主线程等待,进行计算的线程异步执行完毕调用notify通知主线程，然后获取数据。数据部分因为方法返回值是int类型，我使用了一个元素int[]进行数据的传递，以下的各方案中基本上也是按照这个方式进行数据传递的。
```
package java0.conc0303.homework;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用wait，让主线程等待,进行计算的线程异步执行完毕调用notify，然后获取数据。
 */
public class HomeWorkUsingWaitAndNotify {
    public static void main(String[] args) {
        //用于保存方法调用的结果
        final int[] fiboResult = new int[1];
        final Object lock = new Object();

        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        Thread t = new Thread(() -> {
            synchronized (lock) {
                fiboResult[0] = sum();
                lock.notify();
            }
        });
        t.start();
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int result = fiboResult[0];
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：124 ms
```
源代码见HomeWorkUsingWaitAndNotify.java

2. 使用join，让主线程等待进行计算的线程异步执行完毕，然后获取数据。
```
package java0.conc0303.homework;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用join，让主线程等待进行计算的线程异步执行完毕，然后获取数据。
 */
public class HomeWorkUsingJoin {
    public static void main(String[] args) {
        //用于保存方法调用的结果
        final int[] fiboResult = new int[1];

        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        Thread t = new Thread(() -> {
            fiboResult[0] = sum();
       });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int result = fiboResult[0];
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：128 ms
```
源代码见HomeWorkUsingJoin.java

3. 使用sleep，睡10ms，让主线程等待，10ms后醒来检查是否被中断了,这样隔一段时间检查条件是否触发（这里是以中断标志是否被设置为判断条件）。而进行计算的线程异步执行完毕对主线调用interrupt，触发判断条件翻转。此外可以使用其他方式作为判断条件，如设置一个volatile修饰的boolean变量
```
package java0.conc0303.homework;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用sleep，睡10ms，让主线程等待，10ms后醒来检查是否被中断了,这样隔一段时间检查条件是否触发（这里是以中断标志是否被设置为判断条件）
 * 而进行计算的线程异步执行完毕对主线调用interrupt，触发判断条件翻转。此外可以使用其他方式作为判断条件，如设置一个volatile修饰的boolean变量
 */
public class HomeWorkUsingSleepAndInterrupt {
    public static void main(String[] args) {
        //用于保存方法调用的结果
        final int[] fiboResult = new int[1];

        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法

        Thread t = new TaskThread(Thread.currentThread(), fiboResult);
        t.start();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        int result = fiboResult[0];
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static class TaskThread extends Thread {
        private Thread target;
        private int[] result;
        public TaskThread(Thread target, int[] result) {
            this.target = target;
            this.result = result;
        }

        @Override
        public void run() {
            result[0] = sum();
            target.interrupt();
        }
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}

```
执行结果如下：
```
异步计算结果为：24157817
使用时间：74 ms
```
源代码见HomeWorkUsingSleepAndInterrupt.java

4. 使用yield方法，让渡CPU给新线程异步调用方法，不断判断异步线程是否还存在，如果已结束获取结果，否则yield。这里yield所处的循环判断是按照课程中demo中使用的，也只能在简单demo中进行使用。
```
package java0.conc0303.homework;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用yield方法，让渡CPU给新线程异步调用方法，不断判断异步线程是否还存在，如果已结束获取结果，否则yield。
 */
public class HomeWorkUsingYield {
    public static void main(String[] args) {
        //用于保存方法调用的结果
        final int[] fiboResult = new int[1];
        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        Thread t = new Thread(() -> {
            fiboResult[0] = sum();
        });
        t.start();
        while (Thread.activeCount()>2){//当前线程的线程组中的数量>2
            Thread.yield();
        }
        int result = fiboResult[0];
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：134 ms
```
源代码见HomeWorkUsingYield.java

5. 使用yield方法+CopyOnWriteArrayList，主线程判断CopyOnWriteArrayList中没有数据（size为0）则yield，否则取数据。这里其实也可以使用其他同步的容器进行使用，另外COW也可以配合sleep进行（如第3部分）。
```
package java0.conc0303.homework;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用yield方法+CopyOnWriteArrayList，主线程判断CopyOnWriteArrayList中没有数据（size为0）则yield，否则取数据
 */
public class HomeWorkUsingYieldAndCOW {
    public static void main(String[] args) {
        //使用CopyOnWriteArrayList保存数据
        final List<Integer> list = new CopyOnWriteArrayList();
        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        Thread t = new Thread(() -> {
            list.add(sum());
        });
        t.start();
        while (list.size() == 0){//CopyOnWriteArrayList中没有数据，则还未计算完
            Thread.yield();
        }
        int result = list.get(0);
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：133 ms
```
源代码见HomeWorkUsingYieldAndCOW.java

6. 使用LockSupport的park和unpark方法进行等待和通知，可类比第1部分wait和notify
```
package java0.conc0303.homework;

import java.util.concurrent.locks.LockSupport;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用LockSupport的park和unpark方法进行等待和通知
 */
public class HomeWorkUsingLockSupport {
    public static void main(String[] args) {
        //用于保存方法调用的结果
        final int[] fiboResult = new int[1];

        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法

        Thread t = new TaskThread(Thread.currentThread(), fiboResult);
        t.start();
        LockSupport.park();
        int result = fiboResult[0];
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static class TaskThread extends Thread {
        private Thread target;
        private int[] result;
        public TaskThread(Thread target, int[] result) {
            this.target = target;
            this.result = result;
        }

        @Override
        public void run() {
            result[0] = sum();
            LockSupport.unpark(target);
        }
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：70 ms
```
源代码见HomeWorkUsingLockSupport.java

7. 使用Lock + Condition，通过Condition的await使主线程阻塞，signal则负责通知主线程执行完成。可类比第1部分wait和notify。
```
package java0.conc0303.homework;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用Lock + Condition，通过Condition的await使主线程阻塞，signal则负责通知主线程执行完成
 */
public class HomeWorkUsingLockCondition {
    public static void main(String[] args) {
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        //用于保存方法调用的结果
        final int[] fiboResult = new int[1];

        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        new Thread(() -> {
            lock.lock();
            try {
                fiboResult[0] = sum();
                condition.signal();
            } finally {
                lock.unlock();
            }
        }).start();
        lock.lock();
        try {
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        int result = fiboResult[0];
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：128 ms
```
源代码见HomeWorkUsingLockCondition.java

8. 使用CountDownLatch，主线等待CounDownLatch通知结束，获取数据。
```
package java0.conc0303.homework;

import java.util.concurrent.CountDownLatch;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用CountDownLatch，主线等待CounDownLatch通知结束，获取数据。
 */
public class HomeWorkUsingCountDownLatch {
    public static void main(String[] args) {
        //用于保存方法调用的结果
        final int[] fiboResult = new int[1];
        //CountDownLatch，计数值为1
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        new Thread(()->{
            fiboResult[0]=sum();
            countDownLatch.countDown();}).start();
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int result = fiboResult[0];
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    static class Task implements Runnable{
        final int[] result;
        Task(int[] sum) {
            this.result = sum;
        }

        public void run() {
            //可以操作result
            result[0] = sum();
        }
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：130 ms
```
源代码见HomeWorkUsingCountDownLatch.java

9. 使用Future+线程池，主线程通过Future.get方法阻塞等待异步完成
```
package java0.conc0303.homework;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用Future+线程池，主线程通过Future.get方法阻塞等待异步完成
 */
public class HomeWorkUsingFuture {
    public static void main(String[] args) {
        //用于保存方法调用的结果
        final int[] fiboResult = new int[1];

        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<int[]> future = es.submit(new Task(fiboResult), fiboResult);
        int result = 0;
        try {
            result = future.get()[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        es.shutdown();
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    static class Task implements Runnable{
        final int[] result;
        Task(int[] sum) {
            this.result = sum;
        }

        public void run() {
            result[0] = sum();
        }
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：80 ms
```
源代码见HomeWorkUsingFuture.java

10. 使用FutureTask+线程池，主线程使用FutureTask.get获取异步线程返回结果，不需要额外设置变量。
```
package java0.conc0303.homework;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用FutureTask+线程池，主线程使用FutureTask.get获取异步线程返回结果，不需要额外设置变量。
 */
public class HomeWorkUsingFutureTask {
    public static void main(String[] args) {
        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        FutureTask<Integer> futureTask = new FutureTask<>(() -> sum());
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(futureTask);
        int result = 0;
        try {
            result = futureTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        es.shutdown();
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：137 ms
```
源代码见HomeWorkUsingFutureTask.java

11. 使用CompletableFuture，调用异步方式，最后主线程获取数据
```
package java0.conc0303.homework;


import java.util.concurrent.CompletableFuture;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用CompletableFuture，调用异步方式，最后主线程获取数据
 */
public class HomeWorkUsingCompletableFuture {
    public static void main(String[] args) {
        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        int result = CompletableFuture.supplyAsync(()->{int fiboResult = sum();return fiboResult;}).join();
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：121 ms
```
源代码见HomeWorkUsingCompletableFuture.java

12. 使用CyclicBarrier，设置两个计数，主线先await，然后异步线程计算完成后在await，这样会唤醒主线程，获取数据，同时异步线程退出。
```
package java0.conc0303.homework;

import java.util.concurrent.CyclicBarrier;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 使用CyclicBarrier，设置两个计数，主线先await，然后异步线程计算完成后在await，这样会唤醒主线程，获取数据，同时异步线程退出。
 */
public class HomeWorkUsingCyclicBarrier {
    public static void main(String[] args) {
        //用于保存方法调用的结果
        final int[] fiboResult = new int[1];

        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
        new Thread(new Task(fiboResult, cyclicBarrier)).start();
        try {
            cyclicBarrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int result = fiboResult[0];
        // 确保  拿到result 并输出
        System.out.println("异步计算结果为："+result);

        System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");

        // 然后退出main线程
    }

    static class Task implements Runnable{
        final int[] result;
        final CyclicBarrier cyclicBarrier;
        Task(final int[] sum, CyclicBarrier cyclicBarrier) {
            this.result = sum;
            this.cyclicBarrier = cyclicBarrier;
        }

        public void run() {
            result[0] = sum();
            try {
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if ( a < 2)
            return 1;
        return fibo(a-1) + fibo(a-2);
    }
}
```
执行结果如下：
```
异步计算结果为：24157817
使用时间：71 ms
```
源代码见HomeWorkUsingCyclicBarrier.java

---
各方案运行结果
采用方案|运行时间
--|--
wait+notiry|124 ms
join|128 ms
sleep + interrupt|74 ms
yield|134 ms
yield + COW|133 ms
LockSupport|70 ms
Lock + Condition|128 ms
CountDownLatch|130 ms
Future|80 ms
FutureTask|137 ms
CompletableFuture|121 ms
CyclicBarrier|71 ms

根据上面运行结果，LockSupport、Future、Sleep+interrupt以及CyclicBarrier的方案运行时间相对较快，不过Sleep+interrupt的应用场景可能比较受限。而从编程使用看,CompletableFuture的方式代码表达最好，FutureTask方案代码也比较友好，LockSupport和yield不需要涉及异常处理，也很不错，但是yield的实际使用场景比较少。

## week4 必做作业题二：把多线程和并发相关知识带你梳理一遍，画一个脑图
使用xmind画了一个思维导图，源文件是Java_concurrent_programming_mindmap.xmind；导出的图片是Java_concurrent_programming_mindmap.png

![Java并发思维导图](https://github.com/mrzouwudi/JAVA-000/blob/main/Week_04/Java_concurrent_programming_mindmap.png)