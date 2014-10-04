package nl.moj.mgmt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import nl.ctrlaltdev.ui.Build;
import nl.ctrlaltdev.util.Tool;

/**
 * A crude but effective Component to Manage the User List
 * 
 * @author E.Hooijmeijer
 */

public class TeamProfileListManager extends JPanel {

	/**
	 * <code>serialVersionUID</code> indicates/is used for.
	 */
	private static final long serialVersionUID = -3007178908955274735L;
	private static final int WHAT_NAME = 0;
	private static final int WHAT_PWD = 1;
	private static final int WHAT_DISPLAY = 2;
	private static final int WHAT_ASSIGNMENT = 3;
	private static final Color ODD = new Color(240, 240, 240);
	private static final Color EVEN = new Color(220, 220, 220);

	/**
	 * Checks the input texts for correctness and displays an error message if
	 * the value is invalid.
	 */
	private class FieldInputVerifier extends InputVerifier implements ActionListener {
		private int idx, what;
		private JComponent txtField;
		private String msg;

		public FieldInputVerifier(JComponent txt, int idx, int what) {
			txtField = txt;
			this.idx = idx;
			this.what = what;
		}

		@SuppressWarnings("unchecked")
		public boolean verify(JComponent e) {
			//
			try {
				switch (what) {
				case WHAT_NAME:
					myTPL.setTeamName(idx, ((JTextField) txtField).getText());
					break;
				case WHAT_PWD:
					myTPL.setPassword(idx, ((JTextField) txtField).getText());
					break;
				case WHAT_DISPLAY:
					myTPL.setDisplayName(idx, ((JTextField) txtField).getText());
					break;
				case WHAT_ASSIGNMENT:
					myTPL.setExcludedAssignment(idx, (String) ((JComboBox<String>) txtField).getModel().getSelectedItem());
					break;
				}
				return true;
			} catch (RuntimeException ex) {
				msg = ex.getMessage();
				return false;
			}
			//
		}

		public void actionPerformed(ActionEvent e) {
			verify((JComponent) e.getSource());
		}

		public boolean shouldYieldFocus(JComponent input) {
			msg = null;
			boolean result = super.shouldYieldFocus(input);
			if ((!result) && (msg != null)) {
				InputVerifier old = input.getInputVerifier();
				// Neccecary to prevent endless popups.
				input.setInputVerifier(null);
				try {
					JOptionPane.showMessageDialog(input, msg);
				} finally {
					input.setInputVerifier(old);
				}
			}
			return result;
		}

	}

	/**
	 * Deletes a team after confirmation.
	 */
	private class RemoveAction implements ActionListener {
		private int idx;

		public RemoveAction(int idx) {
			this.idx = idx;
		}

		public void actionPerformed(ActionEvent e) {
			if (JOptionPane.showConfirmDialog(TeamProfileListManager.this, "Delete this team ?", "Sure", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				myTPL.deleteTeam(idx);
				rebuildTeamList(myTPL);
			}
		}
	}

