package nl.moj.client;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class ErrorHighlightingCellRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = -8580420261717510153L;

    public static int getErrorLineNr(String s) {
        try {
            if (s.indexOf(".java")>=0) {
                int idx1=s.indexOf(':');
                int idx2=s.lastIndexOf(':');
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
    
    public static String getErrorFile(String s) {
        int idx1=s.indexOf(".java");
        int idx2=s.lastIndexOf('/');
        if (idx2==-1) idx2=s.lastIndexOf('\\');
        if ((idx1>=0)&&(idx2>=0)&&(idx1!=idx2)) {
            return s.substring(idx2+1,idx1+5);
        }
        return null;
    }
    
    public static boolean isErrorLine(String s) {
        return getErrorLineNr(s)>=0;
    }
    
    private Color error;
    
    public ErrorHighlightingCellRenderer() {
        this(new Color(255,220,220));
    }
    public ErrorHighlightingCellRenderer(Color c) {
        error=c;
    }
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String s=String.valueOf(value);        
        JLabel lbl=(JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        lbl.setBackground(isErrorLine(s)?error:Color.WHITE);
        return lbl;
    }
    
}
