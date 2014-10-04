package nl.moj.scoreboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import nl.ctrlaltdev.ui.Build;
import nl.ctrlaltdev.ui.PowerBarPanel;
import nl.ctrlaltdev.util.Tool;
import nl.moj.banner.BannerPanel;
import nl.moj.round.StateImpl;

/**
 * A ScoreBoard capable of showing multiple result files at the same time. With autoscroll option.
 */
public class ScoreBoardToo extends JPanel
{

   /**
    * <code>serialVersionUID</code> indicates/is used for.
    */
   private static final long serialVersionUID = -3083197452484275552L;
   private static final Font bigFont = new Font("Verdana", Font.PLAIN, 42);
   private static final Color backgroundBlue = new Color(64, 64, 128);
   private static final Color darkBlue = new Color(32, 32, 64);
   private static final Color foregroundWhite = new Color(255, 255, 255);

   private BannerPanel[] bmp;

   private JScrollBar scrollBar;
   private int scrollDirection;
   private int pause;

   public ScoreBoardToo(String[] columns) throws IOException
   {
      super(new GridLayout(0, columns.length));
      buildGui(columns);
      pause = 20;
   }

   protected JComponent buildBigLabel(String txt)
   {
      JLabel lbl = new JLabel(txt);
      lbl.setFont(bigFont);
      lbl.setForeground(foregroundWhite);
      return new Build.CFP(lbl, backgroundBlue);
   }

   protected PowerBarPanel.Entry makeEntry(StateImpl state, String team, String[] rounds)
   {
      int sum = 0;
      int[] rnd = new int[rounds.length];
      String[] lbl = new String[rounds.length];
      for (int t = 0; t < rounds.length; t++)
      {
         rnd[t] = state.getScore(rounds[t], team);
         if (rnd[t] >= 0)
         {
            sum += rnd[t];
         }
         else
            rnd[t] = 0;
         lbl[t] = "" + rnd[t];
      }
      return new PowerBarPanel.SimpleEntry(team, sum + " pts", rnd, rounds, lbl);
   }

   protected JComponent buildScorePanel(StateImpl state)
   {
      //
      String[] teams = state.getKnownTeams();
      String[] rounds = state.getKnownRoundNames();
      //
      PowerBarPanel pbp = new PowerBarPanel();
      pbp.setBackground(darkBlue);
      //
      PowerBarPanel.Entry[] e = new PowerBarPanel.Entry[teams.length];
      for (int t = 0; t < e.length; t++)
      {
         e[t] = makeEntry(state, teams[t], rounds);
      }
      //
      Arrays.sort(e, new Comparator<PowerBarPanel.Entry>()
         {
            public int compare(PowerBarPanel.Entry e1, PowerBarPanel.Entry e2)
            {
               int sum1 = 0;
               int sum2 = 0;
               for (int t = 0; t < e1.getLength(); t++)
                  sum1 += e1.getValue(t);
               for (int t = 0; t < e2.getLength(); t++)
                  sum2 += e2.getValue(t);
               return sum2 - sum1;
            }
         });
      //
      for (int t = 0; t < teams.length; t++)
      {
         pbp.addEntry(e[t]);
      }
      //
      return pbp;
   }

   protected void buildGui(String[] columns) throws IOException
   {
      //
      bmp = new BannerPanel[columns.length];
      //
      for (int t = 0; t < columns.length; t++)
      {
         bmp[t] = new BannerPanel(new File("./data/banners/"));
         for (int y = 0; y < t; y++)
            bmp[t].nextImage();
         bmp[t].setPreferredSize(new Dimension(256, 128));
         StateImpl state = new StateImpl(new File(columns[t]));
         JScrollPane scrollPane = new JScrollPane(buildScorePanel(state));
         scrollBar = scrollPane.getVerticalScrollBar();
         JPanel p = new Build.NCS(buildBigLabel("" + (char) ('A' + t)), scrollPane, bmp[t]);
         this.add(p);
      }
   }

   public void updateBannerPanels()
   {
      if (bmp == null)
         return;
      for (int t = 0; t < bmp.length; t++)
      {
         bmp[t].nextImage();
      }
   }

   public void updateScrollBar()
   {
      int max = scrollBar.getModel().getMaximum();
      int min = scrollBar.getModel().getMinimum();
      int pos = scrollBar.getModel().getValue();
      int ext = scrollBar.getModel().getExtent();
      //
      if (max == ext)
         return;
      if (scrollBar.getValueIsAdjusting())
         return;
      //
      if ((scrollDirection == 0) && (pause == 0))
      {
         if (pos == min)
            scrollDirection = 1;
         else
            scrollDirection = -1;
      }
      else
         pause--;
      if (scrollDirection > 0)
      {
         pos += 4;
         if (pos + ext > max)
         {
            pos = max - ext;
            scrollDirection = 0;
            pause = 20;
         }
         scrollBar.setValue(pos);
      }
      else if (scrollDirection < 0)
      {
         pos -= 4;
         if (pos < min)
         {
            pos = min;
            scrollDirection = 0;
            pause = 20;
         }
         scrollBar.setValue(pos);
      }
   }

   public static void main(String[] args) throws Throwable
   {
      //
      args = Tool.parseArgs(args);
      //
      if (args.length == 0)
      {
         System.out.println("Usage : ScoreBoard [filename] <filename> ..");
         System.exit(0);
      }
      //
      JFrame f = new JFrame("Masters Of Java - Scoreboard");
      final ScoreBoardToo sbt = new ScoreBoardToo(args);
      f.getContentPane().add(sbt, BorderLayout.CENTER);
      f.setSize(1024, 768);
      f.addWindowListener(new WindowAdapter()
         {
            public void windowClosing(WindowEvent e)
            {
               System.exit(0);
            }
         });
      //
      f.setVisible(true);
      //
      Thread t = new Thread(new Runnable()
         {
            public void run()
            {
               int cnt = 0;
               while (true)
               {
                  try
                  {
                     Thread.sleep(100);
                  }
                  catch (InterruptedException ex)
                  {
                     //
                  }
                  final boolean update = (++cnt % 100 == 0);
                  SwingUtilities.invokeLater(new Runnable()
                     {
                        public void run()
                        {
                           sbt.updateScrollBar();
                           if (update)
                              sbt.updateBannerPanels();
                        }
                     });
               }
            }
         });
      t.setDaemon(true);
      t.start();
   }

}
