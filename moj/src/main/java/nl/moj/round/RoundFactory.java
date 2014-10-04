package nl.moj.round;

import java.io.File;

import nl.ctrlaltdev.util.Encoder;
import nl.moj.model.Assignment;
import nl.moj.model.GameRules;
import nl.moj.model.Round;
import nl.moj.model.Team;

public class RoundFactory {
	
	public Assignment[] getAvailableAssignments() {
		return null;
	}
	
	public GameRules[] getAvailableGameRules() {
		return null;
	}
	
	public Round createRound(Assignment a,GameRules r,String[] teamNames) {
		Round rnd=new RoundImpl(a,r);
		for(int t=0;t<teamNames.length;t++) {
			Team team=new TeamImpl(fix(teamNames[t]),teamNames[t],Encoder.hash(teamNames[t]),a,r,new DoNothingState(),null);
			rnd.addTeam(team);
		}
		return rnd;
	}

	public Round createRound(Assignment a,GameRules r,String[] teamNames,String[] passwords) {
		Round rnd=new RoundImpl(a,r);
		for(int t=0;t<teamNames.length;t++) {
			Team team=new TeamImpl(fix(teamNames[t]),teamNames[t],Encoder.hash(passwords[t]),a,r,new DoNothingState(),null);
			rnd.addTeam(team);
		}
		return rnd;
	}
	
	public Round createRound(Assignment a,GameRules r,String[] teamNames,String[] displayNames,String[] passwords) {
		Round rnd=new RoundImpl(a,r);
		for(int t=0;t<teamNames.length;t++) {
			Team team=new TeamImpl(fix(teamNames[t]),displayNames[t],Encoder.hash(passwords[t]),a,r,new DoNothingState(),null);
			rnd.addTeam(team);
		}
		return rnd;
	}
	
	/** remove any dangerous chars out of the team name */
	private String fix(String s) {
		return s.replace(' ','_').replace('.','_').replace(File.separatorChar,'_');
	}
	
}
