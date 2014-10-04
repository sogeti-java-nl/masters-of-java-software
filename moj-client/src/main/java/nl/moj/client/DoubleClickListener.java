package nl.moj.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoubleClickListener extends MouseAdapter {

    private ActionListener al;
    
    public DoubleClickListener(ActionListener al) {
        this.al=al;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount()==2) {
            al.actionPerformed(new ActionEvent(e.getSource(),ActionEvent.ACTION_PERFORMED,"DOUBLECLICK"));
        }
    }
    
}
