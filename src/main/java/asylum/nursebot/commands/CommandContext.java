package asylum.nursebot.commands;


import asylum.nursebot.Sender;
import asylum.nursebot.objects.ActionContext;
import org.telegram.telegrambots.meta.api.objects.Message;

public class CommandContext extends ActionContext {

	private String parameter;
	
	public String getParameter() {
		return parameter;
	}
	
	public CommandContext(Message message, Sender sender, String parameter) {
		super(message, sender);
		this.parameter = parameter;
	}
}
