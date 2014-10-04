package nl.moj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nl.ctrlaltdev.ioc.ApplicationBuilder;
import nl.ctrlaltdev.util.Tool;
import nl.moj.assignment.JarFileAssignment;
import nl.moj.model.Assignment;
import nl.moj.model.Operation;
import nl.moj.model.Tester;
import nl.moj.operation.Test;
import nl.moj.process.ProcessPool;
import nl.moj.security.SandboxSecurityManager;

/**
 * Quick n dirty description dumper. Very useful if you want to hand out paper
 * copies of the case-descriptions and testcases.
 */

public class CaseDumper {

	private static void head(BufferedWriter out, String title) throws IOException {
		out.write("<HTML><HEAD><TITLE>" + title + "</TITLE></HEAD><BODY style=\"font-size:11px\"><PRE>");
	}

	private static void tail(BufferedWriter out) throws IOException {
		out.write("</PRE></BODY></HTML>");
	}

	private static void dump(InputStream in, BufferedWriter out) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String current = br.readLine();
		//
		while (current != null) {
			current = current.replace('\t', ' ');
			if (current.length() > 0) {
				if ((Character.isDigit(current.charAt(0)))) {
					current = "<B>" + current + "</B>";
				} else if ((!Character.isDigit(current.charAt(0))) && (current.charAt(0) != ' ') && (current.charAt(0) != '\t')) {
					current = "  " + current;
				}
			}
			dump(current, out);
			//
			current = br.readLine();
			//
		}
	}

	private static void dump(String s, BufferedWriter out) throws IOException {
		out.write(s);
		out.write("\n");
	}

	private static void render(String[] args) throws Throwable {
		//
		// Read round.properties
		//
		File src = new File(args[0]);
		if (!src.exists())
			throw new IOException("File : " + src + " does not exist.");

		ApplicationBuilder root = new ApplicationBuilder();
		root.register(root);
		root.register(ProcessPool.class, new ProcessPool(16));
		root.register(File.class, src);

		ThreadGroup tmp = new ThreadGroup("Tester-ThreadGroup");
		SandboxSecurityManager ssm = new SandboxSecurityManager(tmp);
		System.setSecurityManager(ssm);

		Assignment round = null;

		if (src.getName().endsWith(".jar")) {
			root.build(new Class<?>[] { JarFileAssignment.class });
		} else
			throw new RuntimeException("Unsupported assignment type.");

		round = (Assignment) root.get(Assignment.class);

		BufferedWriter out = new BufferedWriter(new FileWriter(new File(args[1])));

		try {
			head(out, round.getName());
			out.write("<CENTER><H2>" + round.getDisplayName() + "</H2></CENTER>");
			out.write("<CENTER><H3>By " + (round.getAuthor() == null ? "Unknown" : round.getAuthor()) + "</H3></CENTER>");

			String[] d = round.getDescriptionFileNames();
			for (int t = 0; t < d.length; t++) {
				InputStream in = round.getAssignmentFileData(d[t]);
				try {
					dump(in, out);
					dump("", out);
					dump("<HR>", out);
				} finally {
					in.close();
				}
			}

			Operation[] op = round.getOperations();
			for (int t = 0; t < op.length; t++) {
				if (op[t] instanceof Test) {
					Test tst = (Test) op[t];
					Tester tester = tst.getTester();
					String[] names = tester.getTestNames();
					String[] descr = tester.getTestDescriptions();

					for (int y = 0; y < names.length; y++) {
						dump("", out);
						dump("<B>Test case " + (y + 1) + " : " + names[y] + "</B>", out);
						dump(descr[y], out);
					}
				}
			}

			dump("", out);
			dump("<HR>", out);
			tail(out);
		} finally {
			out.close();
		}
	}

	public static void main(String[] args) throws Throwable {
		args = Tool.parseArgs(args);

		if ((args.length != 2) && (args.length != 3)) {
			System.out.println("Dumps the case description and test-cases into a HTML file.");
			System.out.println("Usage : CaseDumper [case.jar] [targetfile.html]");
			System.out.println("        CaseDumper [source directory] [target directory] scan");
			System.exit(0);
		}

		if (args.length == 3) {
			//
			// Scans a dir for .properties and .jar files
			//
			if (!args[2].equalsIgnoreCase("scan")) {
				System.out.println("Unknown option '" + args[2] + "'");
				System.exit(0);
			}

			File f = new File(args[0]);
			if (!f.isDirectory()) {
				System.out.println(f + " is not a directory.");
				System.exit(0);
			}

			File[] src = f.listFiles(new FileFilter() {
				public boolean accept(File f) {
					boolean ok = f.isFile() && f.getName().endsWith(".properties");
					return ((ok) || (f.isFile() && f.getName().endsWith(".jar")));
				}
			});
			//
			// And renders the descriptions for it.
			//
			for (int t = 0; t < src.length; t++)
				try {
					System.out.println("Now rendering description for : " + src[t]);
					render(new String[] { src[t].getAbsolutePath(),
							args[1] + "/" + src[t].getName().substring(0, src[t].getName().lastIndexOf(".")) + ".html" });
				} catch (Exception ex) {
					System.err.println("Failed : " + ex.getMessage());
				}
		} else
			render(args);
	}

}
