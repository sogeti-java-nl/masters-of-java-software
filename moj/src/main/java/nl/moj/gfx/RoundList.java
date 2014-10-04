package nl.moj.gfx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import nl.ctrlaltdev.ioc.ApplicationBuilder;
import nl.moj.banner.BannerPanel;
import nl.moj.gfx.ops.AbstractFx;
import nl.moj.gfx.ops.CompilingFx;
import nl.moj.gfx.ops.FinishedFx;
import nl.moj.gfx.ops.SavingFx;
import nl.moj.gfx.ops.SubmittingFx;
import nl.moj.gfx.ops.TestingFx;
import nl.moj.gfx.ops.TypingFx;
import nl.moj.gfx.ops.WaitingFx;
import nl.moj.model.Round;
import nl.moj.model.Team;
import nl.moj.round.TeamImpl;

/**
 *
 */
public class RoundList extends JPanel {

	/**
	 * <code>serialVersionUID</code> indicates/is used for.
	 */
	private static final long serialVersionUID = -3437408452404787422L;

	/** shared by all TeamPanels. */
	public interface VisualEffect {
		public void paint(Team tm, Graphics g, int frame, Component owner);

		public boolean qualifies(Team tm);
	}

	private static final int TEAMWIDTH = 240;
	private static final Color BACKGROUNDCOLOR = new Color(240, 240, 240);
	private static final Color BACKGROUNDCOLOR2 = new Color(224, 224, 224);

	public static final Font normal = new Font("Courier New", Font.BOLD, 16);
	public static final Font small = new Font("Courier New", Font.BOLD, 12);

	public class TeamPanel extends JPanel implements ActionListener {
		/**
		 * <code>serialVersionUID</code> indicates/is used for.
		 */
		private static final long serialVersionUID = 1142897907711674778L;
		//
		private JPopupMenu popup = new JPopupMenu("Options");
		private JMenuItem disqualify = new JMenuItem("Disqualify");
		//
		private Team myTeam;
		private VisualEffect[] myFx;
		private VisualEffect lastFx;
		private int frameCnt;

