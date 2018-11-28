package asylum.nursebot.objects;


import asylum.nursebot.Sender;
import org.telegram.telegrambots.meta.api.objects.Message;

public abstract class ActionContext {
	private Message message;
	private Sender sender;
	
	public Message getMessage() {
		return message;
	}
	
	public Sender getSender() {
		return sender;
	}

	public ActionContext(Message message, Sender sender) {
		super();
		this.message = message;
		this.sender = sender;
	}
	
	
}
