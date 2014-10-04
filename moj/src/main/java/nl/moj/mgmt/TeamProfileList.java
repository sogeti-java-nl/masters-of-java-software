package nl.moj.mgmt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import nl.ctrlaltdev.util.Encoder;
import nl.ctrlaltdev.util.Tool;

/**
 * TeamProfileList : container object for team profiles. Basically a wrapper
 * round a properties file.
 */

public class TeamProfileList {

	private static final String HEX = "0123456789ABCDEF";
	public static final String WELKOM = "604F7D0C789F79B50303C9DF83F405ADF3007955";
	private static final String VALIDNAMECHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static String getString(ResourceBundle rb, String name, String std) {
		try {
			return rb.getString(name);
		} catch (MissingResourceException ex) {
			return std;
		}
	}

	private List<String> teams = new ArrayList<>();
	private List<String> passwords = new ArrayList<>();
	private List<String> displayNames = new ArrayList<>();
	private List<String> excludedAssignments = new ArrayList<>();
	private List<Integer> registrationIndex = new ArrayList<>();

	/**
	 * creates an empty team profile list.
	 */
	public TeamProfileList() {
		//
	}

	/** reads a team profile list from a file. */
	public TeamProfileList(File f) throws IOException {
		this(new FileInputStream(f));
	}

	/** reads a team profile list from a InputStream. */
	public TeamProfileList(InputStream in) throws IOException {
		this(new PropertyResourceBundle(in));
	}

	/**
	 * reads the team profiles from the specified resource bundle.
	 */
	public TeamProfileList(ResourceBundle res) {
		String[] tmp = Tool.cut(res.getString("TEAMS"), ",");
		// Sort on name
		Arrays.sort(tmp);
		//
		for (int t = 0; t < tmp.length; t++) {
			String pwd = res.getString(tmp[t] + ".PWD");
			String name = getString(res, tmp[t] + ".NAME", tmp[t]);
			String exclude = getString(res, tmp[t] + ".EXCLUDE", "");
			String idx = getString(res, tmp[t] + ".INDEX", "-1");
			//
			checkTeamName(tmp[t]);
			checkPassword(pwd);
			checkDisplayName(tmp[t], name);
			checkExclude(exclude);
			//
			teams.add(tmp[t]);
			passwords.add(pwd);
			displayNames.add(name);
			excludedAssignments.add(exclude);
			registrationIndex.add(new Integer(Integer.parseInt(idx)));
			//
		}
	}

	public int getIndexOfTeam(String teamName) {
		for (int t = 0; t < teams.size(); t++) {
			if (getTeamName(t).equals(teamName))
				return t;
		}
		return -1;
	}

	/** returns the number of teams in the TeamProfilesList */
	public int getNumberOfTeams() {
		return teams.size();
	}

	/** returns the team name with the specified index */
	public String getTeamName(int nr) {
		checkIndex(nr);
		return teams.get(nr);
	}

	/** returns the team password with the specified index */
	public String getPassword(int nr) {
		checkIndex(nr);
		return passwords.get(nr);
	}

	/** returns the team displayName with the specified index */
	public String getDisplayName(int nr) {
		checkIndex(nr);
		return displayNames.get(nr);
	}

	/** returns the team displayName with the specified index */
	public String getExcludeAssignment(int nr) {
		checkIndex(nr);
		return excludedAssignments.get(nr);
	}

	public Integer getRegistrationIndex(int nr) {
		checkIndex(nr);
		return registrationIndex.get(nr);
	}

	/** returns true if the password is encoded */
	public boolean isEncodedPassword(int idx) {
		String pwd = getPassword(idx);
		int cnt = 0;
		for (int t = 0; t < pwd.length(); t++) {
			if (HEX.indexOf(pwd.charAt(t)) >= 0)
				cnt++;
		}
		return (cnt == WELKOM.length());
	}

	/** changes the team name at the specified index */
	public void setTeamName(int idx, String name) {
		checkIndex(idx);
		checkTeamName(name);
		teams.set(idx, name);
	}

	/** changes the display name at the specified index */
	public void setDisplayName(int idx, String displayName) {
		checkIndex(idx);
		displayName = checkDisplayName(getTeamName(idx), displayName);
		displayNames.set(idx, displayName);
	}

	/** changes the password at the specified index */
	public void setPassword(int idx, String pwd) {
		checkIndex(idx);
		checkPassword(pwd);
		passwords.set(idx, pwd);
	}

	public void setExcludedAssignment(int idx, String ex) {
		checkIndex(idx);
		checkExclude(ex);
		excludedAssignments.set(idx, ex);
	}

	/** checks if the index is valid. */
	protected void checkIndex(int idx) {
		if ((idx < 0) || (idx >= teams.size()))
			throw new RuntimeException("Invalid Team index.");
	}

	/**
	 * checks if the team name is not null and not empty and contains only valid
	 * characters.
	 */
	protected void checkTeamName(String name) {
		if ((name == null) || (name.length() == 0))
			throw new RuntimeException("Cannot have a team with an empty or NULL name.");
		name = name.toUpperCase();
		for (int t = 0; t < name.length(); t++) {
			if (VALIDNAMECHARS.indexOf(name.charAt(t)) < 0)
				throw new RuntimeException("Invalid character '" + name.charAt(t) + "' in name " + name);
		}
	}

	/** checks if the password is not null and not empty. */
	protected void checkPassword(String pwd) {
		if ((pwd == null) || (pwd.length() == 0))
			throw new RuntimeException("Cannot have a team with an empty NULL or password.");
	}

	protected void checkExclude(String ex) {
	}

