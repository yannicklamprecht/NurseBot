package asylum.nursebot.semantics;


import asylum.nursebot.Sender;
import asylum.nursebot.objects.ActionContext;
import org.telegram.telegrambots.meta.api.objects.Message;

public class SemanticContext extends ActionContext {

	public SemanticContext(Message message, Sender sender) {
		super(message, sender);
	}
	
}
