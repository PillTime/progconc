package pc.bqueue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Lock-free implementation of queue - unbounded variant.
 * 
 *
 * @param <E> Type of elements.
 */
public class LFBQueueU<E>  implements BQueue<E> {
  private E[] array;
  private final AtomicInteger head;
  private final AtomicInteger tail;
  private final AtomicBoolean resizing;
  private final AtomicBoolean addElementFlag;
  private final Rooms rooms;
  private final boolean useBackoff;

  /**
   * Constructor.
   * @param initialCapacity Initial queue capacity.
   * @param backoff Flag to enable/disable the use of back-off.
   * @throws IllegalArgumentException if {@code capacity <= 0}
   */
  @SuppressWarnings("unchecked")
  public LFBQueueU(int initialCapacity, boolean backoff) {
    head = new AtomicInteger(0);
    tail = new AtomicInteger(0);new AtomicMarkableReference<>(0, false);
    resizing = new AtomicBoolean(false);
    addElementFlag = new AtomicBoolean();
    array = (E[]) new Object[initialCapacity];
    useBackoff = backoff;
    rooms = new Rooms(3, backoff);
  }

  @Override
  public int capacity() {
    return UNBOUNDED;
  }
  
  @Override
  public int size() {
    rooms.enter(0);
    int size = tail.get() - head.get();
    rooms.leave(0);
    return size;
  }

  @Override
  public void add(E elem) {
    while(true) {
      rooms.enter(1);
      int p = tail.getAndIncrement();
      if (p - head.get() < array.length && !resizing.get()) {
        array[p % array.length] = elem;
        break;
      } else {
        // "resize"
        tail.getAndDecrement();
        while (resizing.getAndSet(true)) {
          if (useBackoff)
            Backoff.delay();
        }
        if (useBackoff)
          Backoff.reset();
        if (p - head.get() >= array.length) {
          E[] new_array = (E[]) new Object[array.length * 2];
          for (int i = head.get(); i < tail.get(); i++) {
            new_array[i % new_array.length] = array[i % array.length];
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
  public E remove() {
    E elem = null;
    while(true) {
      rooms.enter(2);
      int p = head.getAndIncrement();
      if (p < tail.get()) {
        int pos = p % array.length;
        elem = array[pos];
        array[pos] = null;
        break;
      } else {
        // "undo"
        head.getAndDecrement();
        rooms.leave(2);
        if (useBackoff)
          Backoff.delay();
      }
    }
    rooms.leave(2);
    if (useBackoff)
      Backoff.reset();
    return elem;
  }

  /**
   * Test instantiation.
   */
  public static final class Test extends BQueueTest {
    @Override
    <T> BQueue<T> createBQueue(int capacity) {
      return new LFBQueueU<>(capacity, false);
    }
  }
}
