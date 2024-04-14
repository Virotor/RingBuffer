import com.lessons.AbstractRingBuffer;
import com.lessons.SyncRingBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.Stream;

public class TestThread {

    private static final int THREADS_COUNT = 10;
    private static final int SIZE = THREADS_COUNT * 2;

    static final int JMP = 10000;
    AbstractRingBuffer<Integer> abstractRingBuffer;

    static List<Integer> res;

    @Before
    public void init() {
        abstractRingBuffer = new SyncRingBuffer<>(new ArrayList<>(SIZE), SIZE);
        res = Collections.synchronizedList(new ArrayList<>(THREADS_COUNT));
    }

    private static Runnable get(AbstractRingBuffer<Integer> abstractRingBuffer, CyclicBarrier barrier) {
        return () -> {
            for (int i = 0; i < JMP; i++) {
                try {
                    barrier.await();
                    res.add(abstractRingBuffer.getNext());
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }

        };
    }


    private static Runnable set(AbstractRingBuffer<Integer> abstractRingBuffer, CyclicBarrier barrier, int value) {
        return () -> {
            for (int i = 0; i < JMP; i++) {
                try {
                    barrier.await();
                    abstractRingBuffer.add(value);
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }


            }

        };
    }

    private static Runnable setSize(AbstractRingBuffer<Integer> abstractRingBuffer, CyclicBarrier barrier, int value) {
        return () -> {
            for (int i = 0; i < JMP; i++) {
                try {
                    barrier.await();
                    abstractRingBuffer.add(value);
                    barrier.await();
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }


            }

        };
    }

    @Test
    public void test() throws BrokenBarrierException, InterruptedException {
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
            barrierSet.await();
            barrierGet.await();
            barrierGet.await();

            Assert.assertEquals(THREADS_COUNT, res.stream().distinct().count());
            res.clear();

        }
        for (var thread : threadList) {
            thread.join();
        }
    }


    @Test
    public void testSecond() throws BrokenBarrierException, InterruptedException {

        CyclicBarrier barrierSet = new CyclicBarrier(THREADS_COUNT + 1);


        List<Thread> threadsSet = Stream.iterate(0, (e) -> e + 1).map(e -> new Thread(set(abstractRingBuffer, barrierSet, e)))
                .limit(THREADS_COUNT)
                .peek(Thread::start)
                .toList();

        List<Thread> threadList = new ArrayList<>(threadsSet);

        for (int i = 0; i < JMP; i++) {

            barrierSet.await();
            barrierSet.await();
            res.addAll(abstractRingBuffer);
            Assert.assertEquals(THREADS_COUNT, res.stream().filter(Objects::nonNull).distinct().count());
            res.clear();

        }
        for (var thread : threadList) {
            thread.join();
        }
    }

    @Test
    public void testSize() throws BrokenBarrierException, InterruptedException {

        CyclicBarrier barrierSet = new CyclicBarrier(THREADS_COUNT + 1);
        List<Thread> threadsSet = Stream.iterate(0, (e) -> e + 1).map(e -> new Thread(setSize(abstractRingBuffer, barrierSet, e)))
                .limit(THREADS_COUNT)
                .peek(Thread::start)
                .toList();

        List<Thread> threadList = new ArrayList<>(threadsSet);

        for (int i = 0; i < JMP; i++) {

            barrierSet.await();
            barrierSet.await();
            Assert.assertEquals(THREADS_COUNT, abstractRingBuffer.size());
            abstractRingBuffer.clear();
            barrierSet.await();
        }
        for (var thread : threadList) {
            thread.join();
        }
    }

}