		//
		public TeamPanel(Team t, VisualEffect[] img) {
			myTeam = t;
			myFx = img;
			this.setPreferredSize(new Dimension(TEAMWIDTH - 8, 42));
			this.setMaximumSize(new Dimension(TEAMWIDTH + 12, 42));
			disqualify.setText(disqualify.getText() + " - " + myTeam.getDisplayName());
			//
			popup.add(disqualify);
			disqualify.addActionListener(this);
			//
			this.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()) {
						if (!popup.isVisible()) {
							disqualify.setEnabled(((TeamImpl) myTeam).canDisqualify());
							popup.show(TeamPanel.this, e.getX(), e.getY());
						}
					}
				}

				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger())
						mousePressed(e);
				}
			});
		}

		public void paint(Graphics g) {
			g.setFont(normal);
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.gray);
			g.drawRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.black);
			g.setFont(normal);
			g.drawString(myTeam.getDisplayName(), 4, 18);
			//
			VisualEffect fx = lastFx;
			if ((fx == null) || (!fx.qualifies(myTeam))) {
				for (int t = 0; t < myFx.length; t++) {
					if (myFx[t].qualifies(myTeam)) {
						frameCnt = 0;
						fx = myFx[t];
						break;
					}
				}
			}
			if (fx != null) {
				//
				g.setFont(small);
				fx.paint(myTeam, g, frameCnt, this);
				//
				frameCnt++;
				//
				lastFx = fx;
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(disqualify)) {
				doDisqualify();
			} else {
				Logger.getLogger("").warning("Spurious action event : " + e);
			}
		}

		private void doDisqualify() {
			int r = JOptionPane
					.showConfirmDialog(this, "Disqualify " + myTeam.getDisplayName() + " ?", "Disqualification", JOptionPane.YES_NO_OPTION);
			if (r == JOptionPane.YES_OPTION) {
				myTeam.disqualify(this);
			}
		}
	}

	private List<Image> imageList = new ArrayList<>();

	public RoundList(Round r, BannerPanel bp, ApplicationBuilder parent) throws ApplicationBuilder.BuildException {
		super(new BorderLayout());
		//
		JPanel tmp = new JPanel(new BorderLayout());
		JPanel tmp1 = new JPanel(new BorderLayout());
		JPanel tmp2 = new JPanel();
		if (r.getAllTeams().length <= 14) {
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
		} else if (r.getAllTeams().length <= 28) {
			tmp2.setLayout(new GridLayout(0, 2));
		} else {
			tmp2.setLayout(new GridLayout(0, 3));
		}
		tmp1.add(tmp2, BorderLayout.NORTH);
		this.add(tmp, BorderLayout.CENTER);
		this.setBackground(BACKGROUNDCOLOR);
		JScrollPane sp = new JScrollPane(tmp1) {
			/**
			 * <code>serialVersionUID</code> indicates/is used for.
			 */
			private static final long serialVersionUID = 3166715483160949627L;

			public boolean isFocusable() {
				return false;
			}
		};
		tmp2.setBackground(BACKGROUNDCOLOR2);
		tmp.add(sp, BorderLayout.CENTER);
		//
		VisualEffect[] fx = getEffects(parent);
		//
		MediaTracker tr = new MediaTracker(this);
		for (int t = 0; t < imageList.size(); t++) {
			tr.addImage(imageList.get(t), 1);
		}
		tr.checkAll(true);
		//
		Team[] tm = r.getAllTeams();
		for (int t = 0; t < tm.length; t++) {
			tmp2.add(new TeamPanel(tm[t], fx));
		}
		//
		bp.setPreferredSize(new Dimension(TEAMWIDTH + 12, 104));
		bp.setBackground(BACKGROUNDCOLOR);
		this.add(bp, BorderLayout.SOUTH);
	}

	/**
	 * loads an Image using the specified name. The root of the classpath can be
	 * specified with a /
	 * 
	 * @param fileName
	 *            the filename and path of the image file (jpg,gif).
	 * @return the image or a dummy image (1,1) if the image cannot be found.
	 */
	public Image loadImage(String fileName) {
		URL myURL = RoundList.class.getResource(fileName);
		if (myURL == null) {
			Logger.getLogger("").warning("Resource " + fileName + " not found. Does it start with / ?");
			return this.createImage(1, 1);
		} else
			try {
				return ImageIO.read(myURL);
			} catch (IOException ex) {
				Logger.getLogger("").warning("Resource " + fileName + " not loaded : " + ex);
				return this.createImage(1, 1);
			}
	}

	public VisualEffect[] getEffects(ApplicationBuilder parent) throws ApplicationBuilder.BuildException {
		List<Class<? extends AbstractFx>> l = new ArrayList<Class<? extends AbstractFx>>();
		l.add(CompilingFx.class);
		l.add(FinishedFx.class);
		l.add(SavingFx.class);
		l.add(SubmittingFx.class);
		l.add(TestingFx.class);
		l.add(TypingFx.class);
		l.add(WaitingFx.class);
		//
		addCustomFxClasses(l);
		//
		ApplicationBuilder ab = new ApplicationBuilder(parent);
		ab.register(RoundList.class, this);
		Class<?>[] src = l.toArray(new Class<?>[l.size()]);
		VisualEffect[] dst = new VisualEffect[l.size()];
		ab.build(src);
		for (int t = 0; t < src.length; t++) {
			dst[t] = (VisualEffect) ab.get(src[t]);
		}
		return dst;
	}

	/**
	 * override to remove existing or add custom Fx _classes_
	 * 
	 * @param l
	 *            a list contaning the VisualEffect classes.
	 */
	protected void addCustomFxClasses(List<Class<? extends AbstractFx>> l) {
		//
		//
		//
	}

}
