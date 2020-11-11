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
