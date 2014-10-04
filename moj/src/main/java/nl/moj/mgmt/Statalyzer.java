package nl.moj.mgmt;

import java.io.File;

import nl.moj.model.State;
import nl.moj.round.StateImpl;

public class Statalyzer
{

   private static String fix(String s, int l)
   {
      if (s.length() >= l)
         return s.substring(0, l - 1) + " ";
      StringBuffer sb = new StringBuffer(s);
      while (sb.length() < l)
         sb.append(" ");
      return sb.toString();
   }

   public static void main(String[] args) throws Exception
   {
      State state = new StateImpl(new File(args[0]));
      //
      String[] rounds = state.getKnownRoundNames();
      String[] teams = state.getKnownTeams();
      //
      for (int r = 0; r < rounds.length; r++)
      {
         double success = 0;
         //double fail=0;
         double sum = 0;
         //
         double avgFileSize = 0;
         double minFileSize = Integer.MAX_VALUE - 1;
         double maxFileSize = 0;
         //
         double avgFileSizeAll = 0;
         double minFileSizeAll = Integer.MAX_VALUE - 1;
         double maxFileSizeAll = 0;
         //
         double compileFailure = 0;
         double compileSuccess = 0;
         //
         double compileFailureAll = 0;
         double compileSuccessAll = 0;
         //
         double testFailure = 0;
         double testSuccess = 0;
         //
         double testFailureAll = 0;
         double testSuccessAll = 0;
         //
         double keystroke = 0;
         double keystrokeMax = 0;
         double keystrokeMin = Integer.MAX_VALUE - 1;
         //
         for (int t = 0; t < teams.length; t++)
         {
            //
            int time = state.getScore(rounds[r], teams[t]);
            if (time >= 0)
            {
               success++;
            }
            else
            {
               //fail++;
            }
            sum++;
            //
            if (time > 0)
            {
               double fz = state.getFinalFileSize(rounds[r], teams[t]);
               if (fz < minFileSize)
                  minFileSize = fz;
               if (fz > maxFileSize)
                  maxFileSize = fz;
               avgFileSize = avgFileSize + fz;
               //
               compileFailure += state.getCompileSuccess(rounds[r], teams[t]);
               compileSuccess += state.getCompileFailures(rounds[r], teams[t]);
               compileSuccess += (state.getTestSuccess(rounds[r], teams[t]) / 7);
               compileSuccess += (state.getTestFailures(rounds[r], teams[t]) / 7);
               //
               testSuccess += state.getTestSuccess(rounds[r], teams[t]);
               testFailure += state.getTestFailures(rounds[r], teams[t]);
               //
            }
            //
            double fz = state.getFinalFileSize(rounds[r], teams[t]);
            if ((fz < minFileSizeAll) && (fz > 0))
               minFileSizeAll = fz;
            if (fz > maxFileSizeAll)
               maxFileSizeAll = fz;
            avgFileSizeAll = avgFileSizeAll + fz;

            //
            compileFailureAll += state.getCompileSuccess(rounds[r], teams[t]);
            compileSuccessAll += state.getCompileFailures(rounds[r], teams[t]);
            compileSuccessAll += (state.getTestSuccess(rounds[r], teams[t]) / 7);
            compileSuccessAll += (state.getTestFailures(rounds[r], teams[t]) / 7);
            //
            testSuccessAll += state.getTestSuccess(rounds[r], teams[t]);
            testFailureAll += state.getTestFailures(rounds[r], teams[t]);
            //
            int str = state.getNrOfKeystrokes(rounds[r], teams[t]);
            keystroke += str;
            if ((str < keystrokeMin) && (str > 0))
               keystrokeMin = str;
            if (str > keystrokeMax)
               keystrokeMax = str;
            //
         }
         //
         System.out.println(fix(rounds[r], 16) + fix("success", 12) + " : " + fix(Math.round(success) + "/" + Math.round(sum), 8) + " successful solutions (" + Math.round(100.0 * success / sum)
               + " %)");
         System.out.println(fix("", 16) + fix("size success", 12) + " : AVG " + Math.round(avgFileSize / success) + ", MIN " + Math.round(minFileSize) + ", MAX " + Math.round(maxFileSize));
         System.out.println(fix("", 16) + fix("size all", 12) + " : AVG " + Math.round(avgFileSizeAll / teams.length) + ", MIN " + Math.round(minFileSizeAll) + ", MAX " + Math.round(maxFileSizeAll));
         System.out.println(fix("", 16) + fix("compile success", 12) + " : AVG " + Math.round(compileSuccess / success) + "/" + Math.round((compileFailure + compileSuccess) / success)
               + " successful compiles (" + Math.round(100.0 * compileSuccess / (compileSuccess + compileFailure)) + " %)");
         System.out.println(fix("", 16) + fix("compile all", 12) + " : AVG " + Math.round(compileSuccessAll / teams.length) + "/" + Math.round((compileFailureAll + compileSuccessAll) / teams.length)
               + " successful compiles (" + Math.round(100.0 * compileSuccessAll / (compileSuccessAll + compileFailureAll)) + " %)");
         System.out.println(fix("", 16) + fix("test success", 12) + " : AVG " + Math.round(testSuccess / success) + "/" + Math.round((testFailure + testSuccess) / success) + " successful tests ("
               + Math.round(100.0 * testSuccess / (testFailure + testSuccess)) + " %)");
         System.out.println(fix("", 16) + fix("test all", 12) + " : AVG " + Math.round(testSuccessAll / teams.length) + "/" + Math.round((testFailureAll + testSuccessAll) / teams.length)
               + " successful tests (" + Math.round(100.0 * testSuccessAll / (testFailureAll + testSuccessAll)) + " %)");
         System.out.println(fix("", 16) + fix("keystrokes all", 12) + " : AVG " + Math.round(keystroke / teams.length) + ", MIN " + Math.round(keystrokeMin) + ", MAX " + Math.round(keystrokeMax));
         System.out.println();
         //
      }
   }

}
