package nl.moj.client.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class UpdateClientStatisticsMessageImpl extends AbstractMessage implements Message.UpdateClientStatistics {

	private int myFinal,myMaxSec,mySecRemain,myState,myTeamsOnline,myTeamCount;
	private int[] myResults;
	
	public UpdateClientStatisticsMessageImpl(int finalScore,int maxSec,int secRemain,int state,int teamsOnline,int teamCount,int[] results) {
		super(Message.MSG_UPDATE_CLIENT);
		myFinal=finalScore;
		myMaxSec=maxSec;
		mySecRemain=secRemain;
		myState=state;
		myTeamsOnline=teamsOnline;
		myTeamCount=teamCount;
		myResults=results;
	}
	
	public UpdateClientStatisticsMessageImpl(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_UPDATE_CLIENT) throw new IOException("Incorrect Type"); 
		myFinal=in.readInt();
		myMaxSec=in.readInt();
		mySecRemain=in.readInt();
		myState=in.readInt();
		myTeamsOnline=in.readInt();
		myTeamCount=in.readInt();
		int cnt=in.readInt();
		myResults=new int[cnt];
		if (cnt==0) myResults=null;
		else for (int t=0;t<cnt;t++) {
			myResults[t]=in.readInt();
		} 
	}
	
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeInt(myFinal);
		out.writeInt(myMaxSec);
		out.writeInt(mySecRemain);
		out.writeInt(myState);
		out.writeInt(myTeamsOnline);
		out.writeInt(myTeamCount);
		if (myResults==null) {
			out.writeInt(0);
		} else {
			out.writeInt(myResults.length);
			for (int t=0;t<myResults.length;t++) {
				out.writeInt(myResults[t]);
			}
		}
	}
	
	public int getFinalScore() { return myFinal; }
	public int getMaxSeconds() { return myMaxSec; }
	public int getSecondsRemaining() { return mySecRemain; }
	public int getState() { return myState; }
	public int getTeamsOnline() { return myTeamsOnline; }
	public int getTeamCount() { return myTeamCount; }
	public int[] getTestResults() { return myResults; }
	
}
