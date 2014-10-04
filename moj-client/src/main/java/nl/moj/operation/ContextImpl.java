package nl.moj.operation;

import java.util.HashMap;
import java.util.Map;

import nl.moj.model.Operation;

/**
 * 
 */
public class ContextImpl implements Operation.Context {

	private int idx;
	private Map<String, String> nameContents;

	public ContextImpl() {
		idx = Operation.IDX_EVERYTHING;
		nameContents = new HashMap<>();
	}

	public ContextImpl(int idx) {
		this.idx = idx;
		nameContents = new HashMap<>();
	}

	public ContextImpl(String name, String contents, int idx) {
		this.idx = idx;
		nameContents = new HashMap<>();
		if (name != null)
			nameContents.put(name, contents);
	}

	public ContextImpl(String[] names, String[] contents, int idx) {
		if (names.length != contents.length)
			throw new RuntimeException("name and content lengths do not match.");
		this.idx = idx;
		nameContents = new HashMap<>();
		for (int t = 0; t < names.length; t++) {
			nameContents.put(names[t], contents[t]);
		}
	}

	public int getIndex() {
		return idx;
	}

	public String getContents(String name) {
		return nameContents.get(name);
	}

	public String[] getNames() {
		return nameContents.keySet().toArray(new String[nameContents.size()]);
	}

}
