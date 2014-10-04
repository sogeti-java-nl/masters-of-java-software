package nl.sogeti.moj.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $1.0
 * @author verduiee (c) 12 aug. 2013, Sogeti B.V. this class is an auxiliary
 *         test class for concurrent server access testing requiring manual
 *         configuration as well as server availability
 */
public class ClientConcurrenceTest {
	private static Logger logger = Logger.getLogger(ClientConcurrenceTest.class);
	/** Client configuration these have to be set manually */
	private String server = "127.0.0.1";
	private int serverPort = 8080;
	private int numberOfTeams = 44;
	private String solutionFilePath = "D:/Eelco/data/cases/2008/PaperScissorsStoneCase.solution";
	private Collection<TestClient> clients = new ArrayList<TestClient>();
	private String codeFileName = "PaperScissorsStoneImpl.java";
	private String code = "public enum PaperScissorsStoneImpl {" + "PAPER, " + "SCISSORS, " + "STONE;"
			+ " public Outcome Battles(PaperScissorsStoneImpl other) {" + "return Outcome.TIE;" + "}" + "}";

	/**
	 * Create a list with clients
	 */
	@Before
	public void init() {
		BasicConfigurator.configure();
		logger.log(Level.INFO, "initializing clients");
		try {
			this.code = readCodeFromFile(solutionFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 1; i < numberOfTeams + 1; i++) {
			String username = Integer.toString(i), password = Integer.toString(i);
			TestClient newClient = new TestClient(server, serverPort, codeFileName, code, username, password);
			clients.add(newClient);
		}

	}

	/** make concurrent connections and send compile commands to server */
	@Test
	public void sendAssignments() {
		logger.log(Level.INFO, "we have as clients: " + clients);
		logger.log(Level.INFO, "test method called");
		for (TestClient TC : clients) {
			logger.log(Level.INFO, "running user: " + TC.toString());
			new Thread(TC).start();
		}
		while (true) {

		}

	}

	/**
	 * reads content from file at path String as String, useful for inputting
	 * .solution files from Case jars
	 */
	private String readCodeFromFile(String path) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			StringBuilder codeString = new StringBuilder();
			String codeLine = null;
			while ((codeLine = br.readLine()) != null) {
				codeString.append(codeLine);
			}
			return codeString.toString();
		}
	}
}