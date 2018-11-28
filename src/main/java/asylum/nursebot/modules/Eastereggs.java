package asylum.nursebot.modules;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.inject.Inject;

import asylum.nursebot.NurseNoakes;
import asylum.nursebot.commands.CommandCategory;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.commands.CommandInterpreter;
import asylum.nursebot.loader.AutoModule;
import asylum.nursebot.objects.Locality;
import asylum.nursebot.objects.Module;
import asylum.nursebot.objects.ModuleType;
import asylum.nursebot.objects.Permission;
import asylum.nursebot.objects.Visibility;
import asylum.nursebot.semantics.SemanticInterpreter;
import asylum.nursebot.semantics.SemanticsHandler;
import asylum.nursebot.semantics.WakeWord;
import asylum.nursebot.semantics.WakeWordType;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@AutoModule(load=true)
public class Eastereggs implements Module {

	private CommandCategory category;
	
	@Inject
	private CommandHandler commandHandler;
	@Inject
	private SemanticsHandler semanticsHandler;
	@Inject
	private NurseNoakes nurse;

	class RandomHugProperties {
		Long chatId;
		Map<Integer, User> users = new ConcurrentHashMap<>();
		Calendar next;
	}

	private Collection<RandomHugProperties> randomHugs = new ConcurrentLinkedQueue<>();

	public Eastereggs() {
		category = new CommandCategory("Eastereggs");
	}
	
	@Override
	public void init() {
		commandHandler.add(new CommandInterpreter(this)
				.setName("miau")
				.setInfo("Da muss wohl eine Katze gestreichelt werden.")
				.setVisibility(Visibility.PRIVATE)
				.setPermission(Permission.ANY)
				.setLocality(Locality.EVERYWHERE)
				.setCategory(category)
				.setAction(c -> {
					try {
						c.getSender().reply("*streichel*", c.getMessage());
					} catch (TelegramApiException e) {
						e.printStackTrace();
					}
				}));
		commandHandler.add(new CommandInterpreter(this)
				.setName("mimimi")
				.setInfo("Wollen wir die Muppets sehen?")
				.setVisibility(Visibility.PRIVATE)
				.setPermission(Permission.ANY)
				.setLocality(Locality.EVERYWHERE)
				.setCategory(category)
				.setAction(c -> {
					try {
						c.getSender().send("https://www.youtube.com/watch?v=VnT7pT6zCcA");
					} catch (TelegramApiException e) {
						e.printStackTrace();
					}
				}));
		commandHandler.add(new CommandInterpreter(this)
				.setName("eyeroll")
				.setInfo("")
				.setVisibility(Visibility.PRIVATE)
				.setPermission(Permission.ANY)
				.setLocality(Locality.EVERYWHERE)
				.setCategory(category)
				.setAction(c -> {
					try {
						c.getSender().send("🙄");
					} catch (TelegramApiException e) {
						e.printStackTrace();
					}
				}));

		commandHandler.add(new CommandInterpreter(this)
				.setName("hugoptin")
				.setInfo("")
				.setVisibility(Visibility.PRIVATE)
				.setPermission(Permission.ANY)
				.setLocality(Locality.EVERYWHERE)
				.setCategory(category)
				.setAction(c -> {
					RandomHugProperties randomHug = null;
					for (RandomHugProperties hug : randomHugs) {
						if (hug.chatId.equals(c.getMessage().getChatId())) {
							randomHug = hug;
							break;
						}
					}
					if (randomHug == null) {
						randomHug = new RandomHugProperties();
						randomHug.chatId = c.getMessage().getChatId();
						randomHugs.add(randomHug);
						final RandomHugProperties hug = randomHug;
						new Thread(() -> {
							Random random = new Random();
							try {
								while(true) {
									if (hug.next == null) {
										hug.next = Calendar.getInstance();
										hug.next.add(Calendar.DATE, 1);
										hug.next.set(Calendar.HOUR_OF_DAY, random.nextInt(24));
										hug.next.set(Calendar.MINUTE, random.nextInt(60));
										hug.next.set(Calendar.SECOND, random.nextInt(60));
									}
									Thread.sleep(30000);
									if (hug.next.compareTo(Calendar.getInstance()) < 0) {
										List<User> users = new LinkedList<>(hug.users.values());
										if (users.size() != 0) {
											User user = users.get(random.nextInt(users.size()));
											c.getSender().mention(user, "*random hug*");
										}
										hug.next = null;
									}
								}
							} catch (InterruptedException | TelegramApiException e) {
								e.printStackTrace();
							}
						}).start();
					}
					boolean add = false;
					if (randomHug.users.containsKey(c.getMessage().getFrom().getId())) {
						randomHug.users.remove(c.getMessage().getFrom().getId());
					} else {
						add = true;
						randomHug.users.put(c.getMessage().getFrom().getId(), c.getMessage().getFrom());
					}

					c.getSender().reply("Okay " + (add ? ": )" : ":c"), c.getMessage());
				}));
		
		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord("mau", WakeWordType.STANDALONE, false))
				.addWakeWord(new WakeWord("mau.", WakeWordType.STANDALONE, false))
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					String[] replys = new String[] {
							"*streichel*", "Mau", "*flausch*"
					};
					
