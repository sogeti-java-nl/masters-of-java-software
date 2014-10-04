package nl.moj.debug;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

import javax.swing.JFrame;

import nl.moj.client.anim.Anim;
import nl.moj.client.anim.AnimPlayer;
import nl.moj.model.Tester;

/**
 * Testable Runner for use with the MoJ Assignment Eclipse plugin.
 * 
 * @author E.Hooijmeijer
 */

public class RunAsTestable
{

   public static void main(String[] args) throws Throwable
   {
      //
      Class< ? > c = Class.forName(args[0]);
      Tester.Testable tst = (Tester.Testable) c.newInstance();
      //
      if (tst instanceof Tester.AnimatedTestable)
      {
         //
         runAnimatedTestable((Tester.AnimatedTestable) tst, tst.getTestCount());
         //
      }
      else
      {
         //
         runTestable(tst);
         //
      }
   }

   protected static void runTestable(Tester.Testable tst) throws Throwable
   {
      //
      boolean pass = true;
      //
      for (int t = 0; t < tst.getTestCount(); t++)
      {
         System.out.println(tst.getTestName(t) + ") -------------------------------------------");
         System.out.println(tst.getTestDescription(t));
         try
         {
            boolean b = tst.performTest(t);
            if (!b)
               pass = false;
            System.out.println("Result: " + (b ? "Pass" : "Fail"));
         }
         catch (Throwable x)
         {
            System.out.println(x.getClass().getName() + " : " + x.getMessage());
            pass = false;
            x.printStackTrace();
         }
      }
      System.out.println("Final Verdict -------------------------------------------");
      System.out.println(pass ? "Pass" : "Fail");
   }

   protected static void runAnimatedTestable(Tester.AnimatedTestable cc, int mx) throws Throwable
   {
      AnimPlayer[] player = new AnimPlayer[cc.getTestCount()];
      //
      JFrame f = new JFrame();
      f.getContentPane().setLayout(new GridLayout(3, 3));
      f.setSize(512, 512);
      for (int t = 0; t < player.length; t++)
      {
         player[t] = new AnimPlayer();
         f.getContentPane().add(player[t]);
      }
      f.addWindowListener(new WindowAdapter()
         {
            public void windowClosing(WindowEvent e)
            {
               System.exit(0);
            }
         });
      //
      //
      boolean good = true;
      Anim[] a = new Anim[cc.getTestCount()];
      for (int t = 0; t < mx; t++)
      {
         System.out.println(cc.getTestName(t) + ") -------------------------------------------");
         System.out.println(cc.getTestDescription(t));
         try
         {
            boolean r = cc.performTest(t, a);
            System.out.println("Result : " + (r ? "Pass" : "Fail"));
            if (!r)
               good = false;
         }
         catch (Exception ex)
         {
            System.out.println(ex.getClass().getName() + " : " + ex.getMessage());
            ex.printStackTrace();
         }
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         DataOutput out = new DataOutputStream(bout);
         a[t].write(out);
         System.out.println("Animation size : " + bout.size());
         player[t].setAnimation(a[t]);
      }
      System.out.println("Final Verdict -------------------------------------------");
      System.out.println(good ? "Pass" : "Fail");
      //
      f.setVisible(true);
      //		
   }
}
