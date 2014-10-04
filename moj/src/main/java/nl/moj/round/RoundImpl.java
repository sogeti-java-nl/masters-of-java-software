package nl.moj.round;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.moj.model.Assignment;
import nl.moj.model.GameRules;
import nl.moj.model.Operation;
import nl.moj.model.Round;
import nl.moj.model.State;
import nl.moj.model.Team;
import nl.moj.workspace.factory.WorkspaceFactory;

/**
 *
 */

public class RoundImpl implements Round
{

   private static final Logger log = Logger.getLogger("Round");

   private List<Team> myTeams = new ArrayList<>();
   private Assignment myAssignment;
   private GameRules myRules;

   public RoundImpl(Assignment a, GameRules rules)
   {
      //
      if (a == null)
         throw new NullPointerException("Assignment is NULL.");
      if (rules == null)
         throw new NullPointerException("GameRules are NULL.");
      //
      Operation[] ops = a.getOperations();
      if (ops == null)
         throw new NullPointerException("Operations are null.");
      if (ops.length == 0)
         throw new RuntimeException("There are no operations defined.");
      //
      myRules = rules;
      myAssignment = a;
      //
   }

   public void addTeam(Team t)
   {
      if (t == null)
         throw new NullPointerException("Team is NULL.");
      if (myTeams.contains(t))
         throw new RuntimeException("Team " + t + " is already in the Round list.");
      log.log(Level.INFO, "Added Team : " + t.getName());
      myTeams.add(t);
   }

   public void removeTeam(Team t)
   {
      if (t == null)
         throw new NullPointerException("Team is NULL.");
      if (!myTeams.contains(t))
         throw new RuntimeException("Team " + t + " is not in the round list.");
      log.log(Level.INFO, "Removed Team : " + t.getName());
      myTeams.remove(t);
   }

   public Team[] getAllTeams()
   {
      return myTeams.toArray(new Team[myTeams.size()]);
   }

   public Team getTeamByName(String name)
   {
      Team[] all = getAllTeams();
      for (int t = 0; t < all.length; t++)
      {
         if (all[t].getName().equalsIgnoreCase(name))
            return all[t];
      }
      return null;
   }

   //
   //
   //

   public void start(WorkspaceFactory workspaceFactory, boolean resume) throws IOException
   {
      //
      if (isStarted())
         return;
      //
      // Setup the workspaces.
      //
      Team[] tm = getAllTeams();
      for (int t = 0; t < tm.length; t++)
      {
         ((TeamImpl) tm[t]).initWorkspace(workspaceFactory);
      }
      //
      // Load the assignments into the workspaces.
      //
      loadAssignment(resume);
      //
      // Start the clock.
      //
      for (int t = 0; t < tm.length; t++)
      {
         myRules.start(tm[t]);
      }
      //
   }

   /**
    * If all teams have entered the finished state this round is finished.
    */
   public boolean isFinished()
   {
      Team[] tm = getAllTeams();
      for (int t = 0; t < tm.length; t++)
      {
         if (myRules.getState(tm[t]) != GameRules.STATE_FINISHED)
         {
            return false;
         }
      }
      return true;
   }

   /**
    * If any of the teams have left the wait-state this round is started.
    */
   public boolean isStarted()
   {
      Team[] tm = getAllTeams();
      for (int t = 0; t < tm.length; t++)
      {
         if (myRules.getState(tm[t]) != GameRules.STATE_WAITING)
         {
            return true;
         }
      }
      return false;
   }

   //
   //
   //

   public Assignment getAssignment()
   {
      return myAssignment;
   }

   public GameRules getGameRules()
   {
      return myRules;
   }

   public void logReport()
   {
      //
      log.info("------ Round Report -------");
      //
      if (this.isFinished())
         log.info("Round has finished.");
      else
         log.info("Round has not finished (!).");
      //
      Team[] tm = getAllTeams();
      for (int t = 0; t < tm.length; t++)
      {
         if (tm[t].isFinished())
         {
            log.info("Team : " + tm[t].getName() + " scored " + tm[t].getFinalScore() + " points.");
         }
         else
         {
            log.info("Team : " + tm[t].getName() + " was still working. Theoretical score : " + tm[t].getTheoreticalScore() + " points.");
         }
      }
      //
      log.info("------ ----- ------ -------");
      //
   }

   public int getTeamsOnline()
   {
      int cnt = 0;
      Team[] tm = getAllTeams();
      for (int t = 0; t < tm.length; t++)
      {
         if (tm[t].isOnline())
         {
            cnt++;
         }
      }
      return cnt;
   }

   public int getTeamCount()
   {
      return myTeams.size();
   }

   //
   //
   //

   public void dispose()
   {
      Team[] tm = getAllTeams();
      for (int t = 0; t < tm.length; t++)
      {
         tm[t].getWorkspace().dispose();
      }
   }

   public void suspend()
   {
      Team[] tm = getAllTeams();
      for (int t = 0; t < tm.length; t++)
      {
         tm[t].getWorkspace().suspend();
      }
   }

   //
   //
   //

   public void load(State st)
   {
      Team[] tm = getAllTeams();
      for (int t = 0; t < tm.length; t++)
      {
         myRules.load(this, tm[t], st);
      }
   }

   public void loadAssignment(boolean resume) throws IOException
   {
      Team[] tm = getAllTeams();
      for (int t = 0; t < tm.length; t++)
      {
         tm[t].getWorkspace().loadAssignment(getAssignment(), resume);
      }
   }

}
