package asylum.nursebot.modules;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.inject.Inject;

import asylum.nursebot.NurseNoakes;
import asylum.nursebot.Sender;
import asylum.nursebot.commands.CommandCategory;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.commands.CommandInterpreter;
import asylum.nursebot.exceptions.ModuleNotActiveException;
import asylum.nursebot.exceptions.NoPrivateChatPresentException;
import asylum.nursebot.loader.AutoDependency;
import asylum.nursebot.loader.AutoModule;
import asylum.nursebot.objects.Locality;
import asylum.nursebot.objects.Module;
import asylum.nursebot.objects.ModuleType;
import asylum.nursebot.objects.Permission;
import asylum.nursebot.objects.Visibility;
import asylum.nursebot.persistence.ModelManager;
import asylum.nursebot.persistence.modules.PrivateNotifierChat;
import asylum.nursebot.utils.StringTools;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@AutoModule(load=true)
@AutoDependency
public class PrivateNotifier implements Module {
	private Set<Integer> chats = new ConcurrentSkipListSet<>();

	@Inject
	private NurseNoakes nurse;
	
	@Inject
	private CommandHandler commandHandler;

	private CommandCategory category;
	
	@Override
	public String getName() {
		return "Private Notifier";
	}

	@Override
	public ModuleType getType() {
		return new ModuleType()
			.set(ModuleType.COMMAND_MODULE)
			.set(ModuleType.DEPENDENCY_MODULE);
	}

	public PrivateNotifier() {
		ModelManager.build(PrivateNotifierChat.class);
		
		category = new CommandCategory("Private Benachrichtigungen");
	}
	
	@Override
	public void init() {
		List<PrivateNotifierChat> list = PrivateNotifierChat.findAll();
		for (PrivateNotifierChat chat : list) {
			chats.add(chat.getUserId());
		}
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("notifier")
				.setInfo("(de-)aktiviert private Benachrichtungen")
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setVisibility(Visibility.PUBLIC)
				.setCategory(category)
				.setAction(c -> {
					if (c.getMessage().getChat().isGroupChat() || c.getMessage().getChat().isSuperGroupChat()) {
						c.getSender().reply("Dazu musst du mich privat anschreibn.", c.getMessage());
						return;
					}
					
					String help = "Synopsis: /notifier on|off";
					
					boolean has = chats.contains(c.getMessage().getFrom().getId());
					
					List<String> args = StringTools.tokenize(c.getParameter());
					
					if (args.size() != 1) {
						c.getSender().reply(help + "\n\nIm Moment bekommst du " + (has ? "" : "keine ") + "privaten Benachrichtigungen.", c.getMessage());
						return;
					}
					
					switch(args.get(0).toLowerCase()) {
					case "on":
						if (!has)
							addPrivateChat(c.getMessage().getFrom(), c.getMessage().getChatId());
						break;
					case "off":
						if (has)
							removePrivateChat(c.getMessage().getFrom());
						break;
					default:
						c.getSender().reply(help, c.getMessage());
						return;
					}
					
					has = chats.contains(c.getMessage().getFrom().getId());
					
					c.getSender().reply("Private Benachrichtigungen sind " + (has ? "aktiviert." : "deaktiviert."), c.getMessage());
				}));
	}
	
	public boolean hasPrivateChat(User user) throws ModuleNotActiveException {
		checkModule();
		
		return chats.contains(user.getId());
	}
	
	private void addPrivateChat(User user, long chatid) {
		if (chats.contains(user.getId())) {
			return;
		}
		chats.add(user.getId());
		new PrivateNotifierChat(user.getId()).saveIt();
	}
	
	private void removePrivateChat(User user) {
		if (!chats.contains(user.getId())) {
			return;
		}
		chats.remove(new Long(user.getId()));
		PrivateNotifierChat chat = PrivateNotifierChat.find(user.getId());
		chat.delete();
	}
	
	private void checkModule() throws ModuleNotActiveException {
		if (!nurse.isActive(this)) {
			throw new ModuleNotActiveException();
		}
	}
	
	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void shutdown() {
	}

	public void send(Sender sender, Chat chat, User user, String string) throws ModuleNotActiveException, TelegramApiException, NoPrivateChatPresentException {
		if (!hasPrivateChat(user))
			throw new NoPrivateChatPresentException();
		
		sender.send(user.getId(), user.getFirstName() + ", du hast eine neue Benachrichtigung aus dem Chat '" +
				(chat.isUserChat() ? (chat.getFirstName() + " " + chat.getLastName()) : chat.getTitle()) + "'\n\n" + string);
	}
}
