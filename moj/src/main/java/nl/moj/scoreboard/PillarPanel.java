package nl.moj.scoreboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class PillarPanel extends JPanel
{

   /**
    * <code>serialVersionUID</code> indicates/is used for.
    */
   private static final long serialVersionUID = 1537033564550594104L;
   public static final Color BACKGROUND = Color.white;
   public static final Color GROUND = new Color(255, 160, 64);
   public static final Color SKY = new Color(255, 255, 255);

   public static final Color PILLARBASE = new Color(64, 32, 0);
   public static final Color PILLARBASEHILIGHT = new Color(128, 64, 0);
   public static final Color PILLAROUT = new Color(64, 32, 0);

   public static final Font SMALLFONT = new Font("Verdana", Font.PLAIN, 10);
   public static final Font SMALLFONTBOLD = new Font("Verdana", Font.BOLD, 10);

   public static final Color[] PILLARCENTER = new Color[] { new Color(192, 128, 128), new Color(192, 192, 128), new Color(128, 192, 128), new Color(128, 160, 192), new Color(128, 128, 192),
         new Color(192, 128, 192) };

   public static final Color[] PILLARHILIGHT = new Color[] { new Color(224, 160, 160), new Color(224, 224, 160), new Color(160, 224, 160), new Color(160, 192, 224), new Color(160, 160, 224),
         new Color(224, 160, 224) };

   private String name;
   private double max;
   private double maxPerRound;
   private List<Double> segments = new ArrayList<>();
   private Image duke;

   public PillarPanel(String name, double maxScorePerRound)
   {
      this.name = name;
      this.maxPerRound = maxScorePerRound;
   }

   public String getName()
   {
      return name;
   }

   public void setMaxScore(double max)
   {
      if (max < 1)
         max = 1;
      this.max = max;
   }

   public void addSegment(int position, double size)
   {
      if (position < segments.size())
      {
         segments.set(position, new Double(size));
      }
      else
      {
         segments.add(position, new Double(size));
      }
      max = maxPerRound * segments.size();
   }

   public Dimension getPreferredSize()
   {
      int w = 42;
      if (8 + name.length() * 8 > w)
         w = 8 + name.length() * 8;
      return new Dimension(w, 512);
   }

   public double totalScore()
   {
      double sum = 0;
      for (int t = 0; t < segments.size(); t++)
      {
         double value = (segments.get(t)).doubleValue();
         if (value < 0)
            value = 0;
         sum += value;
      }
      return sum;
   }

   public void setDuke(Image duke)
   {
      this.duke = duke;
   }

   public void paint(Graphics g)
   {
      //
      Graphics2D g2d = (Graphics2D) g;
      int w = getWidth();
      int h = getHeight();
      //
      int horizon = h - 32;
      int pillarBase = h - 16;
      double scale = horizon / max;
      //
      g2d.setColor(SKY);
      g2d.fillRect(0, 0, w, horizon);
      g2d.setColor(GROUND);
      g2d.fillRect(0, horizon, w, h - horizon);
      //
      g2d.setColor(PILLARBASE);
      g2d.fillRect(4, pillarBase, w - 8, 16);
      g2d.setColor(PILLARBASEHILIGHT);
      g2d.fillRect(6, pillarBase, 2, 16);
      g2d.setColor(PILLARBASEHILIGHT);
      g2d.fillRect(w - 8, pillarBase, 2, 16);
      g2d.setFont(SMALLFONT);
      g2d.setColor(Color.white);
      double nw = g2d.getFontMetrics().getStringBounds(name, 0, name.length(), g2d).getWidth();
      g2d.drawString(name, w / 2 - (int) nw / 2, pillarBase + 12);
      //
      double total = 0.0;
      //
      for (int t = 0; t < segments.size(); t++)
      {
         //
         double score = (segments.get(t)).doubleValue();
         if (score > 0)
            total += score;
         double height = score + 2;
         int sh = (int) (scale * height);
         //
         pillarBase -= sh;
         //
         g2d.setColor(PILLAROUT);
         g2d.fillRect(6, pillarBase, w - 12, sh);
         //
         g2d.setColor(PILLARCENTER[t % PILLARCENTER.length]);
         g2d.fillRect(8, pillarBase, w - 16, sh);
         //
         g2d.setColor(PILLARHILIGHT[t % PILLARHILIGHT.length]);
         g2d.fillRect(10, pillarBase, 4, sh);
         g2d.fillRect(w - 14, pillarBase, 4, sh);
         //
         g2d.setColor(Color.white);
         String s = String.valueOf(score);
         if (s.indexOf(".") >= 0)
            s = s.substring(0, s.indexOf(".") + 2);
         double sw = g2d.getFontMetrics().getStringBounds(s, 0, s.length(), g2d).getWidth();
         g2d.drawString(s, w / 2 - (int) sw / 2, pillarBase + sh / 2 + 4);
         //
      }
      //
      pillarBase -= 16;
      g2d.setColor(PILLARBASE);
      g2d.fillRect(4, pillarBase, w - 8, 16);
      g2d.setColor(PILLARBASEHILIGHT);
      g2d.fillRect(6, pillarBase, 2, 16);
      g2d.setColor(PILLARBASEHILIGHT);
      g2d.fillRect(w - 8, pillarBase, 2, 16);
      //
      g2d.setFont(SMALLFONTBOLD);
      g2d.setColor(Color.white);
      String s = String.valueOf(total);
      if (s.indexOf(".") >= 0)
         s = s.substring(0, s.indexOf(".") + 2);
      double ts = g2d.getFontMetrics().getStringBounds(s, 0, s.length(), g2d).getWidth();
      g2d.drawString(s, w / 2 - (int) ts / 2, pillarBase + 12);
      //
      if (duke != null)
      {
         g2d.drawImage(duke, w / 2 - duke.getWidth(this) / 2, pillarBase - duke.getHeight(this), this);
      }
   }

}
