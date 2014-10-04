package nl.moj.model;

import java.io.IOException;

import nl.moj.workspace.factory.WorkspaceFactory;

/**
 *
 */
public interface Round {

	public void   addTeam(Team t);
	public void   removeTeam(Team t);
	public Team[] getAllTeams();
	public Team   getTeamByName(String name);
	
	public int getTeamCount();
	public int getTeamsOnline();
	
	public GameRules   getGameRules();
	public Assignment  getAssignment();
	
	public void start(WorkspaceFactory workspaceFactory,boolean resume) throws IOException;
	public boolean isStarted();
	public boolean isFinished();
	
	public void logReport();
	
	public void dispose();
	public void suspend();
	
	public void load(State st);
	public void loadAssignment(boolean resume) throws IOException;
	
}
