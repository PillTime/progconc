package pc.bdeque;

/**
 * Interface for deques unbounded capacity.
 * 
 * @param <E> Type of elements.
 */
public interface BDeque<E> {

  /**
   * Get the size of the bdeque.
   * @return The number of elements in the bdeque.
   */
  int size();
  
  /**
   * Add an element to the head of the bdeque.
   * 
   * @param elem Element to add.
   */
  void addFirst(E elem);
  
  /**
   * Remove an element from the head of the bdeque.
   * 
   * The operation MUST block the calling thread while the bdeque
   * is empty before returning.
   * 
   * @return Element removed from the bdeque.
   */
  E removeFirst();
  
  /**
   * Add an element to the tail of the bdeque.
   * 
   * @param elem Element to add.
   */
  void addLast(E elem);
  
  /**
   * Remove an element from the tail of the bdeque.
   * 
   * The operation MUST block the calling thread while the bdeque
   * is empty before returning.
   * 
   * @return Element removed from the bdeque.
   */
  E removeLast();
}
