package asylum.nursebot.modules;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import asylum.nursebot.objects.*;
import asylum.nursebot.objects.Module;
import com.google.inject.Inject;

import asylum.nursebot.NurseNoakes;
import asylum.nursebot.Sender;
import asylum.nursebot.commands.CommandCategory;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.commands.CommandInterpreter;
import asylum.nursebot.loader.AutoModule;
import asylum.nursebot.utils.StringTools;
import asylum.nursebot.utils.log.Logger;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@AutoModule(load=true)
public class Straitjacket implements Module {
	public static final int STRIKES_TO_RESTRICT = 5;
	public static final int RESTRICT_TIME = 5*60*1000;
	public static final int STRIKE_TIMEOUT = 5*60*1000;
	
	@Inject
	private NurseNoakes nurse;
	@Inject
	private CommandHandler commandHandler;

	private Logger logger = Logger.getModuleLogger("Straitjacket");

	class Restrict {
		private long chatid;
		private User target;
		private long time;
		
		public Restrict(long chatid, User target, long time) {
			super();
			this.chatid = chatid;
			this.target = target;
			this.time = time;
		}	
	}
	
	class Strike {
		private long chatid;
		private User target;
		private User source;
		private long time;
		
		public Strike(long chatid, User target, User source, long time) {
			super();
			this.chatid = chatid;
			this.target = target;
			this.source = source;
			this.time = time;
		}
		
		public String toString() {
			return target.getUserName() + " - " + source.getUserName() + " - " + time;
		}
	}
	
	private Collection<Strike> strikes = new ConcurrentLinkedQueue<Strike>();
	private Collection<Restrict> restricts = new ConcurrentLinkedQueue<Restrict>();
	
	private void deleteOldStrikes() {
		long now = System.currentTimeMillis();
		
		for(Strike strike : strikes) {
			if (strike.time < now - STRIKE_TIMEOUT)
				strikes.remove(strike);
		}
	}
	
	private boolean addStrike(long chatid, User target, User source) {
		if (source.getId() != 0) {
			for (Strike strike : strikes) {
				if (strike.chatid == chatid && strike.target.getId().equals(target.getId()) && strike.source.getId().equals(source.getId())) {
					return false;
				}
			}
		}
		strikes.add(new Strike(chatid, target, source, System.currentTimeMillis()));
		return true;
	}
	
	private int countStrikes(long chatid, User target) {
		int i = 0; 
		
		for (Strike strike : strikes) {
			if (strike.chatid == chatid && strike.target.getId().equals(target.getId()))
				i++;
		}
		return i;
	}
	
	private boolean checkRestrict(long chatid, User target) {
		return (countStrikes(chatid, target) >= STRIKES_TO_RESTRICT);
	}

	public void anonymousStrike(long chatid, User target, Sender sender) throws TelegramApiException {
		deleteOldStrikes();

		addStrike(chatid, target, new MinimalUser(0, "", "", ""));

		sender.send(StringTools.makeMention(target) + " wurde gestrikt.", true);
		if (checkRestrict(chatid, target)) {
			restrict(chatid, target, sender);
		}
	}
	
	private void restrict(long chatid, User target, Sender sender) {
		Restrict restrict = new Restrict(chatid, target, System.currentTimeMillis());
		restricts.add(restrict);
		
		RestrictChatMember restrictcmd = new RestrictChatMember(chatid, target.getId());
		restrictcmd.setCanSendMessages(false);
		restrictcmd.setUntilDate(0);
		
		try {
			nurse.execute(restrictcmd);
			
			sender.mention(target, ", sei nett zu den anderen Patienten. Du bist jetzt für " + (RESTRICT_TIME / 1000 / 60) + " Minuten gesperrt.");
		} catch (TelegramApiException e) {
			logger.error("Can not execute restrict command.");
			logger.exception(e);
		}
		
		
		new Thread(() -> {
			try {
				Thread.sleep(STRIKE_TIMEOUT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			try {
				if (!restricts.contains(restrict)) {
					logger.error("Restrict is not current.");
					return;
				}
				
				undoRestrict(restrict);
				sender.mention(target, ", ich hoffe, du hast deine Lektion gelernt.");
			} catch (TelegramApiException e) {
				logger.error("Can not execute unrestrict command.");
				logger.exception(e);
			}
			
		}).start();
	}
	
	private void undoRestrict(Restrict restrict) throws TelegramApiException {
		RestrictChatMember undo = new RestrictChatMember(restrict.chatid, restrict.target.getId());
		undo.setCanSendOtherMessages(true);
		undo.setCanSendMessages(true);
		undo.setCanAddWebPagePreviews(true);
		undo.setCanSendMediaMessages(true);
		
		nurse.execute(undo);
		
		restricts.remove(restrict);
			
	}
	
	private CommandCategory category;
	
	public Straitjacket() {
		this.category = new CommandCategory("Zwangsjacke");
	}
	
	@Override
	public void init() {
		commandHandler.add(new CommandInterpreter(this)
				.setName("strikes")
				.setInfo("zeigt die Anzahl der eigenen Strikes an")
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.USER)
				.setLocality(Locality.GROUPS)
				.setCategory(category)
				.setAction(c -> {
					deleteOldStrikes();
					c.getSender().reply("Du hast " + countStrikes(c.getMessage().getChatId(), c.getMessage().getFrom()) + " Strikes.", c.getMessage());
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("clearstrikes")
				.setInfo("")
				.setVisibility(Visibility.PRIVATE)
				.setPermission(Permission.ADMIN)
				.setLocality(Locality.GROUPS)
				.setCategory(category)
				.setAction(c -> {
					strikes.clear();
					c.getSender().reply("Alle Strikes wurden entfernt.", c.getMessage());
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("strike")
				.setInfo("fügt einem User einen neuen Strike hinzu")
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.USER)
				.setLocality(Locality.GROUPS)
				.setCategory(category)
				.setAction(c -> {
					if (c.getMessage().getReplyToMessage() == null) {
						c.getSender().reply("Um jemanden zu striken, musst du auf eine Nachricht desjenigen antworten.", c.getMessage());
						return;
					}
					User target = c.getMessage().getReplyToMessage().getFrom();
					User source = c.getMessage().getFrom();
					
					if (nurse.getBotUsername().equals(target.getUserName())) {
						c.getSender().reply("Haha, sehr witzig.", c.getMessage());
						return;
					}

					if (target.getId().equals(source.getId())) {
						c.getSender().reply(StringTools.makeMention(target) + " wurde ge-... warte, was?! Du willst dich selbst striken. Ähm. Nein.", c.getMessage(), true);
						return;
					}
					
					deleteOldStrikes();
					if (!addStrike(c.getMessage().getChatId(), target, source)) {
						c.getSender().reply("Du hast " + StringTools.makeMention(target) + " bereits gestrikt.", c.getMessage(), true);
						return;
					}
					c.getSender().reply(StringTools.makeMention(target) + " wurde gestrikt.", c.getMessage(), true);
					if (checkRestrict(c.getMessage().getChatId(), target)) {
						restrict(c.getMessage().getChatId(), target, c.getSender());
					}
				}));
	}

	@Override
	public String getName() {
		return "Straitjacket";
	}

	@Override
	public ModuleType getType() {
		return new ModuleType()
				.set(ModuleType.COMMAND_MODULE)
				.set(ModuleType.DEPENDENCY_MODULE);
	}

	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
		try {
			for (Restrict restrict : restricts) {
					undoRestrict(restrict);
			}
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
		strikes.clear();
	}

	@Override
	public void shutdown() {
		deactivate();
	}
}
