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
