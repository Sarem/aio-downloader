package ir.barantelecom.downloader;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class SwingWorkerExample {
  private static JProgressBar PROGRESS_BAR;
  private static JLabel OUTPUT_LABEL;

  public SwingWorkerExample() {
  }

  private static JFrame createGUI() {
    JFrame testFrame = new JFrame("TestFrame");
    PROGRESS_BAR = new JProgressBar();
    PROGRESS_BAR.setMinimum(0);
    PROGRESS_BAR.setMaximum(100);
    OUTPUT_LABEL = new JLabel("Processing");
    testFrame.getContentPane().add(PROGRESS_BAR, "Center");
    testFrame.getContentPane().add(OUTPUT_LABEL, "South");
    testFrame.getContentPane().add(new JCheckBox("Click me to proof UI is responsive"), "North");
    testFrame.setDefaultCloseOperation(3);
    return testFrame;
  }

  public static void main(String[] args) throws InvocationTargetException, InterruptedException {
    EventQueue.invokeAndWait(new Runnable() {
      public void run() {
        JFrame frame = SwingWorkerExample.createGUI();
        frame.pack();
        frame.setVisible(true);
      }
    });
    SwingWorkerExample.MySwingWorker worker = new SwingWorkerExample.MySwingWorker(PROGRESS_BAR, OUTPUT_LABEL);
    worker.execute();
  }

  private static class MySwingWorker extends SwingWorker<String, Double> {
    private final JProgressBar fProgressBar;
    private final JLabel fLabel;

    private MySwingWorker(JProgressBar aProgressBar, JLabel aLabel) {
      this.fProgressBar = aProgressBar;
      this.fLabel = aLabel;
    }

    protected String doInBackground() throws Exception {
      int maxNumber = 10;

      for(int i = 0; i < maxNumber; ++i) {
        Thread.sleep(2000L);
        double factor = (double)(i + 1) / (double)maxNumber;
        System.out.println("Intermediate results ready");
        this.publish(new Double[]{factor});
      }

      return "Finished";
    }

    protected void process(List<Double> aDoubles) {
      int amount = this.fProgressBar.getMaximum() - this.fProgressBar.getMinimum();
      this.fProgressBar.setValue((int)((double)this.fProgressBar.getMinimum() + (double)amount * (Double)aDoubles.get(aDoubles.size() - 1)));
    }

    protected void done() {
      try {
        this.fLabel.setText((String)this.get());
      } catch (InterruptedException var2) {
        var2.printStackTrace();
      } catch (ExecutionException var3) {
        var3.printStackTrace();
      }

    }
  }
}
