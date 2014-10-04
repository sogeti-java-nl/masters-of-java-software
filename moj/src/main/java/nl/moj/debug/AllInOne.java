package nl.moj.debug;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
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
import nl.moj.assignment.MoJEclipsePluginAssignment;
import nl.moj.banner.BannerPanel;
import nl.moj.client.ClientApplet;
import nl.moj.clock.ClockPanel;
import nl.moj.clock.SimpleClock;
import nl.moj.compile.Compiler;
import nl.moj.gamerules.CompetitionGameRules;
import nl.moj.gfx.RoundList;
import nl.moj.mgmt.TeamProfileList;
import nl.moj.model.Assignment;
import nl.moj.model.Clock;
import nl.moj.model.GameRules;
import nl.moj.model.Round;
import nl.moj.model.State;
import nl.moj.model.Team;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool;
import nl.moj.round.DoNothingState;
import nl.moj.round.RoundImpl;
import nl.moj.round.TeamImpl;
import nl.moj.scheduler.OneRoundScheduler;
import nl.moj.security.SandboxSecurityManager;
import nl.moj.server.socket.SocketGameServer;
import nl.moj.sfx.SoundEffects;
import nl.moj.workspace.factory.LocalWorkspaceFactory;
import nl.moj.workspace.factory.WorkspaceFactory;

/**
 * Special MoJ Assignment Eclipse Plugin launcher - launches both the server and
 * client in one VM and loads the assignment from the specified directory. Has a
 * default user and password: 'Default' and 'Welkom'
 * 
 * @author E.Hooijmeijer
 */

public class AllInOne {

	private static State myState;
	private static State.Writer myStateWriter;
	private static boolean running;

	/** creates the teams and their workspaces out of the team.properties file */
	protected static void buildTeams(Round rnd, ApplicationBuilder root, State.Writer wr, SoundEffects sfx) throws IOException {
		String defaultTeam = "" + "TEAMS=DEFAULT\n" + "DEFAULT.NAME=Default\n" + "DEFAULT.PWD=" + TeamProfileList.WELKOM + "\n" + "\n";

		TeamProfileList res = new TeamProfileList(new ByteArrayInputStream(defaultTeam.getBytes()));
		for (int t = 0; t < res.getNumberOfTeams(); t++) {
			String name = res.getTeamName(t);
			String pwd = res.getPassword(t);
			String displayname = res.getDisplayName(t);

			Assignment assignment = (Assignment) root.get(Assignment.class);

			GameRules gameRules = (GameRules) root.get(GameRules.class);

			TeamImpl ti = new TeamImpl(name, displayname, pwd, assignment, gameRules, wr, sfx);
			Workspace ws = workspaceFactory.createWorkspace(name, ti, ti);
			ti.setWorkspace(ws);

			rnd.addTeam(ti);
		}
	}

	/** initialises the logging */
	private static void initLogging() throws IOException {
		SimpleLogFormatter.clearLogConfig();
		SimpleLogFormatter.addConsoleLogging();
		SimpleLogFormatter.verbose();

		Logger.getLogger("").log(Level.INFO, "Starting Masters Of Java");
	}

	private static WorkspaceFactory workspaceFactory;

	/** main method */
	public static void main(String[] args) throws Throwable {
		//
		// Set the MOJ.ECLIPSEPLUGIN property to true signalling the Workspace
		// Classloader
		// to use reverse classloading order for classes in the default package.
		//
		System.setProperty("MOJ.ECLIPSEPLUGIN", Boolean.TRUE.toString());
		//
		// Initialise output redirection.
		//
		OutputRedirector.getSingleton();

		args = Tool.parseArgs(args);

		boolean resumeMode = false;

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
			if (!src.exists())
				throw new IOException("File '" + src + "' does not exist.");
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

			root.build(new Class<?>[] { MoJEclipsePluginAssignment.class });
			//
			// Fetch State data (if any)
			//
			DoNothingState ds = new DoNothingState();
			ds.setTimeRemaining(60 * 30);
			root.register(ds);
			myState = (State) root.get(State.class);
			myStateWriter = (State.Writer) root.get(State.Writer.class);
			//
			// Construct Workspace factory.
			//
			Logger.getLogger("Using LocalWorkspaceFactory.");
			workspaceFactory = new LocalWorkspaceFactory();

			SoundEffects sfx = new SoundEffects();
			final Clock clock = new SimpleClock(30);
			ClockPanel clockPanel = new ClockPanel(clock, (Assignment) root.get(Assignment.class));
			GameRules gameRules = new CompetitionGameRules(clock);
			root.register(clock);
			root.register(gameRules);
			clock.addNotifier(sfx);
			//
			// Build the round.
			//
			root.build(new Class<?>[] { RoundImpl.class });
			final Round rnd = (Round) root.get(Round.class);
			buildTeams(rnd, root, myStateWriter, sfx);
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

			rnd.loadAssignment(resumeMode);

			BannerPanel bp = new BannerPanel(new File("./data/banners/"));
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

			f.setSize(1024, 768);

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
							cleanUp(rnd);
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
								cleanUp(rnd);
								System.exit(0);
							}
						}
					}
				}
			});

			KeyListener kl = new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					if (clock.isRunning()) {
						clock.stop();
					} else {
						clock.start();
					}
				}
			};

			clockPanel.addKeyListener(kl);
			//
			// Refresh Thread for the Clock
			//
			//
			running = true;

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

			clock.start();
		} catch (Exception ex) {
			Logger.getLogger("").log(Level.SEVERE, "Terminated with Exception.", ex);
			ex.printStackTrace();
			System.exit(0);
		}
		//
		// Launch the client (in the same VM)
		//
		args = new String[] { "127.0.0.1" };

		final JFrame f = new JFrame();
		f.setSize(800, 600);
		final ClientApplet ca = new ClientApplet(f, args[0]);
		f.getContentPane().add(ca);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (ca.canClose()) {
					ca.stop();
					ca.destroy();
					f.dispose();
				}
			}
		});
		ca.init();
		ca.setLogin("Default", "Welkom");
		ca.start();
	}

	/** removes the workspaces after termination. */
	private static void cleanUp(Round rnd) {
		Team[] tm = rnd.getAllTeams();
		for (int t = 0; t < tm.length; t++) {
			tm[t].getWorkspace().dispose();
		}
	}

}
