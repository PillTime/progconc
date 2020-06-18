package pc.bdeque;

import pc.util.Benchmark;
import pc.util.Benchmark.BThread;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Benchmark program for stack implementations.
 */
public class BDequeBenchmark {

  private static final int DURATION = 5;
  private static final int MAX_THREADS = 32;

  /**
   * Program to run a benchmark over deque implementations.
   * @param args Arguments are ignored.
   */
  public static void main(String[] args) {
    //double serial = runBenchmark(1, new UStack<Integer>());

    for (int t = 2; t <= MAX_THREADS; t = t * 2) {
      runBenchmark("Lock-free backoff=y", t, new LFBDequeU<Integer>(MAX_THREADS,true));
      runBenchmark("Lock-free backoff=n", t, new LFBDequeU<Integer>(MAX_THREADS,false));
      runBenchmark("STM", t, new STMBDequeU<Integer>(MAX_THREADS));
    }
  }

  private static void runBenchmark(String desc, int threads, BDeque<Integer> q) {
    Benchmark b = new Benchmark(threads, DURATION, new BDequeOperation(q));
    System.out.printf("%2d,%20s,%11s -> ", threads, desc, q.getClass().getSimpleName());
    System.out.printf("%10.2f thousand ops/s per thread%n", b.run());
  }

  private static class BDequeOperation implements Benchmark.Operation {
    private final BDeque<Integer> deque;
    
    BDequeOperation(BDeque<Integer> q) {
      this.deque = q;
    }

    public void teardown() {
      for (int i = 0 ; i <= MAX_THREADS; i++) {
        deque.addFirst(i);
        deque.addLast(i);
      }
      while (deque.size() > 0) {
        deque.removeFirst();
        deque.removeLast();
      }
    }
    
    @Override
    public void step() {
      BThread t = (BThread) Thread.currentThread();
      ThreadLocalRandom rng = ThreadLocalRandom.current();
      int role = t.getTId() % 2;
      if (role == 0) {
        int v = rng.nextInt(100);
        deque.addFirst(v);
        deque.addLast(v);
      } else if (deque.size() > MAX_THREADS){
        deque.removeFirst();
        deque.removeLast();
      }
    }
  }
}
