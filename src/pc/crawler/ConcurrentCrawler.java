package pc.crawler;


import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

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
    int rid = 0;
    log("Starting at %s", root);
    pool.invoke(new TransferTask(0, root));
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

    final int rid;
    final String path;

    TransferTask(int rid, String path) {
      this.rid = rid;
      this.path = path;
    }

    @Override
    protected Void compute() {

      String tasksResult = "";
      //TODO lógica inerente ao estado partilhado entre tarefas, eu não sei se já estou a fazer isso...

      try {
        List<String> links = performTransfer(rid, new URL(path));

        //usar Fork Join Pool com recursividade
        List<RecursiveTask<Void>> forks = new LinkedList<>();

        //forks
        for(String link : links){
          TransferTask task = new TransferTask(rid,link);
          forks.add(task);
          task.fork();
        }

        //joins
        for(RecursiveTask<Void> task : forks){
          //eu não sei bem como juntar os resultados
          tasksResult += task.join() + " ";
          log(tasksResult);
        }

      } 
      catch (Exception e) {
        throw new UnexpectedException(e);
      }
      return null;
    }

  }
}
