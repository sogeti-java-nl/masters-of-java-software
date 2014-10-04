package nl.moj.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;
import javax.swing.undo.CannotUndoException;

import nl.ctrlaltdev.ui.Build;
import nl.ctrlaltdev.ui.FocusablePopup;
import nl.ctrlaltdev.ui.ImagePanel;
import nl.ctrlaltdev.util.Encoder;
import nl.moj.client.StacktraceHighlightingCellRenderer.FileNameSource;
import nl.moj.client.anim.Anim;
import nl.moj.client.anim.AnimPlayer;
import nl.moj.client.codecompletion.CodeCompletion;
import nl.moj.client.codecompletion.PartialCode;
import nl.moj.client.io.ActionMessageImpl;
import nl.moj.client.io.ActionMessagesImpl;
import nl.moj.client.io.GoodbyeMessageImpl;
import nl.moj.client.io.HelloMessageImpl;
import nl.moj.client.io.Message;
import nl.moj.client.io.MessageFactory;
import nl.moj.util.InetAddressUtil;

/**
 * Client. One big hack to create a kind of like development environment to do
 * the contest in. Communicates constantly with the server. All actions are
 * performed on the server.
 * 
 * In order to function it needs an IP address specifying the location of the
 * server.
 * 
 * (C) 2004-2007 E.Hooijmeijer/42 B.V. - http://www.2en40.nl and
 * http://www.ctrl-alt-dev.nl
 * 
 * @author E.Hooijmeijer
 */

public class ClientApplet extends JPanel implements ActionListener, Runnable, FileNameSource {
	private static final long serialVersionUID = 8988162323907161870L;
	public static final long MAX_CHARS = 5000 * 120;
	public static final long MAX_LINES = 3000;

	private static final String SAVE = "Save";
	private static final String COMPILE = "Compile";
	private static final String TEST = "Test";

	/**
	 * Player Count Panel : displays the number of signed on players
	 */
	private class PlayerCountPanel extends JPanel {
		private static final long serialVersionUID = -2107327366704378073L;
		private Image myImgOn;
		private Image myImgOff;

		public PlayerCountPanel(Image on, Image off) {
			myImgOn = on;
			myImgOff = off;
			this.setPreferredSize(new Dimension(512, 72));
		}

		public void paint(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());

			int iw = myImgOn.getWidth(null);
			if (iw == 0)
				iw = 32;
			int ih = myImgOn.getHeight(null);
			if (ih == 0)
				ih = 32;
			int md = getWidth() / iw;

			if (remoteStats == null)
				return;

			int rx = (remoteStats.getTeamCount() > md ? md : remoteStats.getTeamCount());
			int cx = getWidth() / 2 - rx * iw / 2;

