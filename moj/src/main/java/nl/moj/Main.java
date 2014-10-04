package nl.moj;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import nl.ctrlaltdev.io.OutputRedirector;
import nl.ctrlaltdev.ioc.ApplicationBuilder;
import nl.ctrlaltdev.util.SimpleLogFormatter;
import nl.ctrlaltdev.util.Tool;
import nl.moj.assignment.JarFileAssignment;
import nl.moj.banner.BannerPanel;
import nl.moj.clock.ClockPanel;
import nl.moj.clock.SimpleClock;
import nl.moj.compile.Compiler;
import nl.moj.gamerules.MoJ2006CompetitionGameRules;
import nl.moj.gfx.RoundList;
import nl.moj.mgmt.TeamProfileList;
import nl.moj.model.Assignment;
import nl.moj.model.Clock;
import nl.moj.model.GameRules;
import nl.moj.model.Round;
import nl.moj.model.State;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool;
import nl.moj.round.RoundImpl;
import nl.moj.round.StateImpl;
import nl.moj.round.TeamImpl;
import nl.moj.scheduler.OneRoundScheduler;
import nl.moj.security.SandboxSecurityManager;
import nl.moj.server.socket.SocketGameServer;
import nl.moj.sfx.SoundEffects;
import nl.moj.workspace.factory.LocalWorkspaceFactory;
import nl.moj.workspace.factory.MultiRemoteWorkspaceFactory;
import nl.moj.workspace.factory.WorkspaceFactory;

/**
 * Masters of Java : A simple environment to do programming contests, JavaOne
 * style. Main : Initialises and links the various components of the system. A
 * fine mess so to say. (C) 2004-2007 E.Hooijmeijer / 42 B.V. -
 * http://www.2en40.nl and or http://www.ctrl-alt-dev.nl
 */

public class Main {

	private static final String RESUME = "resume";

	private static State myState;
	private static State.Writer myStateWriter;
	private static boolean running;

	/** creates the teams and their workspaces out of the team.properties file */
	protected static void buildTeams(File file, Round rnd, ApplicationBuilder root, State.Writer wr, SoundEffects sfx) throws IOException {
		TeamProfileList res = new TeamProfileList(file);
		for (int t = 0; t < res.getNumberOfTeams(); t++) {
			//
			String name = res.getTeamName(t);
			String pwd = res.getPassword(t);
			String displayname = res.getDisplayName(t);
			//
			Assignment assignment = (Assignment) root.get(Assignment.class);
			//
			GameRules gameRules = (GameRules) root.get(GameRules.class);
			//
			TeamImpl ti = new TeamImpl(name, displayname, pwd, assignment, gameRules, wr, sfx);
			Workspace ws = workspaceFactory.createWorkspace(name, ti, ti);
			ti.setWorkspace(ws);
			//
			rnd.addTeam(ti);
			//
		}
	}

	/** initialises the logging */
	private static void initLogging() throws IOException {
		//
		SimpleLogFormatter.clearLogConfig();
		SimpleLogFormatter.addConsoleLogging();
		SimpleLogFormatter.addFileLogging("./MOJ%u.log");
		SimpleLogFormatter.verbose();
		SimpleLogFormatter.info("java");
		SimpleLogFormatter.info("javax");
		SimpleLogFormatter.info("sun");
		//
		Logger.getLogger("").log(Level.INFO, "Starting Masters Of Java");
		//
	}

	private static WorkspaceFactory workspaceFactory;

