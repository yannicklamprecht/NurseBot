package asylum.nursebot.semantics;

import java.util.LinkedList;
import java.util.List;

import asylum.nursebot.objects.Locality;
import asylum.nursebot.objects.Module;
import asylum.nursebot.objects.Permission;

public class SemanticInterpreter {
	private List<WakeWord> wakeWords;
	private Permission permission;
	private Locality locality;
	private Module module;
	private SemanticAction action = c -> System.out.println("Action not defined.");

	public SemanticInterpreter(Module module) {
		wakeWords = new LinkedList<>();
		this.module = module;
	}
	
	public List<WakeWord> getWakeWords() {
		return wakeWords;
	}

	public Permission getPermission() {
		return permission;
	}

	public Locality getLocality() {
		return locality;
	}
	
	public Module getModule() {
		return module;
	}

	public SemanticAction getAction() {
		return action;
	}

	public SemanticInterpreter setWakeWords(List<WakeWord> wakeWords) {
		this.wakeWords = wakeWords;
		return this;
	}

	public SemanticInterpreter addWakeWord(WakeWord wakeWord) {
		this.wakeWords.add(wakeWord);
		return this;
	}
	
	public SemanticInterpreter addWakeWords(List<WakeWord> wakeWords) {
		this.wakeWords.addAll(wakeWords);
		return this;
	}
	
	public SemanticInterpreter setPermission(Permission permission) {
		this.permission = permission;
		return this;
	}

	public SemanticInterpreter setLocality(Locality locality) {
		this.locality = locality;
		return this;
	}

	public SemanticInterpreter setAction(SemanticAction action) {
		this.action = action;
		return this;
	}
	
	
}
