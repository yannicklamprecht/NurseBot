package asylum.nursebot.semantics;


import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface SemanticAction {
	void action(SemanticContext context) throws TelegramApiException;
}