	/**
	 * checks the display name.
	 * 
	 * @param name
	 *            the name of the team.
	 * @param displayName
	 *            the display name of the team.
	 * @return the display name to be used.
	 */
	protected String checkDisplayName(String name, String displayName) {
		if ((displayName == null) || (displayName.length() == 0))
			displayName = name;
		return displayName;
	}

	/**
	 * adds the specified team profile.
	 * 
	 * @throws RuntimeException
	 *             if any of the values are invalid or if a team with the
	 *             specified name already exists.
	 */
	public void addTeam(String name, String pwd, String displayName, String ex) {
		checkTeamName(name);
		checkPassword(pwd);
		displayName = checkDisplayName(name, displayName);
		checkExclude(ex);
		//
		if (teams.contains(name))
			throw new RuntimeException("Duplicate name : " + name);
		if ((displayName == null) || (displayName.length() == 0))
			displayName = name;
		//
		teams.add(name);
		passwords.add(pwd);
		displayNames.add(displayName);
		excludedAssignments.add(ex);
		registrationIndex.add(new Integer(registrationIndex.size()));
		//
	}

	/**
	 * removes the team at the specified index.
	 * 
	 * @throws RuntimeException
	 *             if the index is invalid.
	 */
	public void deleteTeam(int idx) {
		//
		checkIndex(idx);
		//
		teams.remove(idx);
		passwords.remove(idx);
		displayNames.remove(idx);
		excludedAssignments.remove(idx);
		registrationIndex.remove(idx);
		//
	}

	public int[] getRegistrationSequence() {
		boolean invalid = false;
		int[] sx = new int[registrationIndex.size()];
		int[] tx = new int[registrationIndex.size()];
		for (int t = 0; t < tx.length; t++) {
			sx[t] = t;
			tx[t] = getRegistrationIndex(t).intValue();
			if (tx[t] < 0)
				invalid = true;
		}
		//
		if (invalid) {
			for (int t = 0; t < tx.length; t++)
				tx[t] = t;
			return tx;
		}
		//
		// AAarghh Bubblesort.
		//
		for (int t = 0; t < tx.length; t++) {
			for (int y = t; y < tx.length; y++) {
				if (tx[t] > tx[y]) {
					int tmp = tx[t];
					tx[t] = tx[y];
					tx[y] = tmp;
					tmp = sx[t];
					sx[t] = sx[y];
					sx[y] = tmp;
				}
			}
		}
		//
		return sx;
	}

	/**
	 * Saves the current settings to the specified output file and encodes any
	 * non encoded passwords.
	 * 
	 * @param out
	 *            the outputstream to write to. The output stream is not closed.
	 * @throws IOException
	 *             when a write error occurs.
	 * @throws NoSuchAlgorithmException
	 *             if the password encoding cannot be done.
	 */
	public void save(OutputStream out, boolean encode) throws IOException, NoSuchAlgorithmException {
		BufferedWriter wout = new BufferedWriter(new OutputStreamWriter(out));
		wout.write("#");
		wout.newLine();
		wout.write("# Team properties : Indentifies teams and their passwords.");
		wout.newLine();
		wout.write("# As these are constant for multiple rounds, they are in a ");
		wout.newLine();
		wout.write("# separate file.");
		wout.newLine();
		wout.write("#");
		wout.newLine();
		wout.write("TEAMS=" + Tool.arrayToString(teams.toArray()));
		wout.newLine();
		for (int t = 0; t < teams.size(); t++) {

			if (isEncodedPassword(t)) {
				wout.write(teams.get(t) + ".PWD=" + passwords.get(t));
			} else {
				if (encode)
					passwords.set(t, Encoder.hash(passwords.get(t).toString()));
				wout.write(teams.get(t) + ".PWD=" + passwords.get(t));
			}
			wout.newLine();
			if (!teams.get(t).equals(displayNames.get(t))) {
				wout.write(teams.get(t) + ".NAME=" + displayNames.get(t));
				wout.newLine();
			}
			if (excludedAssignments.get(t) != null) {
				wout.write(teams.get(t) + ".EXCLUDE=" + excludedAssignments.get(t));
				wout.newLine();
			}
			if (registrationIndex.get(t) != null) {
				wout.write(teams.get(t) + ".INDEX=" + registrationIndex.get(t));
				wout.newLine();
			}
		}
		wout.newLine();
		wout.flush();
	}

	/**
	 * saves the file as CSV file (; separated). Does not encode the passwords.
	 * 
	 * @param out
	 *            the outputstream to write the data to.
	 * @throws IOException
	 *             if something goes wrong.
	 */
	public void saveAsCSV(OutputStream out, String[] lineup) throws IOException {
		BufferedWriter wout = new BufferedWriter(new OutputStreamWriter(out));
		wout.write("Nr;Login;Password;DisplayName;ExcludedAssignment;ExcludedRound;");
		wout.newLine();
		//
		int[] seq = getRegistrationSequence();
		//
		for (int t = 0; t < teams.size(); t++) {
			int idx = seq[t];
			wout.write(String.valueOf(registrationIndex.get(idx).intValue() + 1));
			wout.write(";");
			wout.write(String.valueOf(teams.get(idx)));
			wout.write(";");
			wout.write(String.valueOf(passwords.get(idx)));
			wout.write(";");
			wout.write(String.valueOf(displayNames.get(idx)));
			wout.write(";");
			wout.write(String.valueOf(excludedAssignments.get(idx)));
			wout.write(";");
			boolean found = false;
			for (int r = 0; r < lineup.length; r++) {
				if (excludedAssignments.get(idx).equals(lineup[r])) {
					wout.write(String.valueOf(r + 1));
					found = true;
				}
			}
			if (!found)
				wout.write("-1");
			wout.write(";");
			wout.newLine();
		}
		wout.flush();
	}

}
