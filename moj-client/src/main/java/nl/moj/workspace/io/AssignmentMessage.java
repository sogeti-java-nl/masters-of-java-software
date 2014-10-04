package nl.moj.workspace.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import nl.moj.assignment.JarFileAssignment;

public class AssignmentMessage extends AbstractWorkspaceMessage implements Message.Assignment {

	private String teamName;
	private boolean resumeMode;
	private byte[]  jarData;
	
	public AssignmentMessage(String team,boolean resume,JarFileAssignment jar) throws IOException {
		super(Message.MSG_ASSIGNMENT);
		teamName=team;
		resumeMode=resume;
		jarData=jar.getJarFileData();
	}
	
	public AssignmentMessage(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_ASSIGNMENT) throw new IOException("Incorrect Type");
		teamName=in.readUTF();
		resumeMode=in.readBoolean();
		int ln=in.readInt();
		jarData=new byte[ln];
		in.readFully(jarData);
	}
	
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeUTF(teamName);
		out.writeBoolean(resumeMode);
		out.writeInt(jarData.length);
		out.write(jarData);
	}
	
	public byte[] getJarAssignment() {
		return jarData;
	}
	public String getTeamName() {
		return teamName;
	}
	public boolean isResumeMode() {
		return resumeMode;
	}
	
}
