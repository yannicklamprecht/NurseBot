package asylum.nursebot.semantics;

import java.util.LinkedList;
import java.util.List;

import asylum.nursebot.NurseNoakes;
import asylum.nursebot.Sender;
import asylum.nursebot.objects.Permission;
import asylum.nursebot.utils.SecurityChecker;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SemanticsHandler {
	
	private NurseNoakes nurse;
	
	private List<SemanticInterpreter> interpreters;
	
	public SemanticsHandler(NurseNoakes nurse) {
		interpreters = new LinkedList<>();
		
		this.nurse = nurse;
	}
	
	private boolean matchWakeWord(WakeWord wakeWord, String string) {
		String word = wakeWord.getWord();
		
		if (!wakeWord.isCaseSensitive()) {
			string = string.toLowerCase();
			word = word.toLowerCase();
		}
		
		switch (wakeWord.getType()) {
		case ANYWHERE:
			return string.contains(word);
		case BEGINING:
			return string.startsWith(word);
		case END:
			return string.endsWith(word);
		case MIDDLE:
			return string.indexOf(word) > 0;
		case STANDALONE:
			return string.equals(wakeWord.getWord());
		case REGEX:
			return string.matches(wakeWord.getWord());
		case TEXT_MESSAGE:
			return true;
		default:
			System.out.println("Unknown WakeWordPosition.");
			return false;
		}
	}
	
	public void parse(Message message) {
		if (nurse.isChatPaused(message.getChatId()))
			return;
		
		for(SemanticInterpreter interpreter : interpreters) {
			boolean invoke = false;
			for (WakeWord wakeWord : interpreter.getWakeWords()) {
				if (wakeWord.getType() == WakeWordType.ANY_MESSAGE) {
					invoke = true;
					break;
				} else if (wakeWord.getType() == WakeWordType.META) {
					if (message.getLeftChatMember() != null || (message.getNewChatMembers() != null)) {
						invoke = true;
						break;
					}
				} else if (message.hasText() && matchWakeWord(wakeWord, message.getText())) {
					invoke = true;
					break;
				}
			}
			
			if (!invoke)
				continue;
			
			if (!nurse.isActive(interpreter.getModule())) {
				continue;
			}
			
			if (!interpreter.getLocality().check(message.getChat())) {
				continue;
			}
			
			Sender sender = new Sender(message.getChatId(), nurse);
			
			if (interpreter.getPermission() != Permission.ANY) {
				SecurityChecker checker = new SecurityChecker(nurse);
				try {
					if (!checker.checkRights(message.getChatId(), message.getFrom(), interpreter.getPermission())) {
						continue;
					}
				} catch (TelegramApiException e) {
					e.printStackTrace();
					continue;
				}
			}
			
			SemanticContext context = new SemanticContext(message, sender);
			
			try {
				interpreter.getAction().action(context);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void add(SemanticInterpreter interpreter) {
		interpreters.add(interpreter);
	}
	
	public int getNumberOfEntities() {
		return interpreters.size();
	}
}