	/** main method */
	public static void main(String[] args) throws Throwable {
		//
		// Initialise output redirection.
		//
		OutputRedirector.getSingleton();
		//
		args = Tool.parseArgs(args);
		if ((args.length != 2) && (args.length != 3) && (args.length != 4)) {
			System.out.println("Usage : MastersOfJava [round.jar]|[round dir] [team.properties] <workspaceHosts> <resume>");
			System.out.println("        workspaceHosts is a list of comma separated workspace servers : 127.0.0.1:8081,127.0.0.1:8082");
			System.out.println("        The resume option allows a round to be continued.");
			System.exit(0);
		} else {
			System.out.println("Masters of Java : A simple environment to do programming contests, JavaOne style. ");
			System.out.println("(C)2004-2007 E.Hooijmeijer/42 B.V.-http://www.ctrl-alt-dev.nl, http://www.2en40.nl");
			System.out.println("----------------------------------------------------------------------------------");
		}
		//
		boolean resumeMode = false;
		//
		try {
			//
			// Check if the compiler can be instantiated.
			//
			new Compiler();
			//
			// Initialise Logging
			//
			initLogging();
			//
			// Check the round.properties and team.properties
			//
			File src = new File(args[0]);
			File team = new File(args[1]);
			if (!src.exists())
				throw new IOException("File '" + src + "' does not exist.");
			if (!team.exists())
				throw new IOException("File '" + team + "' does not exist.");
			//
			// Construct ApplicationBuilder and register ProcessPool and
			// assignment file.
			//
			ApplicationBuilder root = new ApplicationBuilder();
			root.register(root);
			root.register(ProcessPool.class, new ProcessPool(16));
			root.register(File.class, src);
			root.register(OutputRedirector.class, OutputRedirector.getSingleton());

			//
			// Read configuration
			//
			ThreadGroup tmp = new ThreadGroup("Tester-ThreadGroup");
			SandboxSecurityManager ssm = new SandboxSecurityManager(tmp);
			System.setSecurityManager(ssm);
			//
			if (src.getName().endsWith(".jar")) {
				root.build(new Class<?>[] { JarFileAssignment.class });
			}
			//
			// Fetch State data (if any)
			//
			root.register(new StateImpl(new File("./state.csv")));
			myState = (State) root.get(State.class);
			myStateWriter = (State.Writer) root.get(State.Writer.class);
			//
			// Construct Workspace factory.
			//
			if ((args.length > 2) && (!RESUME.equalsIgnoreCase(args[2]))) {
				String[] hosts = Tool.cut(args[2], ",");
				int[] ports = new int[hosts.length];
				for (int t = 0; t < hosts.length; t++) {
					if (hosts[t].indexOf(':') < 0) {
						ports[t] = 8081;
					} else {
						ports[t] = Integer.parseInt(hosts[t].substring(hosts[t].indexOf(':') + 1));
						hosts[t] = hosts[t].substring(0, hosts[t].indexOf(':'));
					}
				}
				Logger.getLogger("Using MultiRemoteWorkspaceFactory with " + hosts.length + " hosts.");
				workspaceFactory = new MultiRemoteWorkspaceFactory(hosts, ports, myState, myStateWriter);
			} else {
				Logger.getLogger("Using LocalWorkspaceFactory.");
				workspaceFactory = new LocalWorkspaceFactory();
			}
			//
			// Check if we are in resume mode.
			//
			if (((args.length == 3) && (args[2].equalsIgnoreCase(RESUME))) || ((args.length == 4) && (args[3].equalsIgnoreCase(RESUME)))) {
				Logger.getLogger("").warning("RESUME Mode enabled.");
				resumeMode = true;
			}

			Assignment assignment = (Assignment) root.get(Assignment.class);
			final Clock clock = new SimpleClock(assignment.getDuration());
			ClockPanel clockPanel = new ClockPanel(clock, (Assignment) root.get(Assignment.class));
			GameRules gameRules = new MoJ2006CompetitionGameRules(clock);
			root.register(clock);
			root.register(gameRules);
			SoundEffects sfx = new SoundEffects();
			clock.addNotifier(sfx);

			//
			// Build the round.
			//
			root.build(new Class<?>[] { RoundImpl.class });
			final Round rnd = (Round) root.get(Round.class);
			buildTeams(team, rnd, root, myStateWriter, sfx);
			rnd.load(myState);

			//
			// Check State
			//
			if (rnd.isStarted() && (!resumeMode)) {
				if (rnd.isFinished()) {
					Logger.getLogger("").warning("This round has already finished.");
					System.exit(0);
				} else {
					Logger.getLogger("").warning("This round was already started and can only be continued in resume-mode.");
					System.exit(0);
				}
			} else if (!rnd.isStarted() && (resumeMode)) {
				Logger.getLogger("").warning("The round was not started, so Resume mode is invalid.");
				resumeMode = false;
				Logger.getLogger("").warning("Cleared RESUME mode.");
			}
			//
			rnd.loadAssignment(resumeMode);
			//
			File bannersDir = new File(Main.class.getResource("/data/banners/").getFile());
			BannerPanel bp = new BannerPanel(bannersDir);
			RoundList rndList = new RoundList(rnd, bp, root);
			clock.addNotifier(bp);
			//
			// Prepare a separate threadgroup for running this system.
			//
			ThreadGroup theGroup = new ThreadGroup("Elite");
			//
			// Start the Game Server
			//
			new SocketGameServer(new OneRoundScheduler(rnd), theGroup);
			//
			// And construct the GUI.
			//
			final JFrame f = new JFrame("Masters Of Java");
			f.getContentPane().setLayout(new BorderLayout());
			f.getContentPane().add(clockPanel, BorderLayout.CENTER);
			f.getContentPane().add(rndList, BorderLayout.EAST);
			//
			f.setSize(1024, 768);
			//
			f.setVisible(true);
			//
			// Only allow the program to be terminated when the round is not
			// started or
			// when it is suspended (and then only after confirmation).
			//
			f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent evt) {
					if (clock.isRunning()) {
						f.getToolkit().beep();
					} else {
						if (clock.isFinished() || !clock.isStarted()) {
							if (clock.isFinished())
								rnd.dispose();
							else
								rnd.suspend();
							rnd.logReport();
							Logger.getLogger("").log(Level.INFO, "Terminated by GUI.");
							running = false;
							try {
								Thread.sleep(1500);
							} catch (InterruptedException ex) {
							}
							System.exit(0);
						} else {
							if (JOptionPane.showConfirmDialog(f, "Terminate Round ?", "Sure ?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								rnd.suspend();
								rnd.logReport();
								Logger.getLogger("").log(Level.INFO, "Terminated by GUI.");
								running = false;
								try {
									Thread.sleep(1500);
								} catch (InterruptedException ex) {
								}
								System.exit(0);
							}
						}
					}
				}
			});
			//
			KeyListener kl = new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					if (clock.isRunning()) {
						clock.stop();
					} else {
						clock.start();
					}
				}
			};
			//
			clockPanel.addKeyListener(kl);
			//
			// Refresh Thread for the Clock
			//
			running = true;
			//
			new Thread(new Runnable() {
				public void run() {
					while (running) {
						f.repaint();
						try {
							Thread.sleep(250);
						} catch (Exception ex) {
							//
						}
					}
				}
			}, "Screen Refresh").start();
			//
			// Periodically flushes the state to file.
			//
			new Thread(new Runnable() {
				public void run() {
					while (running) {
						try {
							myState.flush();
						} catch (IOException ex) {
							Logger.getLogger("State-Writer").severe("Error writing to state file : " + ex.getMessage());
						}
						try {
							Thread.sleep(500);
						} catch (Exception ex) {
							//
						}
					}
					try {
						myState.closeFile();
					} catch (IOException ex) {
						Logger.getLogger("State-Writer").severe("Error closing state file : " + ex.getMessage());
					}
				}
			}, "State-Flusher").start();
		} catch (Exception ex) {
			Logger.getLogger("").log(Level.SEVERE, "Terminated with Exception.", ex);
			ex.printStackTrace();
			System.exit(0);
		}
	}

}
