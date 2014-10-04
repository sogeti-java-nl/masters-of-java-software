package nl.moj.scoreboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import nl.ctrlaltdev.util.Tool;
import nl.moj.mgmt.TeamProfileList;
import nl.moj.model.State;
import nl.moj.round.StateImpl;
import nl.moj.scoreboard.scoringrules.MoJ2006ScoringRules;

public class DukeScoreBoard extends JPanel {

	/**
	 * <code>serialVersionUID</code> indicates/is used for.
	 */
	private static final long serialVersionUID = 7179084194542676373L;

	private static final class LabelPanel extends JPanel {
		/**
		 * <code>serialVersionUID</code> indicates/is used for.
		 */
		private static final long serialVersionUID = -7255760694903865279L;
		private Font font = new Font("Verdana", Font.BOLD, 72);
		private Font small = new Font("Verdana", Font.BOLD, 10);
		private String[] names;
		private String year;

		public LabelPanel(String[] assignments) {
			names = assignments;
			year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		}

		public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(this.getBackground());
			g2d.fillRect(0, 0, getWidth(), getHeight());
			//
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(Color.black);
			g2d.setFont(font);
			//
			String s = "Masters of Java " + year;
			double sw = g2d.getFontMetrics().getStringBounds(s, 0, s.length(), g2d).getWidth();
			g2d.drawString(s, getWidth() / 2 - (int) sw / 2, getHeight() - getHeight() / 3);
			//
			for (int t = 0; t < names.length; t++) {
				//
				int n = names.length - t - 1;
				//
				g2d.setColor(PillarPanel.PILLARHILIGHT[n % PillarPanel.PILLARHILIGHT.length]);
				g2d.fillRect(16, 8 + 16 * t, 256, 16);
				//
				g2d.setColor(Color.BLACK);
				g2d.drawRect(16, 8 + 16 * t, 256, 16);
				//
				s = names[n];
				g2d.setFont(small);
				sw = g2d.getFontMetrics().getStringBounds(s, 0, s.length(), g2d).getWidth();
				g2d.drawString(s, 16 + 128 - (int) sw / 2, 8 + 16 * t + 12);
				//
			}
		}
	}

	private State state;
	private TeamProfileList teamList;
	private PillarPanel[] panels;
	private JScrollBar scrollBar;
	private int scrollDirection;
	private int pause = 80;

	public DukeScoreBoard(File stateFile, File teamFile, ScoringRules rules) throws IOException {
		super(new BorderLayout());
		this.state = new StateImpl(stateFile);
		teamList = new TeamProfileList(teamFile);
		//
		JPanel content = new JPanel();
		JScrollPane sp = new JScrollPane(content);
		scrollBar = sp.getHorizontalScrollBar();
		this.add(sp);
		//
		String[] rounds = state.getKnownRoundNames();
		//
		JPanel pillars = new JPanel();
		JPanel labelPanel = new LabelPanel(rounds);
		content.setLayout(new BorderLayout());
		content.add(pillars, BorderLayout.CENTER);
		content.add(labelPanel, BorderLayout.SOUTH);
		//
		labelPanel.setBackground(new Color(255, 160, 64));
		labelPanel.setPreferredSize(new Dimension(512, 128));
		//
		String[] teams = state.getKnownTeams();
		panels = new PillarPanel[teams.length];
		pillars.setLayout(new BoxLayout(pillars, BoxLayout.X_AXIS));
		for (int t = 0; t < teams.length; t++) {
			panels[t] = new PillarPanel(teams[t], rules.getMaxScorePerRound());
		}
		//
		Image duke = loadImage("/data/img/duke_winner1.jpg");
		Image dukeToo = loadImage("/data/img/duke_winner2.jpg");
		Image dukeLoser = loadImage("/data/img/duke_loser.jpg");
		//
		//
		boolean[] checkSkip = new boolean[teams.length];
		int[][] rounds_times = new int[rounds.length][teams.length];
		double[][] rounds_scores = new double[rounds.length][teams.length];
		//
		for (int t = 0; t < rounds.length; t++) {
			for (int y = 0; y < teams.length; y++) {
				int idx = teamList.getIndexOfTeam(teams[y]);
				if (idx >= 0) {
					String ex = teamList.getExcludeAssignment(idx);
					if (!rounds[t].equalsIgnoreCase(ex)) {
						rounds_times[t][y] = state.getScore(rounds[t], teams[y]);
					} else {
						if (checkSkip[y])
							System.out.println("Warning : Team " + teams[y] + " skips twice.");
						checkSkip[y] = true;
						rounds_times[t][y] = 0;
					}
				} else {
					rounds_times[t][y] = state.getScore(rounds[t], teams[y]);
				}
			}
			rounds_scores[t] = rules.assignScores(rounds_times[t].clone());
			//
			// System.out.print(rounds[t]);
			// for (int r=rounds[t].length();r<16;r++) System.out.print(" ");
			for (int y = 0; y < teams.length; y++) {
				panels[y].addSegment(t, rounds_scores[t][y]);
				// System.out.print(fix(prefix(String.valueOf(rounds_times[t][y]),4)+"->"+prefix(String.valueOf(rounds_scores[t][y]),4),12));
			}
			// System.out.println();
		}
		
		for (int t = 0; t < checkSkip.length; t++) {
			int idx = teamList.getIndexOfTeam(teams[t]);
			String skip = (idx >= 0 ? teamList.getExcludeAssignment(idx) : "Team not in team.properties");
			if (!checkSkip[t])
				System.out.println("Warning : Team " + teams[t] + " did not skip any assignment. (" + skip + ")");
		}
		
		for (int r = 0; r < 16; r++)
			System.out.print(" ");
		for (int t = 0; t < rounds.length; t++) {
			System.out.print(fix(rounds[t], 16));
		}
		System.out.println();
		for (int y = 0; y < teams.length; y++) {
			System.out.print(fix(teams[y], 16));
			for (int t = 0; t < rounds.length; t++) {
				System.out.print(fix(prefix(String.valueOf(rounds_times[t][y]), 5) + "->" + prefix(String.valueOf(rounds_scores[t][y]), 5), 16));
			}
			System.out.println();
		}
		
		Arrays.sort(panels, new Comparator<PillarPanel>() {
			public int compare(PillarPanel a, PillarPanel b) {
				double s1 = a.totalScore();
				double s2 = b.totalScore();
				if (s1 > s2)
					return -1;
				if (s1 < s2)
					return 1;
				return 0;
			}
		});
		//
		// Scale panels to the winners score.
		//
		for (int t = 0; t < panels.length; t++)
			panels[t].setMaxScore(panels[0].totalScore() + panels[0].totalScore() * 0.1);
		
		if (panels.length > 0)
			panels[0].setDuke(duke);
		if (panels.length > 1)
			panels[1].setDuke(dukeToo);
		if (panels.length > 2)
			panels[2].setDuke(dukeToo);
		if (panels.length > 3)
			panels[panels.length - 1].setDuke(dukeLoser);
		
		System.out.println();
		
		for (int t = 0; t < teams.length; t++) {
			pillars.add(panels[t]);
			if (t < 3) {
				System.out.println("#" + (t + 1) + " : " + panels[t].getName());
			}
		}
		
	}

	private static String fix(String s, int l) {
		if (s.length() >= l)
			return s.substring(0, l - 1) + " ";
		StringBuffer sb = new StringBuffer(s);
		while (sb.length() < l)
			sb.append(" ");
		return sb.toString();
	}

	private static String prefix(String s, int l) {
		if (s.length() > l)
			return s.substring(0, l - 1);
		StringBuffer sb = new StringBuffer(s);
		while (sb.length() < l)
			sb.insert(0, " ");
		return sb.toString();
	}

	static Image loadImage(String fileName) throws IOException {
		Toolkit tk = Toolkit.getDefaultToolkit();
		//
		URL myURL = DukeScoreBoard.class.getResource(fileName);
		if (myURL == null)
			throw new IOException("Resource " + fileName + " not found. Does it start with / ?");
		//
		return tk.createImage(myURL);
	}

	public void updateScrollBar() {
		int max = scrollBar.getModel().getMaximum();
		int min = scrollBar.getModel().getMinimum();
		int pos = scrollBar.getModel().getValue();
		int ext = scrollBar.getModel().getExtent();
		//
		if (max == ext)
			return;
		if (scrollBar.getValueIsAdjusting())
			return;
		//
		if ((scrollDirection == 0) && (pause == 0)) {
			if (pos == min)
				scrollDirection = 1;
			else
				scrollDirection = -1;
		} else
			pause--;
		if (scrollDirection > 0) {
			pos += 4;
			if (pos + ext > max) {
				pos = max - ext;
				scrollDirection = 0;
				pause = 20;
			}
			scrollBar.setValue(pos);
		} else if (scrollDirection < 0) {
			pos -= 4;
			if (pos < min) {
				pos = min;
				scrollDirection = 0;
				pause = 20;
			}
			scrollBar.setValue(pos);
		}
	}

	public static void main(String[] args) throws IOException {
		args = Tool.parseArgs(args);
		if (args.length != 2) {
			System.out.println("Usage : DukeScoreBoard [state.csv] [team.properties]");
			System.exit(0);
		}
		//
		File state = new File(args[0]);
		File team = new File(args[1]);
		if (state.exists() && (team.exists())) {
			//
			JFrame f = new JFrame("Masters Of Java - Scoreboard");
			final DukeScoreBoard dsc = new DukeScoreBoard(state, team, new MoJ2006ScoringRules());
			f.getContentPane().add(dsc, BorderLayout.CENTER);
			f.setSize(1024, 768);
			f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			//
			f.setVisible(true);
			//
			Thread t = new Thread(new Runnable() {
				public void run() {
					// int cnt=0;
					while (true) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException ex) {
							//
						}
						// final boolean update=(++cnt%100==0);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								dsc.updateScrollBar();
							}
						});
					}
				}
			});
			t.setDaemon(true);
			t.start();
			//
		} else {
			if (!state.exists())
				System.err.println("File '" + state + "' does not exist.");
			if (!team.exists())
				System.err.println("File '" + team + "' does not exist.");
		}
	}

}
