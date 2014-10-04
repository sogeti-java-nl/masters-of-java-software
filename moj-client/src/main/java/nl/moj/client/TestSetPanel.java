package nl.moj.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import nl.ctrlaltdev.ui.Build;
import nl.moj.client.anim.Anim;
import nl.moj.client.anim.AnimPlayer;

/**
 * 
 */

public class TestSetPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5203625033727160647L;

	private static class TestCase {
		//
		public String label;
		public String description;
		//
		public DefaultListModel<String> console;
		public Anim animation;
		public int result;
		//
		public TestCase(String label,String description) {
			this.label=label;
			this.description=description;
			this.console=new DefaultListModel<String>();
			this.animation=null;
			this.result=0;
		}
		//
		public void reset() {
			console.clear();
			result=0;
			animation=null;
		}
		//
		public String toString() {
			return label;
		}
	}
	
	public class TestCaseTreeRenderer extends DefaultTreeCellRenderer {
			/**
		 * 
		 */
		private static final long serialVersionUID = 1925694178856356409L;
			private Icon okIcon,unknownIcon,failedIcon;

			public TestCaseTreeRenderer(Icon ok,Icon unknown,Icon failed) {
				okIcon=ok;
				unknownIcon=unknown;
				failedIcon=failed;
			}

			public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel,expanded, leaf, row,hasFocus);
				//
				value=((DefaultMutableTreeNode)value).getUserObject();
				//
				if (value instanceof TestCase) {
					switch (((TestCase)value).result) {
						case -1 : setIcon(failedIcon);setToolTipText("Failed.");break;
						case  1 : setIcon(okIcon);setToolTipText("Success.");break;
						default : setIcon(unknownIcon);setToolTipText("Unknown.");break;
					}
				} else if (testCases!=null) {
					int r=1;
					for (int t=0;t<testCases.length;t++) {
						if (testCases[t].result==-1) r=-1;
						if ((testCases[t].result==0)&&(r>0)) r=0;
					}
					//
					switch (r) {
						case -1 : setIcon(failedIcon);setToolTipText("Failed.");break;
						case  1 : setIcon(okIcon);setToolTipText("Success.");break;
						default : setIcon(unknownIcon);setToolTipText("Unknown.");break;
					}
					//
				} else {
					setToolTipText(null);
				}
				//
				return this;
			}
		
	} 
	
	private static final Font MONOSPACEFONT=new Font("Monospaced",Font.PLAIN,11);
	
	private JTree 			testSetTree;
	private JTextArea 		descriptionText;
	private JList<String>	consoleOutput;
	private JLabel    		descriptionLabel;
	private JPanel    		testResultIndicator;
	private JLabel    		outputLabel;
	private AnimPlayer	 	animPlayer;	

	private TestCase[] testCases;
	private TestCase   selected;
	
	public TestSetPanel(Icon ok,Icon unknown,Icon failed) {
		super(new BorderLayout());
		//
		testSetTree=new JTree();
		testSetTree.setCellRenderer(new TestCaseTreeRenderer(ok,unknown,failed));
		//
		descriptionText=new JTextArea();
		descriptionText.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));					
		descriptionText.setEditable(false);	
		descriptionText.setLineWrap(true);		
		descriptionText.setWrapStyleWord(true);
		descriptionText.setFont(MONOSPACEFONT);
		//		
		descriptionLabel=new JLabel();
		descriptionLabel.setPreferredSize(new Dimension(128,24));
		descriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		//
		testResultIndicator=new JPanel();			
		testResultIndicator.setPreferredSize(new Dimension(64,24));
		testResultIndicator.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		testResultIndicator.setBackground(Color.yellow);	
		//
		animPlayer=new AnimPlayer();
		animPlayer.setPreferredSize(new Dimension(200,200));
		//
		consoleOutput=new JList<>();
		consoleOutput.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));	
		//
		outputLabel=new JLabel("Output");
		outputLabel.setPreferredSize(new Dimension(128,24));
		outputLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		//
		JScrollPane treeScrollPane=new JScrollPane(testSetTree);
		treeScrollPane.setPreferredSize(new Dimension(128,256));
		//
		this.add(treeScrollPane,BorderLayout.WEST);
		this.add(new Build.BOXY(new JComponent[] {
			new Build.WCE(			
				new Build.NBOXY(new JComponent[] {
					new Build.RFP(descriptionLabel),
					new Build.RFP(testResultIndicator)
				}),
				new JScrollPane(descriptionText),
				null
			),
			new Build.WCE(
				new Build.NBOXY(new JComponent[] {
					new Build.RFP(outputLabel)
				}),
				new JScrollPane(consoleOutput),
				animPlayer
			)
		}),BorderLayout.CENTER);
		//
		testSetTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener(){
            public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode tn=(DefaultMutableTreeNode)e.getPath().getLastPathComponent();
				for (int t=0;t<testCases.length;t++) {
					if (testCases[t].equals(tn.getUserObject())) {
						selectTestCase(testCases[t]);
					}
				}
            }
		});
	}
	
	public void setTestCases(String[] labels,String[] descriptions) {
		//
		//
		testCases=new TestCase[labels.length];
		DefaultMutableTreeNode  root=new DefaultMutableTreeNode("TestSet");
		for (int t=0;t<labels.length;t++) {
			testCases[t]=new TestCase(labels[t],descriptions[t]);
			root.add(new DefaultMutableTreeNode(testCases[t]));
		}
		DefaultTreeModel dtm=new DefaultTreeModel(root);
		//
		testSetTree.setModel(dtm);	
		//
		testSetTree.setSelectionRow(1);
		//	
	}
	
	public void clearResults() {
		for (int t=0;t<testCases.length;t++) {
			testCases[t].reset();
		}
	}
	
	public void setResults(int[] results) {
		for (int t=0;t<testCases.length;t++) {
			if (results.length!=testCases.length) {
				// Unknown
				testCases[t].result=0;
			} else {
				testCases[t].result=results[t];
			}
		}
		selectTestCase(selected);
	}
	public void setAnimation(int testCase,Anim animation) {
		testCases[testCase].animation=animation;
		if (testCases[testCase].equals(selected)) {
			animPlayer.setAnimation(animation);
		}		
	}
	public void addToConsole(int testCase,String line) {
		testCases[testCase].console.addElement(line);
	}
	
	protected void selectTestCase(TestCase idx) {
		//
		if (idx==null) return;
		//
		descriptionText.setText(idx.description);
		descriptionLabel.setText(idx.label);
		consoleOutput.setModel(idx.console);
		animPlayer.setAnimation(idx.animation);
		//
		switch (idx.result) {
			case -1 : testResultIndicator.setBackground(Color.red);break;
			case 0 : testResultIndicator.setBackground(Color.yellow);break;
			case 1 : testResultIndicator.setBackground(Color.green);break;
		}
		//
		selected=idx;
		//
	}

}
