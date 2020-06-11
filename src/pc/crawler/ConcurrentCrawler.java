package pc.crawler;


import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import pc.util.UnexpectedException;

// TODO program it!

/**
 * Concurrent crawler.
 *
 */
public class ConcurrentCrawler extends SequentialCrawler {

  public static void main(String[] args) throws IOException {
    int threads = args.length > 0 ?  Integer.parseInt(args[0]) : 4;
    String url = args.length > 1 ? args[1] : "http://localhost:8123";

    ConcurrentCrawler cc = new ConcurrentCrawler(threads);
    cc.setVerboseOutput(true);
    cc.crawl(url);
    cc.stop();
  }

  /**
   * The fork-join pool.
   */
  private final ForkJoinPool pool;

  /**
   * Constructor.
   * @param threads number of threads.
   * @throws IOException if an I/O error occurs
   */
  public ConcurrentCrawler(int threads) throws IOException {
    pool = new ForkJoinPool(threads);
  }

  @Override
  public void crawl(String root) {
    long t = System.currentTimeMillis();
    log("Starting at %s", root);
    pool.invoke(new TransferTask(new AtomicInteger(0), root,new HashSet<>()));
    t = System.currentTimeMillis() - t;
    log("Done in %d ms", t);
  }

  /**
   * Stop the crawler.
   */
  public void stop() {
    pool.shutdown();
  }
  
  @SuppressWarnings("serial")
  private class TransferTask extends RecursiveTask<Void> {

    final AtomicInteger rid;
    final String path;
    HashSet<String> visited;

    TransferTask(AtomicInteger rid, String path, HashSet<String> visited) {
      super();
      this.rid = rid;
      this.path = path;
      this.visited = visited;// manter estado dos visitados e evitar loops infinitos
    }

    @Override
    protected Void compute() {
      List<String> folders =  new LinkedList<>();
      List<String> foldersAwx = new LinkedList<>();

      try {
        List<String> links = performTransfer(rid.getAndIncrement(), new URL(path));

        //usar Fork Join Pool com recursividade
        List<RecursiveTask<Void>> forks = new LinkedList<>();

        //forks
        for(String link : links){
          String newURL = new URL(new URL(path), new URL(new URL(path),link).getPath()).toString();
          //saltar já visitados
          if(visited.contains(newURL))
            continue;
          visited.add(newURL);
          TransferTask task = new TransferTask(rid,newURL,visited);
          forks.add(task);
          task.fork();

        }

        //joins
        for(RecursiveTask<Void> task : forks){
         task.join();
        }


      } 
      catch (Exception e) {
        throw new UnexpectedException(e);
      }
      return null;
    }

  }
}
