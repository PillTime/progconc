package pc.bqueue;

import org.cooperari.CSystem;
import org.cooperari.config.CMaxTrials;
import org.cooperari.config.CRaceDetection;
import org.cooperari.config.CScheduling;
import org.cooperari.core.scheduling.CProgramStateFactory;
import org.cooperari.core.scheduling.CSchedulerFactory;
import org.cooperari.junit.CJUnitRunner;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

@SuppressWarnings("javadoc")
@RunWith(CJUnitRunner.class)
@CMaxTrials(30)
@CRaceDetection(false)
@CScheduling(schedulerFactory= CSchedulerFactory.MEMINI, stateFactory= CProgramStateFactory.RAW)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BDequeTest {
    abstract <T> BDeque<T> createBDeque(int capacity);

    @Test
    public void test1() {
        BDeque<Integer> d = createBDeque(6);
        AtomicInteger a = new AtomicInteger();
        AtomicInteger b = new AtomicInteger();
        AtomicInteger c = new AtomicInteger();
        AtomicInteger i = new AtomicInteger();
        AtomicInteger j = new AtomicInteger();
        AtomicInteger k = new AtomicInteger();
        AtomicInteger x = new AtomicInteger();
        AtomicInteger y = new AtomicInteger();
        AtomicInteger z = new AtomicInteger();
        CSystem.forkAndJoin(
                () -> d.addFirst(1),
                () -> d.addFirst(10),
                () -> d.addFirst(100)
        );
        assertEquals(3, d.size());
        CSystem.forkAndJoin(
                () -> d.addLast(1000),
                () -> d.addLast(10000),
                () -> d.addLast(100000)
        );
        CSystem.forkAndJoin(
                () -> a.set(d.size()),
                () -> b.set(d.size()),
                () -> c.set(d.size())
        );
        assertEquals(6, a.get());
        assertEquals(6, b.get());
        assertEquals(6, c.get());
        CSystem.forkAndJoin(
                () -> i.set(d.removeLast()),
                () -> j.set(d.removeLast()),
                () -> k.set(d.removeLast())
        );
        assertEquals(3, d.size());
        CSystem.forkAndJoin(
                () -> x.set(d.removeFirst()),
                () -> y.set(d.removeFirst()),
                () -> z.set(d.removeFirst())
        );
        assertEquals(0, d.size());
        assertEquals(111111, i.get() + j.get() + k.get() + x.get() + y.get() + z.get());
    }

    void test2(int capacity) {
        BDeque<Integer> d = createBDeque(capacity);
        AtomicInteger x = new AtomicInteger();
        AtomicInteger y = new AtomicInteger();
        AtomicInteger z = new AtomicInteger();
        AtomicInteger w = new AtomicInteger();
        CSystem.forkAndJoin(
                () -> d.addFirst(1),
                () -> d.addFirst(10),
                () -> d.addLast(100),
                () -> d.addLast(1000),
                () -> x.set(d.removeFirst()),
                () -> y.set(d.removeFirst()),
                () -> z.set(d.removeLast()),
                () -> w.set(d.removeLast())
        );
        assertEquals(0, d.size());
        assertEquals(1111, x.get() + y.get() + z.get() + w.get());
    }
    @Test
    public void test2_2() {
        test2(2);
    }
    @Test
    public void test2_4() {
        test2(4);
    }

    void test3(int capacity) {
        BDeque<Integer> d = createBDeque(capacity);
        AtomicInteger i = new AtomicInteger();
        AtomicInteger j = new AtomicInteger();
        AtomicInteger k = new AtomicInteger();
        AtomicInteger x = new AtomicInteger();
        AtomicInteger y = new AtomicInteger();
        AtomicInteger z = new AtomicInteger();
        CSystem.forkAndJoin(
                () -> d.addFirst(1),
                () -> d.addFirst(10),
                () -> d.addFirst(100),
                () -> d.addLast(1000),
                () -> d.addLast(10000),
                () -> d.addLast(100000),
                () -> i.set(d.removeFirst()),
                () -> j.set(d.removeFirst()),
                () -> k.set(d.removeFirst()),
                () -> x.set(d.removeLast()),
                () -> y.set(d.removeLast()),
                () -> z.set(d.removeLast())
        );
        assertEquals(0, d.size());
        assertEquals(111111, i.get() + j.get() + k.get() + x.get() + y.get() + z.get());
    }
    @Test
    public void test3_2() {
        test3(2);
    }
    @Test
    public void test3_4() {
        test3(4);
    }
    @Test
    public void test3_6() {
        test3(6);
    }

    void test4(int capacity) {
        BDeque<Integer> d = createBDeque(capacity);
        AtomicInteger x = new AtomicInteger();
        AtomicInteger y = new AtomicInteger();
        AtomicInteger z = new AtomicInteger();
        AtomicInteger w = new AtomicInteger();
        CSystem.forkAndJoin(
                () -> d.addFirst(1),
                () -> d.addFirst(10),
                () -> d.addLast(100),
                () -> d.addLast(1000),
                () -> {
                    int n;
                    while ((n = d.size()) == 0);
                    x.set(d.removeFirst());
                    y.set(d.removeFirst());
                    z.set(d.removeLast());
                    w.set(d.removeLast());
                }
        );
        assertEquals(0, d.size());
        assertEquals(1111,x.get() + y.get() + z.get() + w.get());
    }
    @Test
    public void test4_2() {
        test4(2);
    }
    @Test
    public void test4_4() {
        test4(4);
    }

    void test5(int capacity) {
        BDeque<Integer> d = createBDeque(capacity);
        AtomicInteger i = new AtomicInteger();
        AtomicInteger j = new AtomicInteger();
        AtomicInteger k = new AtomicInteger();
        AtomicInteger x = new AtomicInteger();
        AtomicInteger y = new AtomicInteger();
        AtomicInteger z = new AtomicInteger();
        CSystem.forkAndJoin(
                () -> { d.addFirst(1); d.addFirst(10); d.addFirst(100); d.addFirst(1000); d.addFirst(10000); d.addFirst(100000); },
                () -> i.set(d.removeFirst()),
                () -> j.set(d.removeFirst()),
                () -> k.set(d.removeFirst()),
                () -> x.set(d.removeLast()),
                () -> y.set(d.removeLast()),
                () -> z.set(d.removeLast())
        );
        assertEquals(0, d.size());
        assertEquals(111111, i.get() + j.get() + k.get() + x.get() + y.get() + z.get());
    }
    @Test
    public void test5_2() {
        test5(2);
    }
    @Test
    public void test5_4() {
        test5(4);
    }
    @Test
    public void test5_6() {
        test5(6);
    }

    void test6(int capacity) {
        BDeque<Integer> d = createBDeque(capacity);
        AtomicInteger i = new AtomicInteger();
        AtomicInteger j = new AtomicInteger();
        AtomicInteger k = new AtomicInteger();
        AtomicInteger x = new AtomicInteger();
        AtomicInteger y = new AtomicInteger();
        AtomicInteger z = new AtomicInteger();
        CSystem.forkAndJoin(
                () -> { i.set(d.removeFirst()); j.set(d.removeFirst()); k.set(d.removeFirst()); x.set(d.removeLast()); y.set(d.removeLast()); z.set(d.removeLast()); },
                () -> d.addFirst(1),
                () -> d.addFirst(10),
                () -> d.addFirst(100),
                () -> d.addLast(1000),
                () -> d.addLast(10000),
                () -> d.addLast(100000)
        );
        assertEquals(0, d.size());
        assertEquals(111111, i.get() + j.get() + k.get() + x.get() + y.get() + z.get());
    }
    @Test
    public void test6_2() {
        test6(2);
    }
    @Test
    public void test6_4() {
        test6(4);
    }
    @Test
    public void test6_6() {
        test6(6);
    }

    void test7(int capacity) {
        BDeque<Integer> d = createBDeque(capacity);
        AtomicInteger i = new AtomicInteger();
        AtomicInteger j = new AtomicInteger();
        AtomicInteger k = new AtomicInteger();
        AtomicInteger l = new AtomicInteger();
        CSystem.forkAndJoin(
                () -> {
                    d.addLast(1);
                    d.addLast(2);
                    d.addLast(3);
                    d.addLast(4);
                    i.set(d.size());
                },
                () -> {
                    j.set(d.removeFirst());
                    k.set(d.removeFirst());
                    l.set(d.size());
                }
        );
        assertEquals(2, d.size());
        assertTrue(i.get() >= 2 && i.get() <= 4);
        assertEquals(1, j.get());
        assertEquals(2, k.get());
        assertTrue(l.get() >= 0 && l.get() <= 2);
        CSystem.forkAndJoin(
                () -> {
                    d.addFirst(5);
                    d.addFirst(6);
                    d.addFirst(7);
                    d.addFirst(8);
                    i.set(d.size());
                },
                () -> {
                    j.set(d.removeLast());
                    k.set(d.removeLast());
                    l.set(d.size());
                }
        );
        assertEquals(4, d.size());
        assertTrue(i.get() >= 4 && i.get() <= 6);
        assertEquals(4, j.get());
        assertEquals(3, k.get());
        assertTrue(l.get() >= 0 && l.get() <= 4);
        CSystem.forkAndJoin(
                () -> {
                    d.addLast(9);
                    i.set(d.size());
                },
                () -> {
                    j.set(d.removeFirst());
                    k.set(d.removeFirst());
                    l.set(d.size());
                }
        );
        assertEquals(3, d.size());
        assertTrue(i.get() >= 3 && i.get() <= 5);
        assertEquals(8, j.get());
        assertEquals(7, k.get());
        assertTrue(l.get() >= 2 && l.get() <= 3);
        CSystem.forkAndJoin(
                () -> {
                    d.addFirst(10);
                    i.set(d.size());
                },
                () -> {
                    j.set(d.removeLast());
                    k.set(d.removeLast());
                    l.set(d.size());
                }
        );
        assertEquals(2, d.size());
        assertTrue(i.get() >= 2 && i.get() <= 4);
        assertEquals(9, j.get());
        assertEquals(5, k.get());
        assertTrue(l.get() >= 1 && l.get() <= 2);
        CSystem.forkAndJoin(
                () -> {
                    i.set(d.size());
                },
                () -> {
                    j.set(d.removeFirst());
                    d.addLast(11);
                    k.set(d.removeFirst());
                    l.set(d.size());
                }
        );
        assertEquals(1, d.size());
        assertTrue(i.get() >= 1 && i.get() <= 2);
        assertEquals(10, j.get());
        assertEquals(6, k.get());
        assertEquals(1, l.get());
        CSystem.forkAndJoin(
                () -> {
                    i.set(d.size());
                },
                () -> {
                    j.set(d.removeLast());
                    d.addFirst(12);
                    k.set(d.removeLast());
                    l.set(d.size());
                }
        );
        assertEquals(0, d.size());
        assertTrue(i.get() >= 0 && i.get() <= 1);
        assertEquals(11, j.get());
        assertEquals(12, k.get());
        assertEquals(0, l.get());
    }
    @Test
    public void test7_8() {
        test7(8);
    }
    @Test
    public void test7_16() {
        test7(16);
    }

    @Test
    public void test8() {
        BDeque<Integer> d = createBDeque(6);
        AtomicInteger i = new AtomicInteger();
        AtomicInteger j = new AtomicInteger();
        AtomicInteger k = new AtomicInteger();
        AtomicInteger l = new AtomicInteger();
        AtomicInteger x = new AtomicInteger();
        AtomicInteger y = new AtomicInteger();
        AtomicInteger z = new AtomicInteger();
        AtomicInteger w = new AtomicInteger();
        CSystem.forkAndJoin(
                () -> d.addFirst(1),
                () -> d.addFirst(10),
                () -> d.addFirst(100),
                () -> d.addFirst(1000),
                () -> d.addLast(10000),
                () -> d.addLast(100000),
                () -> d.addLast(1000000),
                () -> d.addLast(10000000),
                () -> i.set(d.removeFirst()),
                () -> j.set(d.removeFirst()),
                () -> k.set(d.removeFirst()),
                () -> l.set(d.removeFirst()),
                () -> x.set(d.removeLast()),
                () -> y.set(d.removeLast()),
                () -> z.set(d.removeLast()),
                () -> w.set(d.removeLast())
        );
        assertEquals(0, d.size());
        assertEquals(11111111, i.get() + j.get() + k.get() + l.get() + x.get() + y.get() + z.get() + w.get());
    }

    @Test @Ignore
    public void test9() {

    }
}
