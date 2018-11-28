package asylum.nursebot.commands;


import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CommandAction {
	void action(CommandContext context) throws TelegramApiException;
}