	/**
	 * Asks for the team name and adds it to the list.
	 */
	private class AddAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String tname = JOptionPane.showInputDialog(TeamProfileListManager.this, "Enter team name");
			if (tname == null)
				return;
			try {
				myTPL.addTeam(tname, "Welkom", "", null);
			} catch (RuntimeException ex) {
				JOptionPane.showMessageDialog(TeamProfileListManager.this, ex.getMessage());
				return;
			}
			rebuildTeamList(myTPL);
		}
	}

	/**
	 * Erases the current password.
	 */
	private class ClearPwdAction implements ActionListener {
		private JButton b;
		private JTextField f;

		public ClearPwdAction(JButton b, JTextField f) {
			this.b = b;
			this.f = f;
		}

		public void actionPerformed(ActionEvent e) {
			f.setEnabled(true);
			f.setText("");
			b.setEnabled(false);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					f.requestFocus();
				}
			});
		}
	}

	/**
	 * Saves the current list to a file
	 */
	private class SaveAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//
			// Check if there is a file.
			//
			if (mySourceFile == null) {
				JFileChooser fc = new JFileChooser(new File("."));
				int returnVal = fc.showSaveDialog(TeamProfileListManager.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					mySourceFile = fc.getSelectedFile();
				} else
					return;
			}
			if (mySourceFile == null)
				return;
			//
			// Save the File.
			//
			try {
				FileOutputStream fout = new FileOutputStream(mySourceFile);
				try {
					myTPL.save(fout, encodePwd.isSelected());
				} finally {
					fout.close();
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(TeamProfileListManager.this, "Error saving file : " + ex);
			}
			//
			rebuildTeamList(myTPL);
			//
		}
	}

	private class SaveAsCSVAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			File csvFile = null;
			JFileChooser fc = new JFileChooser(new File("."));
			int returnVal = fc.showSaveDialog(TeamProfileListManager.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				csvFile = fc.getSelectedFile();
			} else
				return;
			if (csvFile == null)
				return;
			//
			// Save the File.
			//
			try {
				FileOutputStream fout = new FileOutputStream(csvFile);
				try {
					myTPL.saveAsCSV(fout, skipMode ? roundLineup : new String[0]);
				} finally {
					fout.close();
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(TeamProfileListManager.this, "Error saving file : " + ex);
			}
			//
		}
	}

	private class GenerateFormsAction implements ActionListener {
		private String[] TIMES = new String[] { "09:00 - 09:30 uur", "09:30 - 10:00 uur", "10:15 - 10:45 uur", "11:00 - 11:30 uur",
				"11:45 - 12:15 uur", "12:15 - 13:30 uur", "13:30 - 14:00 uur", "14:15 - 14:45 uur", "15:15 - 15:45 uur", "16:00 uur" };
		private int[] ROUNDS = new int[] { -3, -4, 1, 2, 3, -1, 4, 5, 6, -2 };

		public void actionPerformed(ActionEvent e) {
			//
			// check if there are sufficient lined up rounds if we're working in
			// skip mode.
			//
			if ((skipMode) && (roundLineup.length < 6))
				throw new RuntimeException("6 Lined up Rounds are required for generating these forms in skip mode.");
			//
			int year = Calendar.getInstance().get(Calendar.YEAR);
			//
			File root = new File("./data/teams/");
			if (!root.exists())
				root.mkdirs();
			//
			for (int t = 0; t < myTPL.getNumberOfTeams(); t++) {
				String displayName = myTPL.getDisplayName(t);
				String loginName = myTPL.getTeamName(t);
				String password = myTPL.getPassword(t);
				Integer regIdx = myTPL.getRegistrationIndex(t);
				String exclude = myTPL.getExcludeAssignment(t);
				//
				StringBuffer sb = new StringBuffer();
				sb.append("<html><body><center>");
				sb.append("<h1>Masters of Java " + year + "</h1>");
				sb.append("</br></br>");
				sb.append("<table border=0 width=512>\n");
				//
				sb.append("<tr><th style=\"border-bottom:1px solid black;\" colspan=2><B>Team gegevens</B></th></tr>\n");
				//
				sb.append("<tr><td width=\"50%\" align=right>Team Naam :</td><td width=\"50%\">");
				sb.append(displayName);
				sb.append("</td></tr>\n");
				//
				sb.append("<tr><td align=right>Inschrijving :</td><td>");
				sb.append((regIdx.intValue() + 1) + "/" + myTPL.getNumberOfTeams());
				sb.append("</td></tr>\n\n");
				//
				sb.append("<tr><td align=right><B>Login :</B></td><td>");
				sb.append(loginName);
				sb.append("</td></tr>");
				//
				sb.append("<tr><td align=right><B>Password :</B></td><td>");
				sb.append(password);
				sb.append("</td></tr>\n");
				//
				sb.append("</table>\n");
				sb.append("<br/>Je Login is niet case-sensitive. Je Password is dat wel !");
				//
				sb.append("<br/><br/><br/>");
				//
				sb.append("<table border=0 width=512>\n");
				//
				sb.append("<tr><th style=\"border-bottom:1px solid black;\" colspan=2><B>Agenda</B></th></tr>\n");
				//
				boolean skip = false;
				for (int r = 0; r < ROUNDS.length; r++) {
					sb.append("<tr><td align=center>");
					sb.append(TIMES[r]);
					sb.append("</td><td>");
					switch (ROUNDS[r]) {
					case -4:
						sb.append("Proefronde");
						break;
					case -3:
						sb.append("Ontvangst en registratie");
						break;
					case -2:
						sb.append("Prijsuitreiking");
						break;
					case -1:
						sb.append("Lunch pauze");
						break;
					default:
						if ((skipMode) && (exclude.equals(roundLineup[ROUNDS[r] - 1]))) {
							skip = true;
							sb.append("Ronde " + ROUNDS[r] + " : Telt niet mee");
						} else {
							sb.append("Ronde " + ROUNDS[r]);
						}
						break;
					}
					sb.append("</td></tr>\n");
				}
				sb.append("</table>\n");
				//
				sb.append("<p style=\"margin-left:10%;margin-right:10%;\" align=left>");
				if (skip) {
					sb.append("Ieder team speelt <i>5 van de 6</i> rondes. De ronde die voor jouw team niet telt ");
					sb.append("staat hierboven aangegeven. De winnaar van een ronde is diegene die als ");
					sb.append("eerste zijn goede oplossing instuurt en scoort hiervoor <i>20</i> punten. Volgende ");
					sb.append("succesvolle inzendingen krijgen respectievelijk : 18,16,14,12 en 10 punten. ");
					sb.append("Inzendingen die daarna komen krijgen 1 punt voor de moeite. Lever je iets in ");
					sb.append("dat niet goed werkt of je levert helemaal niets in dan krijg je 0 punten. ");
				} else {
					sb.append("Ieder team speelt alle ronden. Een ronde duurt 30 minuten. Voor het insturen van een ");
					sb.append("goede oplossing scoor je (400 + het aantal seconden dat je over hebt) punten. ");
					sb.append("Lever je iets in dat niet goed werkt of je levert helemaal niets in dan krijg je 0 punten. ");
					sb.append("Of iets een goede oplossing is wordt bepaald door de unit tests van die opgave. ");
					sb.append("Sommige opgaven hebben een extra verborgen test(!). Welke opgaven dat zijn wordt vantevoren verteld. ");
					sb.append("Winnaar is dat team dat over alle 6 de rondes de meeste punten weet te scoren. ");
				}
				sb.append("</p>");
				//
				sb.append("</br><I>Heel veel plezier en succes met Masters of Java !</I>");
				//
				sb.append("</center></body></html>\n");
				//
				File fout = new File(root, "" + (regIdx.intValue() + 1) + loginName + ".html");
				try {
					FileWriter f = new FileWriter(fout);
					try {
						f.write(sb.toString());
					} finally {
						f.close();
					}
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(list, "Error writing '" + fout.getName() + "' : " + ex.getMessage());
					return;
				}
			}
		}

	}

	private TeamProfileList myTPL;
	private File mySourceFile;
	private JScrollPane list;
	private JButton saveButton = new JButton("Save");
	private JButton addButton = new JButton("Add");
	private JButton formButton = new JButton("Generate Login Forms");
	private JButton csvButton = new JButton("Save as CSV");
	private JCheckBox encodePwd = new JCheckBox("Encode passwords");
	private String[] roundLineup;
	private String[] assignmentModel;
	private boolean skipMode;

	/** constructs an empty TeamProfileListManager with no associated file */
	public TeamProfileListManager() {
		super(new BorderLayout());
		myTPL = new TeamProfileList();
		init(null);
	}

	/** returns the current source file or null. */
	public File getFile() {
		return mySourceFile;
	}

	/** returns the color for odd rows */
	public Color getOddColor() {
		return ODD;
	}

	/** returns the color for even rows */
	public Color getEvenColor() {
		return EVEN;
	}

	/** constructs a TeamProfileListManager from the specified team file */
	public TeamProfileListManager(File src) throws IOException {
		super(new BorderLayout());
		mySourceFile = src;
		myTPL = new TeamProfileList(src);
		init(null);
	}

	/** constructs a TeamProfileListManager from the specified team file */
	public TeamProfileListManager(File src, String lineup) throws IOException {
		super(new BorderLayout());
		mySourceFile = src;
		myTPL = new TeamProfileList(src);
		init(Tool.cut(lineup, ","));
	}

	/** initialises the GUI */
	protected void init(String[] lineup) {
		findAssignments(new File("./data/cases/"));
		if (lineup != null)
			setLineup(lineup);
		rebuildTeamList(myTPL);
		this.add(buildButtonBar(), BorderLayout.SOUTH);
	}

	protected void setLineup(String[] assignments) {
		roundLineup = assignments;
		skipMode = true;
	}

	protected void findAssignments(File dir) {
		List<String> cb = new ArrayList<>();
		cb.add("");
		findAssignments(dir, cb);
		assignmentModel = cb.toArray(new String[cb.size()]);
		skipMode = false;
	}

	protected void findAssignments(File f, List<String> cb) {
		File[] jars = f.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isDirectory() && (!pathname.getName().startsWith(".")))
					return true;
				return pathname.isFile() && pathname.getName().endsWith(".jar");
			}
		});

		for (int t = 0; t < jars.length; t++) {
			if (jars[t].isDirectory()) {
				findAssignments(jars[t], cb);
			} else {
				cb.add(jars[t].getName().substring(0, jars[t].getName().length() - 4));
			}
		}
	}

	/** returns the current team profile list */
	protected TeamProfileList getTeamProfileList() {
		return myTPL;
	}

	/** builds the button bar at the bottom of the screen */
	protected JComponent buildButtonBar() {
		addButton.addActionListener(new AddAction());
		saveButton.addActionListener(new SaveAction());
		formButton.addActionListener(new GenerateFormsAction());
		csvButton.addActionListener(new SaveAsCSVAction());
		return new Build.RFP(new Build.BOXX(new JComponent[] { formButton, null, encodePwd, null, addButton, null, csvButton, saveButton }));
	}

	/**
	 * rebuilds the team list in the userinterface by removing any existing team
	 * list components and adding the new one.
	 */
	protected void rebuildTeamList(TeamProfileList l) {
		if (list != null)
			remove(list);
		list = new JScrollPane(new Build.NBOXY(new JComponent[] { buildTeamList(myTPL) }));
		add(list, BorderLayout.CENTER);
		invalidate();
		repaint();
		validate();
	}

	/**
	 * actually builds a TeamList
	 */
	protected JComponent buildTeamList(TeamProfileList l) {
		//
		JPanel result = new JPanel(new GridLayout(0, 5));
		//
		result.add(new Build.CFP(new JLabel("TeamName")));
		result.add(new Build.CFP(new JLabel("Password")));
		result.add(new Build.CFP(new JLabel("Display Name")));
		result.add(new Build.CFP(new JLabel("Exclude")));
		result.add(new Build.CFP(new JLabel("")));
		//
		int[] seq = l.getRegistrationSequence();
		//
		for (int t = 0; t < l.getNumberOfTeams(); t++) {
			//
			int idx = seq[t];
			//
			JTextField nameField = new JTextField(l.getTeamName(idx), 12);
			JTextField pwdField = new JTextField(l.getPassword(idx), 12);
			JTextField displayField = new JTextField(l.getDisplayName(idx), 12);
			JComboBox<String> excludedField = new JComboBox<>();
			excludedField.setModel(new DefaultComboBoxModel<String>(assignmentModel));
			excludedField.setEditable(true);
			excludedField.setSelectedItem(l.getExcludeAssignment(idx));
			//
			JButton removeButton = new JButton("Del");
			JButton pwdButton = new JButton("Pwd");
			//
			removeButton.setVerifyInputWhenFocusTarget(false);
			removeButton.addActionListener(new RemoveAction(idx));
			pwdButton.addActionListener(new ClearPwdAction(pwdButton, pwdField));
			//
			nameField.setInputVerifier(new FieldInputVerifier(nameField, idx, WHAT_NAME));
			//
			pwdField.setInputVerifier(new FieldInputVerifier(pwdField, idx, WHAT_PWD));
			pwdField.setEnabled(!myTPL.isEncodedPassword(idx));
			pwdButton.setEnabled(!pwdField.isEnabled());
			//
			displayField.setInputVerifier(new FieldInputVerifier(displayField, idx, WHAT_DISPLAY));
			//
			excludedField.addActionListener(new FieldInputVerifier(excludedField, idx, WHAT_ASSIGNMENT));
			//
			pwdField.setCaretPosition(0);
			//
			Color c = ((t % 2 == 0) ? getOddColor() : getEvenColor());
			//
			JLabel lidx = new JLabel(String.valueOf(l.getRegistrationIndex(idx)));
			lidx.setMinimumSize(new Dimension(24, 24));
			lidx.setPreferredSize(new Dimension(24, 24));
			//
			result.add(new Build.LFP(new JComponent[] { lidx, nameField }, c));
			result.add(new Build.LFP(pwdField, c));
			result.add(new Build.LFP(displayField, c));
			result.add(new Build.LFP(excludedField, c));
			result.add(new Build.LFP(new JComponent[] { removeButton, pwdButton }, c));
			//
		}
		//
		return result;
	}

	/** return true if this part of the GUI can close */
	public boolean canClose() {
		return true;
	}

	/**
	 * Team Profile List manager stand alone GUI. Specify an existing team file
	 * on the command line to open it.
	 */
	public static void main(String[] args) throws IOException {
		//
		args = Tool.parseArgs(args);
		//
		final TeamProfileListManager tpm = (args.length == 2 ? new TeamProfileListManager(new File(args[0]), args[1])
				: (args.length == 1 ? new TeamProfileListManager(new File(args[0])) : new TeamProfileListManager()));
		//
		JFrame f = new JFrame("Team Profile Manager (" + tpm.getFile() + ")");
		f.setSize(640, 480);
		f.getContentPane().add(tpm);
		f.setVisible(true);
		//
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (tpm.canClose())
					System.exit(0);
			}
		});
		//
	}
}
