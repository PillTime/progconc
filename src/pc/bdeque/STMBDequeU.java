package pc.bdeque;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.TArray;
import scala.concurrent.stm.japi.STM;

public class STMBDequeU<E> implements BDeque<E> {

    private final Ref.View<Integer> size;
    private final Ref.View<Integer> head;
    private final Ref.View<TArray.View<E>> arrayRef;

    /**
     * Constructor.
     * @param initialCapacity Initial queue capacity.
     * @throws IllegalArgumentException if {@code capacity <= 0}
     */
    public STMBDequeU(int initialCapacity) {
        if (initialCapacity <= 0)
            throw new IllegalArgumentException();
        size = STM.newRef(0);
        head = STM.newRef(0);
        arrayRef = STM.newRef(STM.newTArray(initialCapacity));
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public void addFirst(E elem) {
        STM.atomic(() -> {
            if(size.get() == arrayRef.get().size()) {
                TArray.View<E> new_array = STM.newTArray(size.get() * 2);

                //avançar para a frente
                for (int i = head.get(); i < size.get(); i++) {
                    new_array.update(i + 1, arrayRef.get().apply(i));
                }

                arrayRef.set(new_array);

                arrayRef.get().update(head.get(), elem);

            }else{
                //avançar para a frente
                for (int i = size.get()-1; i >= head.get(); i--) {
                    arrayRef.get().update(i + 1, arrayRef.get().apply(i));
                }

                arrayRef.get().update(head.get(), elem);

            }

            STM.increment(size, 1);
        });
    }

    @Override
    public E removeFirst() {
        return STM.atomic(() -> {
            if (size.get() == 0) {
                STM.retry();
            }

            E elem = arrayRef.get().apply(head.get());

            //saltar elemento
            for( int i = head.get(); i < size.get()-1 ; i++){
                arrayRef.get().update(i, arrayRef.get().apply(i+1));
            }

            STM.increment(size, -1);

            return elem;
        });
    }

    @Override
    public void addLast(E elem) {
        STM.atomic(() -> {
            if(size.get() == arrayRef.get().size()) {
                TArray.View<E> new_array = STM.newTArray(size.get() * 2);

                for (int i = head.get(); i < size.get(); i++) {
                    new_array.update(i, arrayRef.get().apply(i));
                }

                arrayRef.set(new_array);
            }
            arrayRef.get().update((head.get() + size.get()) % arrayRef.get().length(), elem);

            STM.increment(size, 1);
        });
    }

    @Override
    public E removeLast() {
        return STM.atomic(() -> {
            if (size.get() == 0) {
                STM.retry();
            }
            E elem = arrayRef.get().apply(head.get() + size.get() -1);

            STM.increment(size, -1);

            return elem;
        });
    }

    /**
     * Test instantiation (do not run in cooperative mode).
     */
    public static final class Test extends BDequeTest {
        @Override
        <T> BDeque<T> createBDeque(int initialCapacity) {
            return new STMBDequeU<>(initialCapacity);
        }
    }
}
