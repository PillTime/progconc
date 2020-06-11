package pc.bqueue;

import org.cooperari.CSystem;
import org.cooperari.config.CMaxTrials;
import org.cooperari.config.CRaceDetection;
import org.cooperari.config.CScheduling;
import org.cooperari.core.scheduling.CProgramStateFactory;
import org.cooperari.core.scheduling.CSchedulerFactory;
import org.cooperari.junit.CJUnitRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

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
        assertEquals(i.get() + j.get() + k.get() + x.get() + y.get() + z.get(), 111111);
    }
}
