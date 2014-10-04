package nl.moj.client;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class StacktraceHighlightingCellRenderer extends DefaultListCellRenderer {

    public interface FileNameSource {
        public boolean fileNameExists(String name);
    }
    
    private static final long serialVersionUID = 1261623647064680003L;

    public static int getErrorLineNr(String s) {
        try {
            if (s.indexOf(".java")>=0) {
                int idx1=s.indexOf(':');
                int idx2=s.lastIndexOf(')');
                if ((idx1>=0)&&(idx2>=0)&&(idx1!=idx2)) {
                    String line=s.substring(idx1+1,idx2);
                    return Integer.parseInt(line);
                }                
            }
            return -1;
        } catch (Exception ex) {
            return -1;
        }
    }
    
    public static String getErrorFile(String s,FileNameSource fs) {
        int idx1=s.indexOf(".java");
        int idx2=s.lastIndexOf('(');
        if ((idx1>=0)&&(idx2>=0)&&(idx1!=idx2)) {
            String file=s.substring(idx2+1,idx1+5);
            if (fs.fileNameExists(file)) return file;
        }
        return null;
    }
    
    public static boolean isErrorLine(String s,FileNameSource fs) {
        return getErrorLineNr(s)>=0&&getErrorFile(s, fs)!=null;
    }
    
    private Color error;
    private FileNameSource fs;
    
    public StacktraceHighlightingCellRenderer(FileNameSource fs) {
        this(new Color(255,220,220),fs);
    }
    public StacktraceHighlightingCellRenderer(Color c,FileNameSource fs) {
        error=c;
        this.fs=fs;
    }
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String s=String.valueOf(value);        
        JLabel lbl=(JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        lbl.setBackground(isErrorLine(s,fs)?error:Color.WHITE);
        return lbl;
    }
}