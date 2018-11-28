package asylum.nursebot.commands;

import java.util.LinkedList;
import java.util.List;

import asylum.nursebot.NurseNoakes;
import asylum.nursebot.Sender;
import asylum.nursebot.objects.Locality;
import asylum.nursebot.objects.Permission;
import asylum.nursebot.objects.Visibility;
import asylum.nursebot.utils.SecurityChecker;
import asylum.nursebot.utils.StringTools;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CommandHandler {

	private NurseNoakes nurse;
	
	private List<CommandInterpreter> commands;
	
	public CommandHandler(NurseNoakes nurse) {
		commands = new LinkedList<>();
		this.nurse = nurse;
		
		commands.add(new CommandInterpreter(null)
				.setName("help")
				.setInfo("zeigt diese Hilfe an")
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.USER)
				.setAction(c -> {
					List<String> params = StringTools.tokenize(c.getParameter());
					boolean verbose = !params.isEmpty() && params.get(0).equals("verbose");
					c.getSender().send(getHelp(verbose), true);
				}));
	}
	
	public String getHelp(boolean verbose) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Hier ist eine Liste von Dingen, die ich kann:\n\n");
		
		CommandCategory current = null;
		
		for(CommandInterpreter command : commands) {
			if (command.getVisibility() == Visibility.PRIVATE)
				continue;
			
			if (command.getModule() != null && !nurse.isActive(command.getModule()))
				continue;
			
			if (command.getCategory() != current) {
				current = command.getCategory();
				
				if (current == null) {
					builder.append("\n");
				} else {
					builder.append("\n").append(StringTools.makeBold(current.getName())).append("\n");
				}
			}
			
			builder.append("/").append(command.getName()).append(" - ");
			
			if (verbose) {
				if (command.getLocality() != Locality.EVERYWHERE) {
					builder.append("(").append(command.getLocality()).append(") ");
				}
				
				if (command.getPermission() != Permission.ANY) {
					builder.append("(").append(StringTools.makeItalic(command.getPermission().toString())).append(" ) ");
				}
			}
			
			builder.append(command.getInfo()).append("\n");
		}
		
		return builder.toString();
	}
	
	public void parse(Message message) {
		if (!message.hasText()) {
			System.out.println("Command without text.");
			return;
		}
		String token = message.getText().split(" ")[0];
		token = token.substring(1);
		String parameter = "";
		if (token.length() + 1 < message.getText().length())
			parameter = message.getText().substring(token.length() + 2);
		
		CommandInterpreter command = null;
		for (CommandInterpreter c : commands) {
			if (c.getName().equals(token) || (c.getName() + "@" + nurse.getBotUsername()).equals(token)) {
				command = c;
				break;
			}
		}
		if (command == null) {
			System.out.println("Command not found: " + token);
			return;
		}
		
		if (command.getModule() != null && !nurse.isActive(command.getModule())) {
			System.out.println("Module is inactive.");
			return;
		}
		
		if (!command.getLocality().check(message.getChat())) {
			System.out.println("Wrong chat type.");
			return;
		}
		
		if (nurse.isChatPaused(message.getChatId()) && command.isPausable()) {
			System.out.println("Chat is paused.");
			return;
		}
		
		Sender sender = new Sender(message.getChatId(), nurse);
		
		if (command.getPermission() != Permission.ANY) {
			SecurityChecker checker = new SecurityChecker(nurse);
			try {
				if (!checker.checkRights(message.getChatId(), message.getFrom(), command.getPermission())) {
					sender.reply("Du hast leider nicht die nötigen Berechtigungen.", message);
					return;
				}
			} catch (TelegramApiException e) {
				e.printStackTrace();
				return;
			}
		}
		
		CommandContext context = new CommandContext(message, sender, parameter);
		try {
			command.getAction().action(context);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	public void add(CommandInterpreter command) {
		commands.add(command);
	}

	public int getNumberOfEntities() {
		return commands.size();
	}
}
