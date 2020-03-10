package ir.barantelecom.downloader;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;

public class MyTimeFilter extends DocumentFilter {
  public MyTimeFilter() {
  }

  public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
    Document doc = fb.getDocument();
    StringBuilder sb = new StringBuilder();
    sb.append(doc.getText(0, doc.getLength()));
    sb.insert(offset, string);
    if (this.test(sb.toString())) {
      super.insertString(fb, offset, string, attr);
    }

  }

  private boolean test(String text) {
    return text == null || text.matches("([0-9]| |:|-)*");
  }

  public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    Document doc = fb.getDocument();
    StringBuilder sb = new StringBuilder();
    sb.append(doc.getText(0, doc.getLength()));
    sb.replace(offset, offset + length, text);
    if (this.test(sb.toString())) {
      super.replace(fb, offset, length, text, attrs);
    }

  }

  public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
    Document doc = fb.getDocument();
    StringBuilder sb = new StringBuilder();
    sb.append(doc.getText(0, doc.getLength()));
    sb.delete(offset, offset + length);
    if (this.test(sb.toString())) {
      super.remove(fb, offset, length);
    }

  }
}
