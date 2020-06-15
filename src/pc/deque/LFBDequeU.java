package pc.deque;

import pc.bqueue.Backoff;
import pc.bqueue.Rooms;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LFBDequeU<E> implements BDeque<E> {
    private E[] array;
    private final AtomicInteger head;
    private final AtomicInteger tail;
    private final AtomicBoolean resizing;
    private final Rooms rooms;
    private final boolean useBackoff;

    @SuppressWarnings("unchecked")
    public LFBDequeU(int initialCapacity, boolean backoff) {
        head = new AtomicInteger(0);
        tail = new AtomicInteger(0);
        resizing = new AtomicBoolean(false);
        array = (E[]) new Object[initialCapacity];
        useBackoff = backoff;
        rooms = new Rooms(5, backoff);
    }

    @Override
    public int size() {
        rooms.enter(0);
        int size = tail.get() - head.get();
        rooms.leave(0);
        return size;
    }

    @Override
    public void addFirst(E elem) {
        while (true) {
            rooms.enter(1);
            int p = head.decrementAndGet();
            if (tail.get() - p <= array.length && !resizing.get()) {
                array[((p % array.length) + array.length) % array.length] = elem;
                break;
            }
            else {
                head.incrementAndGet();
                while (resizing.getAndSet(true)) {
                    if (useBackoff) {
                        Backoff.delay();
                    }
                }
                if (useBackoff) {
                    Backoff.reset();
                }
                if (tail.get() - p >= array.length) {
                    E[] new_array = (E[]) new Object[array.length * 2];
                    for (int i = head.get(); i < tail.get(); i++) {
                        new_array[((i % new_array.length) + new_array.length) % new_array.length] = array[((i % array.length) + array.length) % array.length];
                    }
                    array = new_array;
                }
                resizing.set(false);
                rooms.leave(1);
            }
        }
        rooms.leave(1);
    }

    @Override
    public E removeFirst() {
        E elem = null;
        while(true) {
            rooms.enter(2);
            int p = head.getAndIncrement();
            if (p < tail.get()) {
                p = ((p % array.length) + array.length) % array.length;
                elem = array[p];
                array[p] = null;
                break;
            }
            else {
                head.getAndDecrement();
                rooms.leave(2);
                if (useBackoff) {
                    Backoff.delay();
                }
            }
        }
        rooms.leave(2);
        if (useBackoff) {
            Backoff.reset();
        }
        return elem;
    }

    @Override
    public void addLast(E elem) {
        while (true) {
            rooms.enter(3);
            int p = tail.getAndIncrement();
            if (p - head.get() < array.length && !resizing.get()) {
                array[((p % array.length) + array.length) % array.length] = elem;
                break;
            }
            else {
                tail.getAndDecrement();
                while (resizing.getAndSet(true)) {
                    if (useBackoff) {
                        Backoff.delay();
                    }
                }
                if (useBackoff) {
                    Backoff.reset();
                }
                if (p - head.get() >= array.length) {
                    E[] new_array = (E[]) new Object[array.length * 2];
                    for (int i = head.get(); i < tail.get(); i++) {
                        new_array[((i % new_array.length) + new_array.length) % new_array.length] = array[((i % array.length) + array.length) % array.length];
                    }
                    array = new_array;
                }
                resizing.set(false);
                rooms.leave(3);
            }
        }
        rooms.leave(3);
    }

    @Override
    public E removeLast() {
        E elem = null;
        while(true) {
            rooms.enter(4);
            int p = tail.decrementAndGet();
            if (p >= head.get()) {
                p = ((p % array.length) + array.length) % array.length;
                elem = array[p];
                array[p] = null;
                break;
            }
            else {
                tail.incrementAndGet();
                rooms.leave(4);
                if (useBackoff) {
                    Backoff.delay();
                }
            }
        }
        rooms.leave(4);
        if (useBackoff) {
            Backoff.reset();
        }
        return elem;
    }

    public static final class Test extends BDequeTest {
        @Override
        <T> BDeque<T> createBDeque(int capacity) {
            return new LFBDequeU<>(capacity, false);
        }
    }
}
