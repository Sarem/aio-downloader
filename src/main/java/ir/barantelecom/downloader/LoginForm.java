package ir.barantelecom.downloader;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.PlainDocument;

public abstract class LoginForm {
  private JPanel mainLoginPanel;
  private JTextField usernameTxt;
  private JButton loginButton;
  private JPasswordField passwordTxt;

  public LoginForm() {
    this.$$$setupUI$$$();
    if (Config.DEFAULT_CELLPHONE != null && !Config.DEFAULT_CELLPHONE.trim().isEmpty()) {
      this.usernameTxt.setText(Config.DEFAULT_CELLPHONE);
    }

    if (Config.DEFAULT_PASSWORD != null && !Config.DEFAULT_PASSWORD.trim().isEmpty()) {
      this.passwordTxt.setText(Config.DEFAULT_PASSWORD);
    }

    ((PlainDocument)this.usernameTxt.getDocument()).setDocumentFilter(new MyNumberFilter());
    this.loginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String username = LoginForm.this.usernameTxt.getText();
        String password = LoginForm.this.passwordTxt.getText();
        if (username != null && !username.trim().isEmpty()) {
          Config.USERNAME = username;
          if (password != null && !password.trim().isEmpty()) {
            Config.PASSWORD = password;
            String key = null;

            try {
              key = PerceptionHttpDAO.login(Config.USERNAME, Config.PASSWORD);
            } catch (PerceptionCommunicationProblemException var6) {
              JOptionPane.showMessageDialog((Component)null, var6.getMessage());
              return;
            }

            String guid = PerceptionHttpDAO.getProfileList(key);
            Config.PROFILE_GUI = guid;
            Config.LOGIN_KEY = key;
            LoginForm.this.closeCurrentFrameAndOpenNextOn();
          } else {
            JOptionPane.showMessageDialog((Component)null, "Invalid password");
          }
        } else {
          JOptionPane.showMessageDialog((Component)null, "Invalid username");
        }
      }
    });
  }

  public JPanel getMainLoginPanel() {
    return this.mainLoginPanel;
  }

  public abstract void closeCurrentFrameAndOpenNextOn();

  private void $$$setupUI$$$() {
    this.mainLoginPanel = new JPanel();
    this.mainLoginPanel.setLayout(new GridLayoutManager(3, 2, new Insets(10, 10, 10, 10), -1, -1));
    this.mainLoginPanel.setMinimumSize(new Dimension(500, 200));
    this.usernameTxt = new JTextField();
    this.mainLoginPanel.add(this.usernameTxt, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, new Dimension(250, 25), new Dimension(250, 25), (Dimension)null, 1, false));
    JLabel label1 = new JLabel();
    label1.setText("Username:");
    this.mainLoginPanel.add(label1, new GridConstraints(0, 0, 1, 1, 8, 0, 0, 0, new Dimension(-1, 25), (Dimension)null, (Dimension)null, 1, false));
    this.passwordTxt = new JPasswordField();
    this.mainLoginPanel.add(this.passwordTxt, new GridConstraints(1, 1, 1, 1, 8, 1, 4, 0, new Dimension(250, 25), new Dimension(250, 25), (Dimension)null, 1, false));
    JLabel label2 = new JLabel();
    label2.setText("Password:");
    this.mainLoginPanel.add(label2, new GridConstraints(1, 0, 1, 1, 8, 0, 0, 0, new Dimension(-1, 25), (Dimension)null, (Dimension)null, 1, false));
    JPanel panel1 = new JPanel();
    panel1.setLayout(new FlowLayout(1, 5, 5));
    this.mainLoginPanel.add(panel1, new GridConstraints(2, 0, 1, 2, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    this.loginButton = new JButton();
    this.loginButton.setText("Login");
    panel1.add(this.loginButton);
  }

  public JComponent $$$getRootComponent$$$() {
    return this.mainLoginPanel;
  }
}
