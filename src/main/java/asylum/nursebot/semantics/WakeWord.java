package asylum.nursebot.semantics;

public class WakeWord {
	private String word;
	private WakeWordType type;
	private boolean caseSensitive;
	
	public WakeWord(String word, WakeWordType type) {
		this(word, type, true);
	}
	
	public WakeWord(String word, WakeWordType type, boolean caseSensitive) {
		super();
		this.word = word;
		this.type = type;
		this.caseSensitive = caseSensitive;
	}

	public String getWord() {
		return word;
	}

	public WakeWordType getType() {
		return type;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}
}
