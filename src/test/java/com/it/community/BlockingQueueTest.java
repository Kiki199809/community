package com.it.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author: KiKi
 * @date: 2021/10/4 - 13:57
 * @project_name：community
 * @description:
 */
public class BlockingQueueTest {
    public static void main(String[] args) {

        ArrayBlockingQueue queue = new ArrayBlockingQueue(10);
        // 一个生产者，三个消费者
        new Thread(new Producer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
    }
}

// 生产者：产生数据的线程
class Producer implements Runnable {

    private BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + "生产：" + queue.size());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// 消费者：使用数据的线程
class Consumer implements Runnable {

    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // 消费者消费能力没有生产者快
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName() + "消费：" + queue.size());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
