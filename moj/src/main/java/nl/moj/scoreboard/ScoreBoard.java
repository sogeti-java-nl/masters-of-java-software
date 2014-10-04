package nl.moj.scoreboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;

import nl.ctrlaltdev.ui.Build;
import nl.ctrlaltdev.ui.ImagePanel;
import nl.ctrlaltdev.util.Tool;
import nl.moj.model.State;
import nl.moj.round.StateImpl;

/**
 * Simple Score Board. Parses the default or commandline specified state file and displays the results.
 */

public class ScoreBoard extends JPanel
{

   /**
    * <code>serialVersionUID</code> indicates/is used for.
    */
   private static final long serialVersionUID = -3249312481773326202L;

   private static final class ScorePanel extends JPanel
   {
      /**
       * <code>serialVersionUID</code> indicates/is used for.
       */
      private static final long serialVersionUID = 4559324873940751173L;
      private String myTeam;
      private String myRound;
      private State myState;
      private String score;
      private int cf, cs;
      private int tf, ts;

      public ScorePanel(String team, String round, State st)
      {
         myTeam = team;
         myRound = round;
         myState = st;
         //
         int sc = myState.getScore(myRound, myTeam);
         if (sc < 0)
         {
            score = "None";
            setBackground(ROUNDNOENTRY);
            setForeground(Color.gray);
         }
         else
         {
            score = String.valueOf(sc);
            if (team.equals(myState.getRoundWinner(round)))
            {
               setBackground(ROUNDWINNER);
               setForeground(Color.black);
            }
            else
            {
               setBackground(ROUNDPLAYED);
               setForeground(Color.black);
            }
         }
         //
         this.setPreferredSize(new Dimension(96, 64));
         //
         cf = myState.getCompileFailures(myRound, myTeam);
         cs = myState.getCompileSuccess(myRound, myTeam);
         tf = myState.getTestFailures(myRound, myTeam);
         ts = myState.getTestSuccess(myRound, myTeam);
         //
      }

      public void paint(Graphics g)
      {
         int w = getWidth();
         int h = getHeight();
         g.setColor(getBackground());
         g.fill3DRect(0, 0, w, h, true);
         g.setColor(getForeground());
         g.setFont(SCOREFONT);
         g.drawString(score, 4, 14);
         g.setColor(Color.gray);
         //
         // FileSize / Minute
         //
         g.drawLine(6, 20, 6, 60);
         g.drawLine(4, 60, 68, 60);
         for (int t = 0; t < 30; t++)
         {
            int sz = myState.getFileSizeForMinute(myRound, myTeam, t);
            int tsz = myState.getFinalFileSize(myRound, myTeam);
            if (tsz == 0)
               tsz = 1;
            //
            sz = 30 * sz / tsz;
            if (sz > 42)
               sz = 42;
            //
            g.setColor(GRAPHOUT);
            g.fillRect(7 + t * 2, 59 - sz, 2, sz);
            g.setColor(GRAPHIN);
            g.drawRect(7 + t * 2, 59 - sz, 2, sz);
         }
         //
         drawBar(g, cf, cs, cf + cs, 72, 42, 24);
         drawBar(g, tf, ts, tf + ts, 72, 46, 24);
         //
      }

      private void drawBar(Graphics g, double f, double s, double sum, int x, int y, double w)
      {
         double d = w / sum;
         double l = f * d;
         double r = s * d;
         g.setColor(FAILURE);
         g.fillRect(x, y, (int) l, 2);
         g.setColor(SUCCESS);
         g.fillRect((int) (x + l), y, (int) r, 2);
      }
   }

   private static final Font SCOREFONT = new Font("Courier New", Font.BOLD, 16);
   private static final Color ROUNDWINNER = new Color(128, 224, 128);
   private static final Color ROUNDPLAYED = new Color(0, 192, 0);
   private static final Color ROUNDNOENTRY = new Color(128, 32, 32);
   //	private static final Color ROUNDFAULT=new Color(255,64,64);

   private static final Color MATCHWINNER = new Color(192, 192, 255);
   private static final Color MATCHPLAYED = new Color(128, 128, 192);

   private static final Color GRAPHIN = new Color(192, 192, 192);
   private static final Color GRAPHOUT = new Color(128, 128, 128);

   private static final Color FAILURE = new Color(255, 128, 128);
   private static final Color SUCCESS = new Color(128, 255, 128);

   private State state;
   private String[] rounds;
   private String[] teams;

