package ir.barantelecom.downloader;

import java.awt.Component;
import java.awt.Toolkit;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Runner {
  public Runner() {
  }

  public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    final JFrame loginFrame = new JFrame("Login - AIO Downloader");
    loginFrame.setIconImages(Arrays.asList(Toolkit.getDefaultToolkit().getImage("./img/dl_16.png"), Toolkit.getDefaultToolkit().getImage("./img/dl_32.png"), Toolkit.getDefaultToolkit().getImage("./img/dl_64.png"), Toolkit.getDefaultToolkit().getImage("./img/dl_128.png")));
    loginFrame.setContentPane((new LoginForm() {
      public void closeCurrentFrameAndOpenNextOn() {
        loginFrame.dispose();
        JFrame mainFrame = new JFrame("AIO Downloader");
        mainFrame.setContentPane((new MainForm()).getMainFormPanel());
        mainFrame.setDefaultCloseOperation(3);
        mainFrame.setLocationRelativeTo((Component)null);
        mainFrame.pack();
        mainFrame.setVisible(true);
      }
    }).getMainLoginPanel());
    loginFrame.setDefaultCloseOperation(3);
    loginFrame.setLocationRelativeTo((Component)null);
    loginFrame.pack();
    loginFrame.setVisible(true);
  }
}
