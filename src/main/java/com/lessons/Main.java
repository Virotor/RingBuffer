package com.lessons;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.Stream;

public class Main {

    private static final int THREADS_COUNT = 10;
    private static final int SIZE = THREADS_COUNT * 5;

    static final int JMP = 10000;

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        AbstractRingBuffer<Integer> abstractRingBuffer = new SyncRingBuffer<>(new ArrayList<>(SIZE), SIZE);

        CyclicBarrier barrierGet = new CyclicBarrier(THREADS_COUNT + 1);
        CyclicBarrier barrierSet = new CyclicBarrier(THREADS_COUNT + 1);
        Runnable taskGet = get(abstractRingBuffer, barrierGet);


        List<Thread> threadsGet = Stream.generate(() -> new Thread(taskGet)).limit(THREADS_COUNT).peek(Thread::start).toList();
        List<Thread> threadsSet = Stream.iterate(0, (e) -> e + 1).map(e -> new Thread(set(abstractRingBuffer, barrierSet, e)))
                .limit(THREADS_COUNT)
                .peek(Thread::start)
                .toList();

        List<Thread> threadList = new ArrayList<>(threadsGet);
        threadList.addAll(threadsSet);
        for (int i = 0; i < JMP; i++) {
            barrierSet.await();
            barrierGet.await();

        }

        for (var thread : threadList) {
            thread.join();
        }

    }

    private static Runnable get(AbstractRingBuffer<Integer> abstractRingBuffer, CyclicBarrier barrier) {
        return () -> {
            for (int i = 0; i < JMP; i++) {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(abstractRingBuffer.getNext());

            }

        };
    }


    private static Runnable set(AbstractRingBuffer<Integer> abstractRingBuffer, CyclicBarrier barrier, int value) {
        return () -> {
            for (int i = 0; i < JMP; i++) {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                abstractRingBuffer.add(value);

            }

        };
    }
}