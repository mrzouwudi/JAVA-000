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
