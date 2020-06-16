package pc.bqueue;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import pc.bdeque.LFBDequeU;
import pc.bdeque.STMBDequeU;


// Uncomment lines as you find suitable.
@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({ 
  MBQueue.Test.class,
  MBQueueU.Test.class,
  LFBQueue.Test.class,
  LFBQueueU.Test.class,
  LFBDequeU.Test.class,
  // Run STM-based tests only in preemptive mode using cjunitp.sh.
  // Cooperari cannot handle STM-based code with cooperative semantics.
  //STMBQueue.Test.class,
  //STMBQueueU.Test.class,
  //STMBDequeU.Test.class,
})
public class RunTests {

}
