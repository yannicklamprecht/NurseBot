package asylum.nursebot.modules;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import asylum.nursebot.loader.ModuleDependencies;
import asylum.nursebot.utils.StringTools;
import asylum.nursebot.utils.ThreadHelper;
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
	private ModuleDependencies moduleDependencies;
	@Inject
	private NurseNoakes nurse;

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
					c.getSender().send("https://www.youtube.com/watch?v=VnT7pT6zCcA");
				}));
		commandHandler.add(new CommandInterpreter(this)
				.setName("eyeroll")
				.setInfo("")
				.setVisibility(Visibility.PRIVATE)
				.setPermission(Permission.ANY)
				.setLocality(Locality.EVERYWHERE)
				.setCategory(category)
				.setAction(c -> {
					c.getSender().send("🙄");
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
					
					Random random = ThreadLocalRandom.current();
					
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
					
					Random random = ThreadLocalRandom.current();
					
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
					
					Random random = ThreadLocalRandom.current();
					c.getSender().reply(replys[random.nextInt(replys.length)], c.getMessage());
				}));

		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord("Nobelpreis", WakeWordType.ANYWHERE, false))
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					if (!c.getMessage().getFrom().getUserName().equals("overflowerror"))
						return;
					if (!c.getMessage().getText().contains(" geht an"))
						return;

					UserLookup lookup = moduleDependencies.get(UserLookup.class);

					List<User> users = null;

					if (lookup != null) {
						users = lookup.getMentions(c.getMessage());
					}

					if (users == null) {
						users = new LinkedList<>();
						if (c.getMessage().getReplyToMessage() != null) {
							users.add(c.getMessage().getReplyToMessage().getFrom());
						}
					}

					if (users.size() != 1)
						return;

					final User user = users.get(0);

					ThreadHelper.delay(() -> {
						c.getSender().send("Gratuliere," + StringTools.makeMention(user) + "!\nDu hast es wirklich verdient.", true);
					}, 1000);

					ThreadHelper.delay(() -> {
						c.getSender().send("*überreicht die Medaille*\n\nhttps://upload.wikimedia.org/wikipedia/ka/e/ed/Nobel_Prize.png", false);
					}, 2000);

					ThreadHelper.delay(() -> {
						c.getSender().send("*applaudiert*", false);
					}, 3000);

				}));

		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord("CPR", WakeWordType.ANYWHERE, false))
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					if (!c.getMessage().getFrom().getUserName().equals("overflowerror"))
						return;
					if (!c.getMessage().getText().contains("@" + nurse.getBotUsername()))
						return;

					ThreadHelper.delay(() -> {
						c.getSender().send("*schreckt auf und atmet schnell* Was... wie... Was ist passiert? D:");
					}, 5000);

				}));

		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord("Messer", WakeWordType.ANYWHERE, false))
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					if (!c.getMessage().getFrom().getUserName().equals("Konirrikon") && !c.getMessage().getFrom().getUserName().equals("overflowerror"))
						return;

					String compareString = ("*ein Messer nach @" + nurse.getBotUsername() + " werf*").toLowerCase();

					if (!c.getMessage().getText().toLowerCase().contains(compareString))
						return;

					String[] replys = new String[] {
							"*das Messer streift Noakes an der Schulter* AAAAAHHH... *läuft schreiend zum Verbandskasten*",
							"*mit dem Klemmbrett das Messer abfang* HAST DU GERADE EIN MESSER NACH MIR GEWORFEN?! Das wird noch ein Nachspiel haben, Freundchen!",
							"*das Messer verfehlt und trifft die Vase hinter Noakes* ... Wer war das?! Wenn ich die Person erwische... *fängt an, Überwachungskameras aufzubauen*",
							"*das Messer aus der Luft greif und zurückwerf* *verfehlt und stattdessen das Sofa treff* ... Ach, verdammt..."
					};

					Random random = ThreadLocalRandom.current();
					int i = random.nextInt(replys.length);

					c.getSender().send(replys[i]);

					if (i == 0) {
						ThreadHelper.delay(() -> {
							c.getSender().send(StringTools.makeMention(c.getMessage().getFrom()) + " wurde gestrikt.", true);
							c.getSender().send("... Das kommt davon.");
						}, 1000);
					}

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
