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
  private enum RoomType {
    Size, Add, Remove;

    private final int getId() {
      switch (this) {
        case Size:
          return 0;
        case Add:
          return 1;
        case Remove:
          return 2;
        default:
          return -1;
      }
    }
  };

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
    rooms.enter(RoomType.Size.getId());
    int size = tail.get() - head.get();
    rooms.leave(RoomType.Size.getId());
    return size;
  }

  @Override
  public void add(E elem) {
    while(true) {
      rooms.enter(RoomType.Add.getId());
      int p = tail.getAndIncrement();
      if (p - head.get() < array.length && !resizing.get()) {
        array[p % array.length] = elem;
        break;
      } else {
        // "resize"
        while (resizing.getAndSet(true));
        if (p - head.get() >= array.length) {
          E[] new_array = (E[]) new Object[array.length * 2];
          for (int i = head.get(); i < tail.get(); i++) {
            new_array[i % new_array.length] = array[i % array.length];
          }
          array = new_array;
        }
        tail.getAndDecrement();
        resizing.set(false);
        rooms.leave(RoomType.Add.getId());
      }
    }
    rooms.leave(RoomType.Add.getId());
  }
  
  @Override
  public E remove() {
    E elem = null;
    while(true) {
      rooms.enter(RoomType.Remove.getId());
      int p = head.getAndIncrement();
      if (p < tail.get()) {
        int pos = p % array.length;
        elem = array[pos];
        array[pos] = null;
        break;
      } else {
        // "undo"
        head.getAndDecrement();
        rooms.leave(RoomType.Remove.getId());
      }
    }
    rooms.leave(RoomType.Remove.getId());
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