					new Thread(() -> {
						Random random = new Random();
						
						try {
							Thread.sleep((random.nextInt(5) + 1) * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						try {
							c.getSender().reply(replys[random.nextInt(replys.length)], c.getMessage());
						} catch (TelegramApiException e) {
							e.printStackTrace();
						}
					}).start();
				}));
		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord("*boop*", WakeWordType.ANYWHERE, false))
				.addWakeWord(new WakeWord("*stups*", WakeWordType.ANYWHERE, false))
				.addWakeWord(new WakeWord("*anstups*", WakeWordType.ANYWHERE, false))
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					if (!c.getMessage().getText().contains("@" + nurse.getBotUsername()))
						return;
					
					String[] replys = new String[] {
							"*erschreck*", "*erschreck*\n*vom Stuhl fall*", "Au! D:"
					};
					
					Random random = new Random();
					
					c.getSender().reply(replys[random.nextInt(replys.length)], c.getMessage());
				}));
		
		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord("scheiße", WakeWordType.ANYWHERE, false))
				.addWakeWord(new WakeWord("scheiß", WakeWordType.ANYWHERE, false))
				.addWakeWord(new WakeWord("fuck ", WakeWordType.ANYWHERE, false))
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					String[] replys = new String[] {
							"Ich dulde keine Kraftausdrücke hier!", "Hey! Achte auf deine Sprache!", "Hey! Es sind Kinder anwesend."
					};
					
					Random random = new Random();
					
					if (random.nextInt(5) != 0)
						return;
					
					c.getSender().reply(replys[random.nextInt(replys.length)], c.getMessage());
				}));
		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord("danke", WakeWordType.ANYWHERE, false))
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					if (c.getMessage().getText().split(" ").length > 4)
						return;
					if (!c.getMessage().getText().contains(" Noakes"))
						return;
					
					String[] replys = new String[] {
							"Gern geschehen.", "Hab ich gerne gemacht."
					};
					
					Random random = new Random();	
					
					c.getSender().reply(replys[random.nextInt(replys.length)], c.getMessage());
				}));
		
		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord("hawara", WakeWordType.ANYWHERE, false))
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					if (!c.getMessage().getFrom().getUserName().equals("m4xcoat"))
						return;
					
					String[] replys = new String[] {
							"Heast!"
					};
					
					Random random = new Random();	
					
					c.getSender().reply(replys[random.nextInt(replys.length)], c.getMessage());
				}));
		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord("Gute Nacht", WakeWordType.ANYWHERE, false))
				.addWakeWord(new WakeWord("Nachti", WakeWordType.ANYWHERE, false))
				.addWakeWord(new WakeWord("Ich geh dann mal ins Bett.", WakeWordType.ANYWHERE, false))
				.addWakeWord(new WakeWord("Ich geh dann mal schlafen.", WakeWordType.ANYWHERE, false))
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					String[] replys = new String[] {
							"Ciao. o/", "Gute Nacht.", "Gute Nacht!", "Eine erholsame Nacht wünsche ich.", 
							"Schlaf gut.", "Träum was Schönes.", "Nachtilein", "*zum Bett trag*", 
							"Schlaf fein.", "Träum was Flauschiges.", "Eine gute Nacht wünsche ich.", 
							"Gute Idee, ich bin auch schon müde. *gähn*", "Bis morgen.",
							"*auf die Uhr schau*\nJetzt schon? o.ô", "Tschüss, man liest sich morgen.",
							"Aber nicht mehr lange mit dem Handy spielen, ja?"
					};
					
					Calendar calendar = Calendar.getInstance();
					int hour = calendar.get(Calendar.HOUR_OF_DAY);
					if (!(hour < 2 || hour > 21))
						return;
					
					Random random = new Random();
					c.getSender().reply(replys[random.nextInt(replys.length)], c.getMessage());
				}));
	}

	@Override
	public String getName() {
		return "Eastereggs";
	}

	@Override
	public ModuleType getType() {
		return new ModuleType()
				.set(ModuleType.COMMAND_MODULE)
				.set(ModuleType.SEMANTIC_MODULE);
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
	
}
