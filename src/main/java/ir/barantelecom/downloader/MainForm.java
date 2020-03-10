package ir.barantelecom.downloader;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.PlainDocument;
import org.apache.commons.lang3.RandomStringUtils;

public class MainForm {
  private JComboBox<ChannelDTO> channelCmbx;
  private JTextField dateTimeTxt;
  private JTextField downloadFolderTxt;
  private JTextField durationTxt;
  private JPanel mainFormPanel;
  private JButton nowBtn;
  private JButton startDownloadBtn;
  private JLabel timeVerificationLbl;
  private JRadioButton liveRadioButton;
  private JRadioButton catchupRadioButton;
  private JProgressBar progressBar;
  private JButton openFolderButton;
  private JTextField targetFileNameTxt;
  private JButton somethingButton;
  private JButton channelOneBtn;
  private JButton channelTwoBtn;
  private JButton channelThreeBtn;
  private JButton channelNasimBtn;
  private JButton channelAiosportBtn;
  private JFileChooser fc;
  private static Map<Integer, ChannelDTO> ID_TO_CHANNEL_MAP;

  public MainForm() {
    this.$$$setupUI$$$();
    this.progressBar.setVisible(false);
    ButtonGroup streamTypeGroup = new ButtonGroup();
    this.catchupRadioButton.setSelected(true);
    streamTypeGroup.add(this.liveRadioButton);
    streamTypeGroup.add(this.catchupRadioButton);
    ((PlainDocument)this.durationTxt.getDocument()).setDocumentFilter(new MyNumberFilter());
    ((PlainDocument)this.dateTimeTxt.getDocument()).setDocumentFilter(new MyTimeFilter());
    ArrayList channelList = new ArrayList();

    try {
      List<ChannelDTO> tmpList = PerceptionHttpDAO.getChannelList(Config.LOGIN_KEY, Config.PROFILE_GUI);
      channelList.addAll(tmpList);
    } catch (PerceptionCommunicationProblemException var5) {
      JOptionPane.showMessageDialog((Component)null, var5.getMessage());
    }

    if (!channelList.isEmpty()) {
      ID_TO_CHANNEL_MAP = new HashMap();
      Iterator var6 = channelList.iterator();

      while(var6.hasNext()) {
        ChannelDTO channelDTO = (ChannelDTO)var6.next();
        this.channelCmbx.addItem(channelDTO);
        ID_TO_CHANNEL_MAP.put(channelDTO.id, channelDTO);
      }
    }

    if (Config.DEST_DIR_ADD != null && !Config.DEST_DIR_ADD.isEmpty()) {
      this.downloadFolderTxt.setText(Config.DEST_DIR_ADD);
    }

    this.downloadFolderTxt.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (MainForm.this.downloadFolderTxt.getText() != null && !MainForm.this.downloadFolderTxt.getText().isEmpty()) {
          MainForm.this.fc = new JFileChooser(MainForm.this.downloadFolderTxt.getText().trim());
        } else if (Config.DEST_DIR_ADD != null && !Config.DEST_DIR_ADD.isEmpty()) {
          MainForm.this.fc = new JFileChooser(Config.DEST_DIR_ADD);
        } else {
          MainForm.this.fc = new JFileChooser();
        }

        MainForm.this.fc.setFileSelectionMode(1);
        int returnValue = MainForm.this.fc.showOpenDialog((Component)null);
        if (returnValue == 0) {
          File selectedFile = MainForm.this.fc.getSelectedFile();
          MainForm.this.downloadFolderTxt.setText(selectedFile.getAbsolutePath());
          Config.DEST_DIR_ADD = selectedFile.getAbsolutePath();
        }

      }
    });
    this.downloadFolderTxt.addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        super.keyTyped(e);
        String dirLoc = MainForm.this.downloadFolderTxt.getText().trim();
        if ((new File(dirLoc)).exists()) {
          Config.DEST_DIR_ADD = dirLoc;
        }

      }
    });
    this.dateTimeTxt.setText((new SimpleDateFormat(Config.DATE_TIME_FORMAT)).format(new Date()));
    this.nowBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainForm.this.dateTimeTxt.setText((new SimpleDateFormat(Config.DATE_TIME_FORMAT)).format(new Date()));
      }
    });
    this.durationTxt.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        String text = MainForm.this.durationTxt.getText();
        if (text != null && !text.isEmpty()) {
          long mins = Long.parseLong(text);
          MainForm.this.timeVerificationLbl.setText(" " + mins / 60L + "h " + mins % 60L + "min");
        }
      }
    });
    this.liveRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainForm.this.dateTimeTxt.setEnabled(false);
        MainForm.this.nowBtn.setEnabled(false);
      }
    });
    this.catchupRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainForm.this.dateTimeTxt.setEnabled(true);
        MainForm.this.nowBtn.setEnabled(true);
      }
    });
    this.startDownloadBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (MainForm.this.targetFileNameTxt.getText() == null || MainForm.this.targetFileNameTxt.getText().trim().isEmpty()) {
          MainForm.this.somethingButton.doClick();
        }

        File destFolder = new File(MainForm.this.downloadFolderTxt.getText());
        String targetFileName = MainForm.this.targetFileNameTxt.getText();
        if ((new File(destFolder, targetFileName + ".ts")).exists()) {
          int reply = JOptionPane.showConfirmDialog((Component)null, String.format("File '%s' already exists. Do you like to replace it?", targetFileName), "Confirmation", 0);
          if (reply == 1) {
            return;
          }
        }

        MainForm.this.startDownloadBtn.setEnabled(false);
        MainForm.this.progressBar.setValue(0);
        MainForm.this.progressBar.setVisible(true);
        long delay = 0L;
        if (MainForm.this.catchupRadioButton.isSelected()) {
          try {
            if (Config.TIME_DIFF_IN_MILLIS == null) {
              Config.TIME_DIFF_IN_MILLIS = PerceptionHttpDAO.getTimeDiffInMillis();
            }

            delay = (new Date()).getTime() - (new SimpleDateFormat(Config.DATE_TIME_FORMAT)).parse(MainForm.this.dateTimeTxt.getText().trim()).getTime();
          } catch (ParseException var7) {
            var7.printStackTrace();
          }
        }