			for (int t = 0; t < remoteStats.getTeamCount(); t++) {
				if (t < remoteStats.getTeamsOnline()) {
					g.drawImage(myImgOn, cx + (t % md) * iw, t / md * ih, null);
				} else {
					g.drawImage(myImgOff, cx + (t % md) * iw, t / md * ih, null);
				}
			}
		}
	}

	private static final Font BIG = new Font("Verdana", Font.PLAIN, 64);
	private static final Font MEDIUM = new Font("Verdana", Font.PLAIN, 32);
	private static final Font SMALL = new Font("Verdana", Font.PLAIN, 16);
	private static final Font MONOSPACEFONT = new Font("Monospaced", Font.PLAIN, 11);
	private static final Font CODECOMPLETIONFONT = new Font("Monospaced", Font.PLAIN, 10);

	/**
	 * Result panel : Displays results of the test.
	 */
	private class ResultPanel extends JPanel {
		private static final long serialVersionUID = 526450289492918330L;
		private Image win, loose;

		public ResultPanel(Image win, Image loose) {
			this.setPreferredSize(new Dimension(256, 72));
			this.win = win;
			this.loose = loose;
		}

		public void paint(Graphics g) {
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
			if (remoteStats == null)
				return;
			g.setColor(Color.black);
			int score = remoteStats.getFinalScore();
			String sc = String.valueOf(score);
			g.setFont(BIG);
			int w = g.getFontMetrics().stringWidth(sc);
			g.drawString(sc, 160 - w, 64);
			g.setFont(SMALL);
			g.drawString("pts", 164, 64);
			if (score > 0) {
				g.drawImage(win, 200, 32, null);
			} else {
				g.drawImage(loose, 200, 32, null);
			}
		}
	}

	/**
	 * Color's
	 */
	private static final Color HI = new Color(220, 220, 220);
	private static final Color LO = new Color(192, 192, 192);
	private static final Color READONLYEDITOR = new Color(240, 240, 240);
	// private static final Color DARKBLUE = new Color(0,0,64);
	// private static final Color BLUE = new Color(0,32,108);
	private static final Color COLORKEYBOARD = new Color(0x004a5b6c);
	private static final Color DARKGREEN = new Color(0, 32, 0);
	private static final Color NLJUGORANGE = new Color(0x00F1B081);

	class TestPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = -8891384247572164694L;
		private JButton testButton;
		private JPanel testResult;
		private int myIndex;

		public TestPanel(String name, String description, int idx, boolean hi) {
			super(new BorderLayout());
			setBackground(hi ? HI : LO);
			myIndex = idx;
			this.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

			JLabel lbl = new JLabel((idx + 1) + ": " + name, JLabel.RIGHT);
			lbl.setPreferredSize(new Dimension(160, 24));

			testResult = new JPanel();
			testResult.setPreferredSize(new Dimension(64, 24));
			testResult.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			testResult.setBackground(Color.yellow);

			testButton = new JButton(TEST);
			testButton.addActionListener(this);

			JComponent top = new Build.NBOXY(new JComponent[] { new Build.RFP(lbl, getBackground()), new Build.RFP(testResult, getBackground()),
					new Build.RFP(testButton, getBackground()) });

			top.setBackground(getBackground());
			top.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			this.add(top, BorderLayout.WEST);
			//
			JTextArea txt = new JTextArea(description);
			txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			txt.setEditable(false);
			txt.setLineWrap(true);
			txt.setWrapStyleWord(true);
			txt.setFont(MONOSPACEFONT);
			// JScrollPane stxt=new JScrollPane(txt);
			// stxt.setPreferredSize(new Dimension(200,96));
			this.add(txt, BorderLayout.CENTER);
		}

		public void clearResult() {
			testResult.setBackground(Color.yellow);
			this.repaint();
		}

		public void setResult(boolean ok) {
			testResult.setBackground(ok ? Color.green : Color.red);
			this.repaint();
		}

		public void actionPerformed(ActionEvent e) {
			indexedActionPerformed(e, myIndex);
		}

		public int getTestIndex() {
			return myIndex;
		}
	}

	/**
	 * Draws a horizontal bar at the specified height.
	 */
	private static class BarPanel extends JPanel {
		private static final long serialVersionUID = -8352765993516455613L;
		private int y, dy;
		private Color c;

		public BarPanel(int y, int dy, Color c, int w) {
			this(y, dy, c, w, null);
		}

		public BarPanel(int y, int dy, Color c, int w, Color back) {
			this.y = y;
			this.dy = dy;
			this.c = c;
			this.setPreferredSize(new Dimension(w, 64));
			if (back != null)
				setBackground(back);
		}

		public void paint(Graphics g) {
			g.setColor(this.getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(c);
			g.fillRect(0, y, getWidth(), dy);
		}
	}

	/**
	 * Utility mouse listener the show a popup menu.
	 */
	private static class PopupShower extends MouseAdapter {
		private JPopupMenu pm;

		public PopupShower(JPopupMenu pm) {
			this.pm = pm;
		}

		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				pm.show((Component) e.getSource(), e.getX(), e.getY());
				e.consume();
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				pm.show((Component) e.getSource(), e.getX(), e.getY());
				e.consume();
			}
		}
	}

	/**
	 * clears the specified JList when invoked. Assumes DefaultListModel.
	 */
	private static class JListClearListener implements ActionListener {
		private JList<String> list;

		public JListClearListener(JList<String> l) {
			list = l;
		}

		public void actionPerformed(ActionEvent e) {
			DefaultListModel<String> lm = (DefaultListModel<String>) list.getModel();
			lm.clear();
		}
	}

	private class ConsoleClearListener implements ActionListener {
		private JTabbedPane tab;

		public ConsoleClearListener(JTabbedPane tab) {
			this.tab = tab;
		}

		public void actionPerformed(ActionEvent e) {
			clearAllConsoles(tab);
		}
	}

	/**
	 * Editor kit for syntax highlighting.
	 */
	private static EditorKit javaEditorKit = new StyledEditorKit() {
		private static final long serialVersionUID = -5295292838701818000L;

		public Document createDefaultDocument() {
			return new JavaDocument();
		}
	};

	private DividerSyncer theDividerSyncer = new DividerSyncer();

	/** takes care of syncing all dividers */
	private class DividerSyncer implements PropertyChangeListener {
		private boolean changing;

		public void propertyChange(PropertyChangeEvent evt) {
			if (changing)
				return;
			changing = true;
			try {
				Component[] c = consoleTab.getComponents();
				for (int t = 0; t < c.length; t++) {
					if ((c[t] != null) && (c[t] instanceof JSplitPane) && (evt.getSource() != c[t])) {
						((JSplitPane) c[t]).setDividerLocation(((Integer) evt.getNewValue()).intValue());
					}
				}
			} finally {
				changing = false;
			}
		}
	}

	private class IndexActionListener implements ActionListener {
		private int idx;

		public IndexActionListener(int idx) {
			this.idx = idx;
		}

		public void actionPerformed(ActionEvent e) {
			indexedActionPerformed(e, idx);
		}
	}

	/**
	 * loads an Image using the specified name. The root of the classpath can be
	 * specified with a /
	 * 
	 * @param fileName
	 *            the filename and path of the image file (jpg,gif).
	 * @throws IOException
	 *             if the file cannot be found.
	 */
	static Image loadImage(String fileName) throws IOException {
		Toolkit tk = Toolkit.getDefaultToolkit();

		URL myURL = ClientApplet.class.getResource(fileName);
		if (myURL == null)
			throw new IOException("Resource " + fileName + " not found. Does it start with / ?");

		return tk.createImage(myURL);
	}

	private static final String TABCHARS = "    ";
	private static final int TAB_LOGON = 0;
	private static final int TAB_WAIT = 1;
	private static final int TAB_EDIT = 2;
	private static final int TAB_FINISHED = 3;

	private String teamName;
	private String password;
	private boolean loggedIn;
	// Server from the command prompt (default)
	private String server;
	private int serverPort;
	// Server actually selected by the user.
	private String selectedServer;
	private boolean shouldRun;
	private Socket mySocket;
	private DataInput myDataInput;
	private DataOutput myDataOutput;
	private MessageFactory myFactory = new MessageFactory();

	private JTabbedPane editorTab;
	private JTabbedPane consoleTab;
	private JComponent[] myStatsPanel = new JComponent[4];
	private JLabel[] myMessagePanel = new JLabel[4];
	private AssignmentSponsorPanel waitSponsorPanel;
	private AssignmentSponsorPanel finishedSponsorPanel;
	private TestPanel[] myTestPanels;
	private JButton loginButton;
	private Box buttonBar;
	private JTextField userNameField;
	private JTextField passwordField;
	private JComboBox<String> serverCB;
	private JLabel myLineLabel;
	private JLabel myClockLabel;
	private JCheckBox myClearDisplaysBeforeTest = new JCheckBox("Clear Displays");

	private int myActiveTab;
	private Message.UpdateClientStatistics remoteStats;
	private Set<String> knownFileNames = new HashSet<String>();

	private int myKeyStrokes;

	private CodeCompletion myCC;

	private String[] names = new String[] { "/client/duke_hi.jpg", "/client/duke_winner2.jpg", "/client/duke_loser.jpg", "/client/duke_winner1.jpg",
			"/client/mojLogo.gif", "/client/icons/compile_icon.gif", "/client/icons/test_icon.gif", "/client/icons/submit_icon.gif",
			"/client/sogeti.jpg", "/client/keyboard.jpg", "/client/trix-top.jpg", "/client/trix-bottom.jpg", "/client/icons/save_icon.gif",
			"/client/icons/test-success.png", "/client/icons/test-failed.png", "/client/icons/test-unknown.png", };
	private Image[] images;
	private Icon testSuccess;
	private Icon testFailed;
	private Icon testUnknown;
	private JFrame parentFrame;

	/** Args constructor for using in standalone mode */
	public ClientApplet(JFrame parent, String server) {
		this(parent, server, "8080");
	}

	/** Args constructor for using in standalone mode */
	public ClientApplet(JFrame parent, String server, String serverPort) {
		if ((server == null) || (serverPort == null))
			throw new NullPointerException("server parameter(s) missing.");
		this.server = server;
		this.serverPort = Integer.parseInt(serverPort);
		this.parentFrame = parent;
		makeTitle();
	}

	protected void makeTitle() {
		if (teamName == null) {
			parentFrame.setTitle("Masters of Java - Client - (" + Message.PROTOCOLVERSION + ")");
		} else {
			parentFrame.setTitle("Masters of Java - Team '" + teamName + "'");
		}
	}

	public void init() {
		//
		// Set up the Main GUI parts (Structure)
		//
		loadImages();

		this.setLayout(new CardLayout());
		this.add(createLoginPanel(), "-");
		this.add(createWaitPanel(), "0");
		this.add(createEditorPanel(), "1");
		this.add(createFinishedPanel(), "2");

		myCC = new CodeCompletion();
	}

	/**
	 * Loads the images or creates stubs if they fail to load.
	 */
	protected void loadImages() {
		//
		// Image loading
		//
		images = new Image[names.length];
		for (int t = 0; t < images.length; t++)
			try {
				InputStream in = getClass().getResourceAsStream(names[t]);
				if (in == null)
					throw new IOException("Image not found " + names[t]);
				images[t] = ImageIO.read(in);
			} catch (IOException ex) {
				System.out.println("Failed loading image : " + names[t]);
				images[t] = this.createImage(1, 1);
			}
		testSuccess = new ImageIcon(images[13]);
		testFailed = new ImageIcon(images[14]);
		testUnknown = new ImageIcon(images[15]);

		images[8] = images[8].getScaledInstance(140, 32, Image.SCALE_SMOOTH);
	}

	/**
	 * Creates the panel that is displayed while the user waits for the round to
	 * start. It displays a waiting message and the number of users currently
	 * logged in.
	 */
	protected JPanel createWaitPanel() {
		JLabel waitText = new JLabel("Please Wait...");
		waitText.setFont(MEDIUM);
		myStatsPanel[1] = new PlayerCountPanel(images[1], images[2]);
		myMessagePanel[1] = new JLabel("", JLabel.CENTER);
		myMessagePanel[1].setPreferredSize(new Dimension(512, 24));
		waitSponsorPanel = new AssignmentSponsorPanel();
		waitSponsorPanel.setPreferredSize(new Dimension(321, 128));
		Box tmp;

		JPanel p = new Build.NCS(
		// new Build.Spacer(512,128),
				new Build.CFP(waitSponsorPanel), tmp = new Build.BOXY(new JComponent[] { new Build.Spacer(320, 32),
						new Build.CFP(waitText, new ImagePanel(images[0], 32, 32, Color.GRAY)), new Build.CFP(myStatsPanel[1]),
						new Build.CFP(myMessagePanel[1]), }), new Build.CP(null, new BarPanel(24, 191, COLORKEYBOARD, 16, Color.white),
						new ImagePanel(images[9], 799, 191, COLORKEYBOARD), new BarPanel(9, 191, COLORKEYBOARD, 16, Color.white), null));

		p.setBackground(Color.white);
		makeWhite(tmp);

		return p;
	}

	/**
	 * This is the main IDE. This bit just creates the structure. The actual
	 * editors are created on the fly.
	 */
	protected JPanel createEditorPanel() {
		JPanel topLevel = new JPanel(new BorderLayout());

		editorTab = new JTabbedPane();
		consoleTab = new JTabbedPane();
		buttonBar = new Box(BoxLayout.X_AXIS);
		buttonBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

		myLineLabel = new JLabel("Line : 1", JLabel.RIGHT);
		myLineLabel.setPreferredSize(new Dimension(64, 16));

		myClockLabel = new JLabel("29:59", JLabel.RIGHT);
		myClockLabel.setPreferredSize(new Dimension(64, 16));

		myClearDisplaysBeforeTest.setToolTipText("Clear consoles before each test.");
		myClearDisplaysBeforeTest.setSelected(true);

		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		right.add(myClearDisplaysBeforeTest);
		right.add(myClockLabel);
		right.add(myLineLabel);

		JPanel lrPanel = new JPanel(new BorderLayout());
		JPanel controlPage = new JPanel(new BorderLayout());
		controlPage.add(lrPanel, BorderLayout.NORTH);
		lrPanel.add(buttonBar, BorderLayout.CENTER);
		lrPanel.add(right, BorderLayout.EAST);
		controlPage.add(consoleTab, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorTab, controlPage);

		topLevel.add(splitPane, BorderLayout.CENTER);

		addConsoleTab(consoleTab, "Output", true);

		splitPane.setDividerLocation(368);
		splitPane.setResizeWeight(0.75);
		//
		// Add a logo panel for 42
		//
		JPanel tmp = new Build.RFP(new ImagePanel(images[8], 140, 32, Color.white));
		tmp.setBackground(Color.white);
		topLevel.add(tmp, BorderLayout.SOUTH);

		return topLevel;
	}

	/**
	 * creates the panel that displays the final score.
	 */
	protected JPanel createFinishedPanel() {
		JLabel resultLabel = new JLabel("Your Score");
		resultLabel.setFont(MEDIUM);
		ResultPanel score = new ResultPanel(images[3], images[2]);
		myStatsPanel[3] = score;
		myMessagePanel[3] = new JLabel("", JLabel.CENTER);
		myMessagePanel[3].setPreferredSize(new Dimension(512, 24));
		finishedSponsorPanel = new AssignmentSponsorPanel();
		Box tmp;

		JPanel p = new Build.NCS(new ImagePanel(images[10], 800, 128, DARKGREEN), tmp = new Build.BOXY(new JComponent[] {
				new Build.CFP(finishedSponsorPanel), new Build.CFP(resultLabel), new Build.CFP(score), new Build.CFP(myMessagePanel[3]) }),
				new ImagePanel(images[11], 800, 128, DARKGREEN));

		makeWhite(tmp);

		return p;
	}

	/**
	 * Creates the title and login panel.
	 */
	protected JPanel createLoginPanel() {
		userNameField = new JTextField(10);
		passwordField = new JPasswordField(10);
		serverCB = new JComboBox<String>(new String[] { server });
		final JLabel serverCBLabel = new JLabel("Server :  ", JLabel.RIGHT);
		//
		serverCB.setVisible(false);
		serverCBLabel.setVisible(false);
		//
		JLabel advancedLabel = new JLabel("Advanced :  ", JLabel.RIGHT);
		final JCheckBox advanced = new JCheckBox();
		advancedLabel.setLabelFor(advanced);
		advanced.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serverCB.setVisible(advanced.getModel().isSelected());
				serverCBLabel.setVisible(advanced.getModel().isSelected());
			}
		});
		serverCB.setEditable(true);
		loginButton = new JButton("Login");
		//
		JLabel msg = new JLabel("", JLabel.CENTER);
		msg.setPreferredSize(new Dimension(400, 12));
		myMessagePanel[0] = msg;
		//
		JPanel p = new Build.NWCES(new Build.Spacer(32, 32), null, new Build.NBOXY(new JComponent[] {
				new Build.CP(null, new BarPanel(116, 22, NLJUGORANGE, 400), new ImagePanel(images[4], 320, 138, Color.white), new BarPanel(116, 22,
						NLJUGORANGE, 400), null),
				new Build.Spacer(8, 8),
				new Build.Grid(0, 5, new JComponent[] { null, new JLabel("TeamName :  ", JLabel.RIGHT), userNameField, null, null, null,
						new JLabel("Password :  ", JLabel.RIGHT), passwordField, null, null, }),
				new Build.Grid(0, 5, new JComponent[] { null, advancedLabel, advanced, null, null, null, serverCBLabel, serverCB, null, null, null,
						null, loginButton }), new Build.CFP(msg), new JPanel() }), null, new Build.RFP(
				new ImagePanel(images[8], 140, 32, Color.white)));
		//
		makeWhite(p);
		//
		Action logonAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				try {
					if (!loginButton.isEnabled())
						return;
					teamName = userNameField.getText();
					password = Encoder.hash(passwordField.getText());
					selectedServer = server;
					if (advanced.getModel().isSelected()) {
						selectedServer = serverCB.getModel().getSelectedItem().toString();
					}
					//
					userNameField.setEnabled(false);
					passwordField.setEnabled(false);
					loginButton.setEnabled(false);
					serverCB.setEnabled(false);
					//
					makeTitle();
					//
					if (!shouldRun) {
						new Thread(ClientApplet.this).start();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(loginButton, "Error encoding password : " + ex);
				} finally {
					passwordField.setText("");
				}
			}
		};
		//
		loginButton.addActionListener(logonAction);
		loginButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "SUBMIT");
		loginButton.getActionMap().put("SUBMIT", logonAction);
		//
		return p;
	}

	/** paints components white and buttons orange */
	private void makeWhite(JComponent c) {
		Build.visit(c, new Build.Visitor() {
			public void visit(JComponent c) {
				if (c instanceof JButton) {
					c.setBackground(new Color(0x00FAA677));
				} else {
					c.setBackground(Color.white);
				}
			}
		});
	}

	protected JPanel createTestSetPanel(Message.TestSet tst) {
		JPanel p = new JPanel(new BorderLayout());
		JPanel bx = new JPanel(new GridLayout(0, 1));
		BoxLayout bl = new BoxLayout(bx, BoxLayout.Y_AXIS);
		bx.setLayout(bl);
		//
		JScrollPane sp = new JScrollPane(bx);
		sp.getVerticalScrollBar().setUnitIncrement(32);
		p.add(sp, BorderLayout.CENTER);
		//
		int cnt = 0;
		myTestPanels = new TestPanel[tst.getCount()];
		for (int t = 0; t < myTestPanels.length; t++) {
			myTestPanels[t] = new TestPanel(tst.getName(t), tst.getDescription(t), t, cnt % 2 == 0);
			bx.add(myTestPanels[t]);
			cnt++;
		}
		//
		JPanel tmp = new JPanel(new BorderLayout());
		JPanel tmp2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton testAllButton = new JButton("Test All");
		testAllButton.setMnemonic('A');
		testAllButton.setActionCommand(TEST);
		testAllButton.addActionListener(this);
		tmp2.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		tmp2.add(testAllButton);
		tmp.add(tmp2, BorderLayout.SOUTH);
		bx.add(tmp);
		//
		return p;
	}

	protected String translateConsoleTab(int testIdx) {
		return "Test " + (testIdx + 1);
	}

	protected String translateConsoleTab(String name) {
		if ("Output".equals(name))
			return name;
		try {
			return "Test " + (Integer.parseInt(name) + 1);
		} catch (Exception ex) {
			return name;
		}
	}

	protected void updateTestSet(Message.TestSet tst) {
		int idx = editorTab.indexOfTab("Test-Set");
		if (idx < 0) {
			editorTab.addTab("Test-Set", createTestSetPanel(tst));
			idx = editorTab.indexOfTab("Test-Set");
			editorTab.setMnemonicAt(idx, KeyEvent.VK_1 + idx);
			//
			int nr = tst.getCount();
			for (int t = 0; t < nr; t++) {
				addConsoleTab(consoleTab, translateConsoleTab(t), true);
			}
		} else {
			//
			// Not implemented.
			//
		}
	}

	public boolean canClose() {
		return (JOptionPane.showConfirmDialog(this, "Exit Client", "Exit Client : Sure ?", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
	}

	/**
	 * starts the background communication thread if propertly logged on,
	 * otherwise switches to the login panel.
	 */
	public void start() {
		//
		if (canRun()) {
			new Thread(this).start();
		} else {
			setState(0);
		}
		//
		userNameField.requestFocus();
	}

	/**
	 * Signals the communication thread to stop.
	 */
	public void stop() {
		shouldRun = false;
		//
		// Signal goodbye to server if running.
		//
		if (canRun())
			try {
				DataOutput out = getDataOutput();
				new GoodbyeMessageImpl().write(out);
				((DataOutputStream) out).flush();
			} catch (IOException ex) {
				System.out.println(ex);
			}
	}

	public void destroy() {
		//
	}

	public boolean fileNameExists(String name) {
		return knownFileNames.contains(name);
	}

	/**
	 * canRun returns true if a user name and password have been entered.
	 */
	protected boolean canRun() {
		return (teamName != null) && (password != null);
	}

	/**
	 * updates the various panels.
	 */
	protected void updateStats(Message.UpdateClientStatistics msg) {
		int s = myActiveTab;
		remoteStats = msg;
		if (myStatsPanel[s] != null)
			myStatsPanel[s].repaint();
		int[] tst = msg.getTestResults();
		updateTestResults(tst);
		//
		int sec = msg.getSecondsRemaining();
		int min = sec / 60;
		sec = sec % 60;
		//
		// 5 minute alert.
		//
		if (min < 5) {
			myClockLabel.setForeground((sec % 2) == 0 ? Color.RED : Color.black);
		} else {
			myClockLabel.setForeground(Color.black);
		}
		//
		myClockLabel.setText((min < 10 ? "0" + min : "" + min) + ":" + (sec < 10 ? "0" + sec : "" + sec));
	}

	protected void clearTestResults() {
		if (myTestPanels == null)
			return;
		for (int t = 0; t < myTestPanels.length; t++) {
			TestPanel tp = myTestPanels[t];
			consoleTab.setIconAt(t + 1, testUnknown);
			tp.clearResult();
		}
	}

	protected void updateTestResults(int[] r) {
		if (myTestPanels == null)
			return;
		if (r == null)
			return;
		//
		for (int t = 0; t < myTestPanels.length; t++) {
			TestPanel tp = myTestPanels[t];
			int ti = tp.getTestIndex();
			if (ti < r.length) {
				switch (r[ti]) {
				case -1:
					consoleTab.setIconAt(t + 1, testFailed);
					tp.setResult(false);
					break;
				case 0:
					consoleTab.setIconAt(t + 1, testUnknown);
					tp.clearResult();
					break;
				case 1:
					consoleTab.setIconAt(t + 1, testSuccess);
					tp.setResult(true);
					break;
				}
			} else {
				//
				// If the test fails with an exception, an array of length 1
				// is returned. This makes sure that all other test-cases are
				// also set to false.
				//
				consoleTab.setIconAt(t + 1, testFailed);
				tp.setResult(false);

			}
		}
	}

	protected void setAnimation(int test, Anim a) {
		getAnimPlayer(consoleTab, test).setAnimation(a);
	}

	/** adds a message to one of the console's */
	protected void addMessage(String tab, String msg) {
		int s = myActiveTab;
		if (s == TAB_EDIT) {
			// System.out.println(tab+" "+msg);
			int idx = consoleTab.indexOfTab(tab);
			if (idx < 0) {
				JList<String> tmp = addConsoleTab(consoleTab, tab, true);
				((DefaultListModel<String>) tmp.getModel()).addElement(msg);
			} else {
				JList<String> tmp = getConsoleTab(consoleTab, tab);
				DefaultListModel<String> dlm = ((DefaultListModel<String>) tmp.getModel());
				dlm.addElement(msg);
				tmp.setSelectedIndex(dlm.getSize() - 1);
				// No more than 512 lines.
				while (dlm.size() > 512)
					dlm.remove(0);
				final JScrollPane p = getScrollPane(consoleTab, tab);
				// Adjusts the scrollbar to the lowest position.
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						int mx = p.getVerticalScrollBar().getMaximum();
						p.getVerticalScrollBar().setValue(mx);
					}
				});
				//
				if (isImportant(msg) && (idx == 0)) {
					consoleTab.setSelectedComponent(consoleTab.getComponent(idx));
				}
			}
		} else {
			if (myMessagePanel[s] != null) {
				myMessagePanel[s].setText(msg);
			}
		}
	}

	/** returns true if the msg contains the string 'error' */
	private boolean isImportant(String msg) {
		if (msg == null)
			return false;
		return (msg.toLowerCase().indexOf("error") >= 0);
	}

	/** updates the text in an editor or creates a new one if it does not exist. */
	protected void setText(String editor, String text, boolean isJava, boolean readOnly, boolean isMonospaced) {
		int idx = editorTab.indexOfTab(editor);
		if (idx < 0) {
			addSourceEditor(editorTab, editor, isJava, readOnly, isMonospaced, text);
			if ((readOnly) && (isJava))
				try {
					myCC.addStaticSource(text);
				} catch (Exception ex) {
					System.err.println("** Failed parsing : " + editor + " ** : " + ex);
				}
		} else {
			JEditorPane tmp = getSourceEditor(editorTab, editor);
			tmp.setText(text);
		}
	}

	/** adds the named editor to the specified tabbedpane. */
	protected JList<String> addConsoleTab(JTabbedPane tab, String name, boolean readOnly) {
		//
		final JList<String> ta = new JList<>();
		ta.setFont(MONOSPACEFONT);
		ta.setModel(new DefaultListModel<String>());
		JScrollPane sp = new JScrollPane(ta);
		//
		JPopupMenu popup = new JPopupMenu();
		JMenuItem clear = new JMenuItem("clear");
		clear.addActionListener(new JListClearListener(ta));
		JMenuItem clearAll = new JMenuItem("clear All");
		clearAll.addActionListener(new ConsoleClearListener(tab));
		//
		popup.add(clear);
		popup.add(clearAll);
		ta.addMouseListener(new PopupShower(popup));
		//
		if (name.startsWith("Test")) {
			AnimPlayer ap = new AnimPlayer();
			//
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, ap);
			//
			tab.addTab(name, testUnknown, splitPane);
			//
			int idx = tab.indexOfTab(name);
			//
			JMenuItem test = new JMenuItem("run Test-case");
			test.setActionCommand(TEST);
			JMenuItem testAll = new JMenuItem("run all Test-cases");
			testAll.setActionCommand(TEST);
			//
			test.addActionListener(new IndexActionListener(idx - 1));
			testAll.addActionListener(this);
			//
			popup.add(test);
			popup.add(testAll);
			//
			splitPane.setDividerLocation(640);
			splitPane.addPropertyChangeListener("dividerLocation", theDividerSyncer);
			//
			ta.setCellRenderer(new StacktraceHighlightingCellRenderer(this));
			//
			final JMenuItem gotoError = new JMenuItem("goto Error");
			popup.add(gotoError);
			//
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int idx = ta.getSelectedIndex();
					if (idx >= 0) {
						int lnr = StacktraceHighlightingCellRenderer.getErrorLineNr(String.valueOf(ta.getModel().getElementAt(idx)));
						String file = StacktraceHighlightingCellRenderer.getErrorFile(String.valueOf(ta.getModel().getElementAt(idx)),
								ClientApplet.this);
						if ((lnr >= 0) && (file != null)) {
							JEditorPane tmp = getSourceEditor(editorTab, file);
							if (tmp != null) {
								scrollToLine(tmp, lnr);
								editorTab.setSelectedComponent(tmp.getParent().getParent());
							}
						}
					}
				}
			};
			//
			gotoError.addActionListener(al);
			ta.addMouseListener(new DoubleClickListener(al));
			//
			popup.addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					int idx = ta.getSelectedIndex();
					if (idx >= 0) {
						int lnr = StacktraceHighlightingCellRenderer.getErrorLineNr(String.valueOf(ta.getModel().getElementAt(idx)));
						String file = StacktraceHighlightingCellRenderer.getErrorFile(String.valueOf(ta.getModel().getElementAt(idx)),
								ClientApplet.this);
						gotoError.setEnabled(lnr >= 0 && file != null);
					} else {
						gotoError.setEnabled(false);
					}
				}

				public void popupMenuCanceled(PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}
			});
		} else {
			tab.addTab(name, sp);
			//
			ta.setCellRenderer(new ErrorHighlightingCellRenderer());
			//
			final JMenuItem gotoError = new JMenuItem("goto Error");
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int idx = ta.getSelectedIndex();
					if (idx >= 0) {
						int lnr = ErrorHighlightingCellRenderer.getErrorLineNr(String.valueOf(ta.getModel().getElementAt(idx)));
						String file = ErrorHighlightingCellRenderer.getErrorFile(String.valueOf(ta.getModel().getElementAt(idx)));
						if ((lnr >= 0) && (file != null)) {
							JEditorPane tmp = getSourceEditor(editorTab, file);
							if (tmp != null) {
								scrollToLine(tmp, lnr);
								editorTab.setSelectedComponent(tmp.getParent().getParent());
							}
						}
					}
				}
			};
			//
			gotoError.addActionListener(al);
			ta.addMouseListener(new DoubleClickListener(al));
			popup.add(gotoError);
			//
			popup.addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					int idx = ta.getSelectedIndex();
					if (idx >= 0) {
						int lnr = ErrorHighlightingCellRenderer.getErrorLineNr(String.valueOf(ta.getModel().getElementAt(idx)));
						String file = ErrorHighlightingCellRenderer.getErrorFile(String.valueOf(ta.getModel().getElementAt(idx)));
						gotoError.setEnabled(lnr >= 0 && file != null);
					} else {
						gotoError.setEnabled(false);
					}
				}

				public void popupMenuCanceled(PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}
			});
		}
		//
		return ta;
	}

	protected void scrollToLine(JEditorPane pane, int line) {
		int pos = -1;
		String txt = pane.getText();
		for (int t = 0; t < txt.length(); t++) {
			if (txt.charAt(t) == '\n') {
				line--;
				if (line == 0) {
					pos = t;
				}
			}
		}
		if (pos != -1) {
			Caret caret = pane.getCaret();
			caret.setDot(pos);
			pane.requestFocus();
		}
	}

	/** returns the named tab editor */
	@SuppressWarnings("unchecked")
	protected JList<String> getConsoleTab(JTabbedPane tab, String name) {
		int idx = tab.indexOfTab(name);
		JComponent c = (JComponent) tab.getComponentAt(idx);
		if (idx == 0) {
			Component result = ((JScrollPane) c).getViewport().getComponents()[0];
			return (JList<String>) result;
		} else {
			JScrollPane sp = (JScrollPane) c.getComponent(0);
			return (JList<String>) sp.getViewport().getComponents()[0];
		}
	}

	/** clears all consoles of their messages. */
	@SuppressWarnings("unchecked")
	protected void clearAllConsoles(JTabbedPane tab) {
		for (int t = 0; t < tab.getComponentCount(); t++) {
			JList<String> lst = null;
			JComponent c = (JComponent) tab.getComponentAt(t);
			if (t == 0) {
				lst = (JList<String>) ((JScrollPane) c).getViewport().getComponents()[0];
			} else {
				JScrollPane sp = (JScrollPane) c.getComponent(0);
				lst = (JList<String>) sp.getViewport().getComponents()[0];
			}
			//
			if (lst != null) {
				DefaultListModel<String> lm = (DefaultListModel<String>) lst.getModel();
				lm.clear();
			}
		}
	}

	protected JScrollPane getScrollPane(JTabbedPane tab, String name) {
		int idx = tab.indexOfTab(name);
		JComponent c = (JComponent) tab.getComponentAt(idx);
		if (idx == 0)
			return (JScrollPane) c;
		else
			return (JScrollPane) c.getComponent(0);
	}

	protected AnimPlayer getAnimPlayer(JTabbedPane tab, int testIdx) {
		int idx = testIdx + 1;
		JComponent c = (JComponent) tab.getComponentAt(idx);
		AnimPlayer ap = (AnimPlayer) c.getComponent(1);
		return ap;
	}

	/** adds a source code editor */
	protected JEditorPane addSourceEditor(JTabbedPane tab, String name, boolean isJava, boolean readOnly, boolean isMono, String text) {
		knownFileNames.add(name);
		final JEditorPane editor = new JEditorPane();
		editor.setEditorKitForContentType("text/java", javaEditorKit);
		if (isJava)
			editor.setContentType("text/java");
		editor.setEditable(!readOnly);
		if (readOnly)
			editor.setBackground(READONLYEDITOR);
		//
		editor.getActionMap().put(DefaultEditorKit.insertTabAction, new InsertTabAction(TABCHARS));
		editor.getActionMap().put(DefaultEditorKit.nextWordAction, new JavaWordAction(DefaultEditorKit.nextWordAction, false, true));
		editor.getActionMap().put(DefaultEditorKit.selectionNextWordAction, new JavaWordAction(DefaultEditorKit.selectionNextWordAction, true, true));
		editor.getActionMap().put(DefaultEditorKit.previousWordAction, new JavaWordAction(DefaultEditorKit.previousWordAction, false, false));
		editor.getActionMap().put(DefaultEditorKit.selectionPreviousWordAction,
				new JavaWordAction(DefaultEditorKit.selectionPreviousWordAction, true, false));
		//
		JScrollPane sp = new JScrollPane(editor);
		tab.addTab(name, sp);
		int idx = editorTab.indexOfTab(name);
		editorTab.setMnemonicAt(idx, KeyEvent.VK_1 + idx);
		//
		if (isMono) {
			editor.setFont(MONOSPACEFONT);
		}
		//
		editor.setText(text);
		editor.setCaretPosition(0);
		//
		// Add a line number counter.
		//
		if (!readOnly) {
			final JavaUndoManager um = new JavaUndoManager();
			//
			editor.getDocument().addUndoableEditListener(new UndoableEditListener() {
				public void undoableEditHappened(UndoableEditEvent e) {
					um.addEdit(e.getEdit());
				}
			});
			//
			editor.addCaretListener(new CaretListener() {
				public void caretUpdate(CaretEvent e) {
					JavaDocument jd = ((JavaDocument) editor.getDocument());
					myLineLabel.setText("Line : " + String.valueOf(jd.getLineNumber(e.getDot()) + 1));
				}
			});
			//
			editor.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					keyStroke();
					if ((e.getKeyCode() == KeyEvent.VK_SPACE) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
						autoCompleteTrigger(editor);
					}
					if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
						try {
							//
							if (um.canUndo())
								um.undo();
							while (um.canUndo() && !(um.isInsertOrDelete()))
								um.undo();
							//
						} catch (CannotUndoException ex) {
							Toolkit.getDefaultToolkit().beep();
						}
					}
				}
			});
		}
		//
		return editor;
	}

	private void autoCompleteTrigger(final JEditorPane editor) {
		// Make a copy of the point (!)
		Point p = new Point(editor.getCaret().getMagicCaretPosition());
		//
		// Determine popup position on screen.
		//
		Container c = editor;
		while ((c != null) && (!(c instanceof Window))) {
			p.x = p.x + c.getLocation().x;
			p.y = p.y + c.getLocation().y;
			c = c.getParent();
		}
		//
		// Analyse the current line.
		//
		int len = 0;
		int pos = editor.getCaretPosition();
		String txt = "";
		while (pos > 0)
			try {
				pos--;
				len++;
				txt = editor.getDocument().getText(pos, len);
				if (txt.charAt(0) == '\n') {
					txt = editor.getDocument().getText(pos + 1, len - 1);
					break;
				}
			} catch (BadLocationException ex) {
				txt = "";
			}
		//
		txt = txt.trim();
		final PartialCode base = myCC.getPartToComplete(txt);
		final JList<String> cmp = new JList<>(myCC.getCompletions(editor.getText(), editor.getCaretPosition(), base));
		cmp.setFont(CODECOMPLETIONFONT);
		//
		cmp.setBorder(BorderFactory.createEtchedBorder());
		JScrollPane sp = new JScrollPane(cmp);
		sp.setPreferredSize(new Dimension(256, 96));
		final FocusablePopup pop = FocusablePopup.getPopup(editor, sp, p.x, p.y);
		cmp.requestFocus();
		cmp.setSelectedIndex(0);
		cmp.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				pop.closePopup();
			}
		});
		//
		cmp.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					try {
						if ((cmp.getSelectedIndex() >= 0) && (cmp.getModel().getSize() != 0)) {
							String sel = cmp.getSelectedValue().toString();
							if (sel.length() >= base.length()) {
								editor.getDocument().insertString(editor.getCaretPosition(), sel.substring(base.length()), null);
							}
						}
						pop.closePopup();
						editor.requestFocus();
					} catch (BadLocationException ex) {
						// Ignore
					}
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					pop.closePopup();
					editor.requestFocus();
				}
			}
		});
		//
		cmp.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					if ((cmp.getSelectedIndex() >= 0) && (cmp.getModel().getSize() != 0)) {
						editor.getDocument()
								.insertString(editor.getCaretPosition(), cmp.getSelectedValue().toString().substring(base.length()), null);
					}
					pop.closePopup();
					editor.requestFocus();
				} catch (BadLocationException ex) {
					// Ignore
				}
			}
		});
	}

	private synchronized void keyStroke() {
		myKeyStrokes++;
	}

	private synchronized int getKeyStrokes() {
		int r = myKeyStrokes;
		myKeyStrokes = 0;
		return r;
	}

	/** returns the named source code editor */
	protected JEditorPane getSourceEditor(JTabbedPane tab, String name) {
		int idx = tab.indexOfTab(name);
		if (idx < 0)
			return null;
		JScrollPane sp = (JScrollPane) tab.getComponentAt(idx);
		return (JEditorPane) sp.getViewport().getComponents()[0];
	}

	/** adds an operation button (compile,test etc) */
	protected void addOperation(String label, String tooltip, boolean confirm) {
		final JButton btn = new JButton(label);
		if (COMPILE.equalsIgnoreCase(label)) {
			btn.setMnemonic('C');
			btn.setIcon(new ImageIcon(images[5]));
		}
		if (TEST.equalsIgnoreCase(label)) {
			btn.setMnemonic('T');
			btn.setIcon(new ImageIcon(images[6]));
		}
		if ("Submit".equalsIgnoreCase(label)) {
			btn.setMnemonic('u');
			btn.setIcon(new ImageIcon(images[7]));
		}
		if ("Save".equalsIgnoreCase(label)) {
			btn.setActionCommand(label);
			btn.setMnemonic('S');
			btn.setIcon(new ImageIcon(images[12]));
			//
			// Add CTRL-S as keybinding for save by populair demand.
			//
			KeyStroke ks = KeyStroke.getKeyStroke("ctrl S");
			btn.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ks, btn.getActionCommand());
			btn.getActionMap().put(btn.getActionCommand(), new AbstractAction() {
				private static final long serialVersionUID = 3420329536006550241L;

				public void actionPerformed(ActionEvent e) {
					e = new ActionEvent(btn, ActionEvent.ACTION_PERFORMED, btn.getActionCommand());
					ClientApplet.this.actionPerformed(e);
				}
			});
		}
		btn.setToolTipText(tooltip);
		buttonBar.add(btn);
		buttonBar.add(Box.createHorizontalStrut(8));
		if (confirm) {
			btn.addActionListener(new ConfirmingActionListener(btn, this));
		} else {
			btn.addActionListener(this);
		}
	}

	/** changes the state of the applet */
	protected void setState(int newState) {
		if ((teamName == null) || (password == null) || (!loggedIn)) {
			myActiveTab = TAB_LOGON;
			((CardLayout) this.getLayout()).show(this, "-");
		} else {
			if (myActiveTab == newState + 1)
				return;
			switch (newState) {
			case Message.UpdateClientStatistics.STATE_WAIT:
				myActiveTab = TAB_WAIT;
				break;
			case Message.UpdateClientStatistics.STATE_PROGRAMMING:
				myActiveTab = TAB_EDIT;
				break;
			case Message.UpdateClientStatistics.STATE_FINISHED:
				myActiveTab = TAB_FINISHED;
				break;
			default:
				myActiveTab = TAB_LOGON;
				break;
			}
			//
			((CardLayout) this.getLayout()).show(this, String.valueOf(myActiveTab - 1));
		}
	}

	/** propagates the activated action to the server */
	public void actionPerformed(ActionEvent e) {
		indexedActionPerformed(e, -1);
	}

	protected void indexedActionPerformed(ActionEvent e, int idx) {
		ActionMessagesImpl actionMessages = null;
		
		String file = null;
		String data = null;
		int cnt = editorTab.getTabCount();
		for (int t = 0; t < cnt; t++) {
			Component c = editorTab.getComponent(t);
			if (c instanceof JScrollPane) {
				JScrollPane sp = (JScrollPane) c;
				JEditorPane p = (JEditorPane) sp.getViewport().getComponents()[0];
				if (p.isEditable()) {
					file = editorTab.getTitleAt(t);
					data = p.getText();
					
					if(actionMessages == null) {
						actionMessages = new ActionMessagesImpl(file, data, e.getActionCommand(), idx, getKeyStrokes());
					} else {
						actionMessages.addActionMessage(new ActionMessageImpl(file, data, e.getActionCommand(), idx, getKeyStrokes()));
					}
				}
			}
		}
		//
		if (COMPILE.equals(e.getActionCommand())) {
			consoleTab.setSelectedIndex(0);
		}
		//
		if (TEST.equals(e.getActionCommand())) {
			clearTestResults();
			if (myClearDisplaysBeforeTest.isSelected()) {
				clearAllConsoles(consoleTab);
			}
		}
		//
		try {
			if ((file == null) || (data == null) || (actionMessages == null))
				throw new IOException("No editable source.");
			//
			DataOutput out = getDataOutput();
			actionMessages.write(out);
			((DataOutputStream) out).flush();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Action Failed. Please try again.\nReason : " + ex.getMessage());
		}
	}

	public void setLogin(String username, String password) {
		userNameField.setText(username);
		passwordField.setText(password);
	}

	protected void forceLogout(String reason) {
		editorTab.removeAll();
		consoleTab.removeAll();
		knownFileNames.clear();
		// First is always output.
		addConsoleTab(consoleTab, "Output", true);
		//
		buttonBar.removeAll();
		teamName = null;
		password = null;
		loggedIn = false;
		setState(TAB_LOGON);
		addMessage("Output", reason);
		userNameField.setEnabled(true);
		passwordField.setEnabled(true);
		loginButton.setEnabled(true);
		serverCB.setEnabled(true);
		//
		userNameField.requestFocus();
		//
		makeTitle();
	}

	/**
	 * creates a new connection with the server and sends the logon message.
	 */
	protected synchronized void newConnection() throws IOException {
		//
		// Open Connection.
		//
		mySocket = new Socket(InetAddressUtil.makeInetAddress(selectedServer), serverPort);
		// System.out.println("Connecting to "+selectedServer);
		//
		myDataInput = new DataInputStream(mySocket.getInputStream());
		myDataOutput = new DataOutputStream(mySocket.getOutputStream());
		//
		// SignIn.
		//
		new HelloMessageImpl(teamName, password).write(myDataOutput);
	}

	/**
	 * returns the data input from the socket. If it does not exist, a new
	 * connection is made.
	 */
	protected synchronized DataInput getDataInput() throws IOException {
		if ((mySocket == null) || (!mySocket.isConnected())) {
			newConnection();
		}
		return myDataInput;
	}

	/**
	 * returns the data output from the socket. If it does not exist, a new
	 * connection is made.
	 */
	protected synchronized DataOutput getDataOutput() throws IOException {
		if ((mySocket == null) || (!mySocket.isConnected())) {
			newConnection();
		}
		return myDataOutput;
	}

	/**
	 * Main message loop : reads incoming messages from the server and delegates
	 * them to the UI.
	 */
	public void run() {
		shouldRun = true;
		//
		long consoleLineCounter = 0;
		long consoleCharCounter = 0;
		long lastReset = System.currentTimeMillis();
		//
		try {
			while (shouldRun) {
				try {
					//
					// Stop if the username/password is not present.
					//
					if (!canRun()) {
						shouldRun = false;
						return;
					}
					//
					DataInput in = getDataInput();
					Message msg = myFactory.createMessage(in);
					//
					if (System.currentTimeMillis() - lastReset > 10000L) {
						consoleLineCounter = 0;
						consoleCharCounter = 0;
						lastReset = System.currentTimeMillis();
						// System.out.println("Reset");
					} else {
						// System.out.println(consoleLineCounter+" "+consoleCharCounter);
					}
					//
					switch (msg.getType()) {
					/** a message for the console tabs */
					case Message.MSG_CONSOLE:
						if (consoleCharCounter > MAX_CHARS)
							break;
						if (consoleLineCounter++ > MAX_LINES)
							break;
						final Message.Console con = (Message.Console) msg;
						consoleCharCounter += con.getContent().length() + 1;
						//
						if ((consoleCharCounter < MAX_CHARS) && (consoleLineCounter < MAX_LINES)) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									addMessage(translateConsoleTab(con.getConsole()), con.getContent());
								}
							});
						} else {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									Toolkit.getDefaultToolkit().beep();
									JOptionPane.showMessageDialog(ClientApplet.this, "Your program generated more than " + MAX_CHARS
											+ " chars and/or " + MAX_LINES
											+ " lines in 10 Seconds.\nOutput will be temporarily suspended and automatically resumed after 15 sec.",
											"Warning", JOptionPane.WARNING_MESSAGE);
								}
							});
						}
						break;
					/** a message for the editor tabs */
					case Message.MSG_EDITOR:
						final Message.Editor ed = (Message.Editor) msg;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								setText(ed.getFileName(), ed.getContents(), ed.isJava(), ed.isReadOnly(), ed.isMonospaced());
							}
						});
						break;
					/** a message for adding the action buttons */
					case Message.MSG_ADDACTION:
						final Message.AddAction add = (Message.AddAction) msg;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								addOperation(add.getAction(), add.getToolTip(), add.mustConfirm());
							}
						});
						break;
					/** a message containing client statistics */
					case Message.MSG_UPDATE_CLIENT:
						final Message.UpdateClientStatistics upd = (Message.UpdateClientStatistics) msg;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								setState(upd.getState());
								updateStats(upd);
							}
						});
						break;
					case Message.MSG_TESTSET:
						final Message.TestSet tstset = (Message.TestSet) msg;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								updateTestSet(tstset);
							}
						});
						break;
					/** signalling that the authentication failed */
					case Message.MSG_UNKNOWNUSERPASSWORD:
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								forceLogout("Invalid username/password.");
							}
						});
						shouldRun = false;
						return;
						/** using an old version of the client */
					case Message.MSG_PROTOCOLVERSIONMISMATCH:
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								forceLogout("Protocol version mismatch - update your client.");
							}
						});
						shouldRun = false;
						return;
						/** animation test result */
					case Message.MSG_ANIMATION:
						final Message.Animation animMsg = (Message.Animation) msg;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								setAnimation(animMsg.getTest(), animMsg.getAnimation());
							}
						});
						break;
					case Message.MSG_ASSIGNMENT:
						final Message.Assignment assignmentMsg = (Message.Assignment) msg;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								waitSponsorPanel.setAssignmentData(assignmentMsg);
								finishedSponsorPanel.setAssignmentData(assignmentMsg);
							}
						});
						break;
					/** all other messages : close the connection */
					default:
						throw new IOException("UnExcpected Message : " + msg.getType());
					}
					//
					// If we get here, we must be properly signed on.
					//
					loggedIn = true;
					//
				} catch (final IOException ex) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							forceLogout("Connection Lost (" + ex.getClass().getName() + " : " + ex.getMessage() + ")");
						}
					});
					try {
						Thread.sleep(2000);
					} catch (Exception x) {
					}
				} catch (final Exception ex) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							addMessage("Output", ex.getClass().getName() + " : " + ex.getMessage());
						}
					});
					try {
						Thread.sleep(2000);
					} catch (Exception x) {
					}
				}
			}
			//
			// Close the socket when done.
			//
		} finally {
			try {
				if ((mySocket != null) && (mySocket.isConnected())) {
					mySocket.close();
				}
				mySocket = null;
			} catch (IOException ex) {
				// Ignore.
			}
		}
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			if (args.length == 1) {
				args = new String[] { args[0], "8080" };
			} else {
				System.out.println("Usage : ClientApplet <server ip address> <port>");
				args = new String[] { "127.0.0.1", "8080" };
			}
		}
		//
		// Install Security manager to block system clipboard ccp operations.
		//
		/* if (!args[0].equals("127.0.0.1")) { */
		System.setSecurityManager(new SecurityManager() {
			public void checkPermission(Permission perm) {
			}

			public void checkPermission(Permission perm, Object context) {
			}

			public void checkSystemClipboardAccess() {
				throw new SecurityException();
			}
		});
		/* } */
		//
		JFrame f = new JFrame();
		f.setSize(800, 600);
		final ClientApplet ca = new ClientApplet(f, args[0], args[1]);
		f.getContentPane().add(ca);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (ca.canClose()) {
					ca.stop();
					ca.destroy();
					System.exit(0);
				}
			}
		});
		ca.init();
		ca.start();
	}

}