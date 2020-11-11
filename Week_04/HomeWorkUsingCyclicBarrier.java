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