//        HLSUtils.downloadStreamInNewThread(Config.LOGIN_KEY, Config.PROFILE_GUI, ((ChannelDTO)MainForm.this.channelCmbx.getSelectedItem()).id, delay, Integer.parseInt(MainForm.this.durationTxt.getText()), destFolder, targetFileName, MainForm.this.progressBar, MainForm.this.startDownloadBtn);
        HLSUtilsParallel.downloadStreamInNewThread(Config.LOGIN_KEY, Config.PROFILE_GUI, ((ChannelDTO)MainForm.this.channelCmbx.getSelectedItem()).id, delay, Integer.parseInt(MainForm.this.durationTxt.getText()), destFolder, targetFileName, MainForm.this.progressBar, MainForm.this.startDownloadBtn);
      }
    });
    this.channelOneBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainForm.this.channelCmbx.setSelectedItem(MainForm.ID_TO_CHANNEL_MAP.get(8));
      }
    });
    this.channelTwoBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainForm.this.channelCmbx.setSelectedItem(MainForm.ID_TO_CHANNEL_MAP.get(9));
      }
    });
    this.channelThreeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainForm.this.channelCmbx.setSelectedItem(MainForm.ID_TO_CHANNEL_MAP.get(10));
      }
    });
    this.channelNasimBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainForm.this.channelCmbx.setSelectedItem(MainForm.ID_TO_CHANNEL_MAP.get(17));
      }
    });
    this.channelAiosportBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainForm.this.channelCmbx.setSelectedItem(MainForm.ID_TO_CHANNEL_MAP.get(52));
      }
    });
    this.openFolderButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Desktop.getDesktop().open(new File(Config.DEST_DIR_ADD));
        } catch (IOException var3) {
          var3.printStackTrace();
        }

      }
    });
    this.somethingButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainForm.this.setSomethingAsATargetFileName();
      }
    });
  }

  private void setSomethingAsATargetFileName() {
    this.targetFileNameTxt.setText(this.getNameSuggestion());
  }

  private String getNameSuggestion() {
    try {
      return (((ChannelDTO)this.channelCmbx.getSelectedItem()).name.trim() + "-" + this.durationTxt.getText() + "M-" + (this.liveRadioButton.isSelected() ? "LV" : "CU") + "-" + (new SimpleDateFormat("EEE-HH-mm")).format((new SimpleDateFormat(Config.DATE_TIME_FORMAT)).parse(this.dateTimeTxt.getText().trim())) + "-" + RandomStringUtils.random(6, true, true)).toUpperCase();
    } catch (ParseException var2) {
      var2.printStackTrace();
      return null;
    }
  }

  public JPanel getMainFormPanel() {
    return this.mainFormPanel;
  }

  private void $$$setupUI$$$() {
    this.mainFormPanel = new JPanel();
    this.mainFormPanel.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
    this.mainFormPanel.setMaximumSize(new Dimension(-1, -1));
    this.mainFormPanel.setMinimumSize(new Dimension(560, 280));
    this.mainFormPanel.setPreferredSize(new Dimension(560, 280));
    JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
    this.mainFormPanel.add(panel1, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    JLabel label1 = new JLabel();
    label1.setText("Channel:");
    panel1.add(label1, new GridConstraints(0, 0, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    JLabel label2 = new JLabel();
    label2.setText("From date:");
    panel1.add(label2, new GridConstraints(2, 0, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    JLabel label3 = new JLabel();
    label3.setText("Duration (min):");
    panel1.add(label3, new GridConstraints(3, 0, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    JPanel panel2 = new JPanel();
    panel2.setLayout(new FlowLayout(0, 0, 0));
    panel1.add(panel2, new GridConstraints(2, 1, 1, 1, 0, 1, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    this.dateTimeTxt = new JTextField();
    this.dateTimeTxt.setMaximumSize(new Dimension(200, 28));
    this.dateTimeTxt.setMinimumSize(new Dimension(200, 28));
    this.dateTimeTxt.setPreferredSize(new Dimension(200, 28));
    panel2.add(this.dateTimeTxt);
    this.nowBtn = new JButton();
    Font nowBtnFont = this.$$$getFont$$$((String)null, 0, 10, this.nowBtn.getFont());
    if (nowBtnFont != null) {
      this.nowBtn.setFont(nowBtnFont);
    }

    this.nowBtn.setMaximumSize(new Dimension(25, 25));
    this.nowBtn.setMinimumSize(new Dimension(25, 25));
    this.nowBtn.setPreferredSize(new Dimension(50, 25));
    this.nowBtn.setText("now");
    panel2.add(this.nowBtn);
    JPanel panel3 = new JPanel();
    panel3.setLayout(new FlowLayout(0, 0, 0));
    panel1.add(panel3, new GridConstraints(3, 1, 1, 1, 0, 1, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    this.durationTxt = new JTextField();
    this.durationTxt.setInheritsPopupMenu(false);
    this.durationTxt.setMargin(new Insets(2, 5, 2, 5));
    this.durationTxt.setMaximumSize(new Dimension(75, 28));
    this.durationTxt.setMinimumSize(new Dimension(75, 28));
    this.durationTxt.setOpaque(true);
    this.durationTxt.setPreferredSize(new Dimension(75, 28));
    this.durationTxt.setText("1");
    panel3.add(this.durationTxt);
    this.timeVerificationLbl = new JLabel();
    this.timeVerificationLbl.setText(" 0h 1min");
    panel3.add(this.timeVerificationLbl);
    JLabel label4 = new JLabel();
    label4.setText("Download folder:");
    panel1.add(label4, new GridConstraints(4, 0, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    JPanel panel4 = new JPanel();
    panel4.setLayout(new BorderLayout(0, 0));
    panel1.add(panel4, new GridConstraints(4, 1, 1, 1, 0, 1, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    this.downloadFolderTxt = new JTextField();
    this.downloadFolderTxt.setEditable(false);
    this.downloadFolderTxt.setFocusCycleRoot(true);
    this.downloadFolderTxt.setMaximumSize(new Dimension(2147483647, 28));
    this.downloadFolderTxt.setMinimumSize(new Dimension(64, 28));
    this.downloadFolderTxt.setPreferredSize(new Dimension(64, 28));
    this.downloadFolderTxt.setRequestFocusEnabled(true);
    this.downloadFolderTxt.setText("");
    panel4.add(this.downloadFolderTxt, "Center");
    this.openFolderButton = new JButton();
    Font openFolderButtonFont = this.$$$getFont$$$((String)null, 0, 10, this.openFolderButton.getFont());
    if (openFolderButtonFont != null) {
      this.openFolderButton.setFont(openFolderButtonFont);
    }

    this.openFolderButton.setText("Open folder");
    panel4.add(this.openFolderButton, "East");
    JPanel panel5 = new JPanel();
    panel5.setLayout(new FlowLayout(0, 0, 0));
    panel1.add(panel5, new GridConstraints(0, 1, 1, 1, 0, 1, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    this.channelCmbx = new JComboBox();
    Font channelCmbxFont = this.$$$getFont$$$("IRANSans Light", 0, -1, this.channelCmbx.getFont());
    if (channelCmbxFont != null) {
      this.channelCmbx.setFont(channelCmbxFont);
    }

    this.channelCmbx.setMaximumSize(new Dimension(200, 28));
    this.channelCmbx.setMinimumSize(new Dimension(200, 28));
    this.channelCmbx.setPreferredSize(new Dimension(200, 28));
    panel5.add(this.channelCmbx);
    this.channelOneBtn = new JButton();
    Font channelOneBtnFont = this.$$$getFont$$$((String)null, 0, 10, this.channelOneBtn.getFont());
    if (channelOneBtnFont != null) {
      this.channelOneBtn.setFont(channelOneBtnFont);
    }

    this.channelOneBtn.setMaximumSize(new Dimension(30, 25));
    this.channelOneBtn.setMinimumSize(new Dimension(30, 25));
    this.channelOneBtn.setPreferredSize(new Dimension(30, 25));
    this.channelOneBtn.setText("1");
    panel5.add(this.channelOneBtn);
    this.channelTwoBtn = new JButton();
    Font channelTwoBtnFont = this.$$$getFont$$$((String)null, 0, 10, this.channelTwoBtn.getFont());
    if (channelTwoBtnFont != null) {
      this.channelTwoBtn.setFont(channelTwoBtnFont);
    }

    this.channelTwoBtn.setMaximumSize(new Dimension(30, 25));
    this.channelTwoBtn.setMinimumSize(new Dimension(30, 25));
    this.channelTwoBtn.setPreferredSize(new Dimension(30, 25));
    this.channelTwoBtn.setText("2");
    panel5.add(this.channelTwoBtn);
    this.channelThreeBtn = new JButton();
    Font channelThreeBtnFont = this.$$$getFont$$$((String)null, 0, 10, this.channelThreeBtn.getFont());
    if (channelThreeBtnFont != null) {
      this.channelThreeBtn.setFont(channelThreeBtnFont);
    }

    this.channelThreeBtn.setMaximumSize(new Dimension(30, 25));
    this.channelThreeBtn.setMinimumSize(new Dimension(30, 25));
    this.channelThreeBtn.setPreferredSize(new Dimension(30, 25));
    this.channelThreeBtn.setText("3");
    panel5.add(this.channelThreeBtn);
    this.channelNasimBtn = new JButton();
    Font channelNasimBtnFont = this.$$$getFont$$$((String)null, 0, 10, this.channelNasimBtn.getFont());
    if (channelNasimBtnFont != null) {
      this.channelNasimBtn.setFont(channelNasimBtnFont);
    }

    this.channelNasimBtn.setMaximumSize(new Dimension(25, 25));
    this.channelNasimBtn.setMinimumSize(new Dimension(25, 25));
    this.channelNasimBtn.setPreferredSize(new Dimension(50, 25));
    this.channelNasimBtn.setText("Nasim");
    panel5.add(this.channelNasimBtn);
    this.channelAiosportBtn = new JButton();
    Font channelAiosportBtnFont = this.$$$getFont$$$((String)null, 0, 10, this.channelAiosportBtn.getFont());
    if (channelAiosportBtnFont != null) {
      this.channelAiosportBtn.setFont(channelAiosportBtnFont);
    }

    this.channelAiosportBtn.setMaximumSize(new Dimension(25, 25));
    this.channelAiosportBtn.setMinimumSize(new Dimension(25, 25));
    this.channelAiosportBtn.setPreferredSize(new Dimension(60, 25));
    this.channelAiosportBtn.setText("Aiosport");
    panel5.add(this.channelAiosportBtn);
    JPanel panel6 = new JPanel();
    panel6.setLayout(new FlowLayout(0, 0, 0));
    panel1.add(panel6, new GridConstraints(1, 1, 1, 1, 0, 1, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    this.catchupRadioButton = new JRadioButton();
    this.catchupRadioButton.setMargin(new Insets(2, 2, 2, 2));
    this.catchupRadioButton.setText("Catchup");
    panel6.add(this.catchupRadioButton);
    this.liveRadioButton = new JRadioButton();
    this.liveRadioButton.setMargin(new Insets(2, 10, 2, 2));
    this.liveRadioButton.setText("Live");
    panel6.add(this.liveRadioButton);
    JLabel label5 = new JLabel();
    label5.setText("Stream type:");
    panel1.add(label5, new GridConstraints(1, 0, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    JLabel label6 = new JLabel();
    label6.setText("Target file name:");
    panel1.add(label6, new GridConstraints(5, 0, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    JPanel panel7 = new JPanel();
    panel7.setLayout(new BorderLayout(0, 0));
    panel1.add(panel7, new GridConstraints(5, 1, 1, 1, 0, 1, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    this.targetFileNameTxt = new JTextField();
    this.targetFileNameTxt.setMaximumSize(new Dimension(2147483647, 28));
    this.targetFileNameTxt.setMinimumSize(new Dimension(200, 28));
    this.targetFileNameTxt.setPreferredSize(new Dimension(200, 28));
    panel7.add(this.targetFileNameTxt, "Center");
    this.somethingButton = new JButton();
    Font somethingButtonFont = this.$$$getFont$$$((String)null, 0, 10, this.somethingButton.getFont());
    if (somethingButtonFont != null) {
      this.somethingButton.setFont(somethingButtonFont);
    }

    this.somethingButton.setMaximumSize(new Dimension(109, 25));
    this.somethingButton.setMinimumSize(new Dimension(109, 25));
    this.somethingButton.setPreferredSize(new Dimension(80, 25));
    this.somethingButton.setText("Something");
    panel7.add(this.somethingButton, "East");
    JPanel panel8 = new JPanel();
    panel8.setLayout(new FlowLayout(1, 0, 0));
    this.mainFormPanel.add(panel8, new GridConstraints(1, 0, 1, 1, 0, 1, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    this.startDownloadBtn = new JButton();
    this.startDownloadBtn.setText("Start new download");
    panel8.add(this.startDownloadBtn);
    JPanel panel9 = new JPanel();
    panel9.setLayout(new BorderLayout(0, 0));
    this.mainFormPanel.add(panel9, new GridConstraints(2, 0, 1, 1, 0, 1, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    this.progressBar = new JProgressBar();
    this.progressBar.setFocusable(false);
    this.progressBar.setMaximumSize(new Dimension(32767, 15));
    this.progressBar.setMinimumSize(new Dimension(10, 15));
    this.progressBar.setPreferredSize(new Dimension(146, 15));
    this.progressBar.setStringPainted(true);
    this.progressBar.setValue(0);
    panel9.add(this.progressBar, "Center");
  }

  private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
    if (currentFont == null) {
      return null;
    } else {
      String resultName;
      if (fontName == null) {
        resultName = currentFont.getName();
      } else {
        Font testFont = new Font(fontName, 0, 10);
        if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
          resultName = fontName;
        } else {
          resultName = currentFont.getName();
        }
      }

      return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }
  }

  public JComponent $$$getRootComponent$$$() {
    return this.mainFormPanel;
  }
}