   public ScoreBoard(String[] args) throws IOException
   {
      super(new BorderLayout());
      //
      if (args.length == 0)
      {
         state = new StateImpl(new File("./state.csv"));
      }
      else
      {
         state = new StateImpl(new File(args[0]));
      }
      //
      rounds = state.getKnownRoundNames();
      teams = state.getKnownTeams();
      //
      //
      JComponent[] rows = new JComponent[teams.length + 1];
      //
      rows[0] = new JPanel(new GridLayout(0, rounds.length + 2));
      rows[0].add(new JPanel());
      for (int r = 0; r < rounds.length; r++)
      {
         rows[0].add(createRoundPanel(rounds[r]));
      }
      rows[0].add(createRoundPanel("** TOTAL **"));
      //
      String maxName = null;
      int max = 0;
      int[] sums = new int[teams.length];
      for (int t = 0; t < sums.length; t++)
      {
         String tm = teams[t];
         for (int r = 0; r < rounds.length; r++)
         {
            String rnd = rounds[r];
            int score = state.getScore(rnd, tm);
            if (score >= 0)
            {
               sums[t] = sums[t] + score;
            }
         }
         if (sums[t] > max)
         {
            max = sums[t];
            maxName = tm;
         }
      }
      //
      for (int t = 0; t < teams.length; t++)
      {
         rows[t + 1] = new JPanel(new GridLayout(0, rounds.length + 2));
         String tm = teams[t];
         rows[t + 1].add(createTeamPanel(teams[t]));
         for (int r = 0; r < rounds.length; r++)
         {
            String rnd = rounds[r];
            rows[t + 1].add(createScorePanel(tm, rnd, renderDetails(rnd, tm)));
         }
         rows[t + 1].add(createTotalScorePanel(String.valueOf(sums[t]), tm.equals(maxName)));
      }
      //
      Box box = new Build.BOXY(rows);
      this.add(box, BorderLayout.NORTH);
      //		
   }

   private String renderDetails(String rnd, String tm)
   {
      StringBuffer sb = new StringBuffer();
      sb.append("<HTML><TABLE>");
      sb.append("<TR><TD>Typed/Source Chars</TD><TD>" + state.getKeyStrokeFileSizeRatio(rnd, tm));
      sb.append("</TD></TR>");
      sb.append("<TR><TD>Compile Success</TD><TD>" + state.getCompileSuccess(rnd, tm));
      sb.append("</TD></TR>");
      sb.append("<TR><TD>Compile Failure</TD><TD>" + state.getCompileFailures(rnd, tm));
      sb.append("</TD></TR>");
      sb.append("<TR><TD>Test Success</TD><TD>" + state.getTestSuccess(rnd, tm));
      sb.append("</TD></TR>");
      sb.append("<TR><TD>Test Failure</TD><TD>" + state.getTestFailures(rnd, tm));
      sb.append("</TD></TR>");
      sb.append("</TABLE></HTML>");
      return sb.toString();
   }

   private JPanel createTeamPanel(String name)
   {
      JPanel p = new Build.CFP(new JLabel(name));
      p.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
      return p;
   }

   private JPanel createScorePanel(String team, String round, String tt)
   {
      JPanel p = new ScorePanel(team, round, state);
      p.setToolTipText(tt);
      return p;
   }

   private JPanel createTotalScorePanel(String name, boolean winner)
   {
      JLabel lbl = new JLabel(name);
      JPanel p = new Build.CFP(lbl);
      p.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
      p.setBackground(MATCHPLAYED);
      if (winner)
         p.setBackground(MATCHWINNER);
      return p;
   }

   private JPanel createRoundPanel(String name)
   {
      JPanel p = new Build.CFP(new JLabel(name));
      p.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
      return p;
   }

   public static Image loadImage(String fileName) throws IOException
   {
      Toolkit tk = Toolkit.getDefaultToolkit();
      //
      URL myURL = ScoreBoard.class.getResource(fileName);
      if (myURL == null)
         throw new IOException("Resource " + fileName + " not found. Does it start with / ?");
      //
      return tk.createImage(myURL);
   }

   public static void main(String[] args) throws Throwable
   {
      //
      ToolTipManager.sharedInstance().setDismissDelay(5000);
      ToolTipManager.sharedInstance().setInitialDelay(500);
      ToolTipManager.sharedInstance().setReshowDelay(1000);
      //
      Image logo = loadImage("/client/mojLogo.gif");
      //
      JFrame f = new JFrame("Score Board");
      f.setSize(800, 600);
      ScoreBoard sc = new ScoreBoard(Tool.parseArgs(args));
      f.getContentPane().add(new ImagePanel(logo, 320, 136, Color.white), BorderLayout.NORTH);
      f.getContentPane().add(new JScrollPane(sc), BorderLayout.CENTER);
      //
      MediaTracker tr = new MediaTracker(f);
      tr.addImage(logo, 1);
      tr.waitForAll();
      //
      f.setVisible(true);
      f.addWindowListener(new WindowAdapter()
         {
            public void windowClosing(WindowEvent e)
            {
               System.exit(0);
            }
         });
      //	
   }

}
