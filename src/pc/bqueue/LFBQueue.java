package pc.bqueue;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lock-free implementation of queue. 
 * 
 *
 * @param <E> Type of elements.
 */
public class LFBQueue<E> implements BQueue<E> {
  private enum RoomType {
    Size, Add, Remove;

    private final int getRoomTypeId() {
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
  private final AtomicInteger head, tail;
  // TODO make use of Rooms!
  private final Rooms rooms;
  // TODO prepare code for back-off
  private final boolean useBackoff;

  /**
   * Constructor.
   * @param initialCapacity Initial queue capacity.
   * @param backoff Flag to enable/disable the use of back-off.
   * @throws IllegalArgumentException if {@code capacity <= 0}
   */
  @SuppressWarnings("unchecked")
  public LFBQueue(int initialCapacity, boolean backoff) {
    head = new AtomicInteger(0);
    tail = new AtomicInteger(0);
    array = (E[]) new Object[initialCapacity];
    useBackoff = backoff;
    rooms = new Rooms(3, backoff);
  }

  @Override
  public int capacity() {
    return array.length;
  }

  @Override
  public int size() {
    rooms.enter(RoomType.Size.getRoomTypeId());
    int size = tail.get() - head.get();
    rooms.leave(RoomType.Size.getRoomTypeId());
    return size;
  }

  @Override
  public void add(E elem) {
    while(true) {
      rooms.enter(RoomType.Add.getRoomTypeId());
      int p = tail.getAndIncrement();
      if (p - head.get() < array.length) {
        array[p % array.length] = elem;
        break;
      } else {
        // "undo"
        tail.getAndDecrement();
        rooms.leave(RoomType.Add.getRoomTypeId());
      }
    }
    rooms.leave(RoomType.Add.getRoomTypeId());
  }

  @Override
  public E remove() {
    E elem = null;
    while(true) {
      rooms.enter(RoomType.Remove.getRoomTypeId());
      int p = head.getAndIncrement();
      if (p < tail.get()) {
        int pos = p % array.length;
        elem = array[pos];
        array[pos] = null;
        break;
      } else {
        // "undo"
        head.getAndDecrement();
        rooms.leave(RoomType.Remove.getRoomTypeId());
      }
    }
    rooms.leave(RoomType.Remove.getRoomTypeId());
    return elem;
  }

  /**
   * Test instantiation.
   */
  public static final class Test extends BQueueTest {
    @Override
    <T> BQueue<T> createBQueue(int capacity) {
      return new LFBQueue<>(capacity, true);
    }
  }
}
