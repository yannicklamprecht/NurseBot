package asylum.nursebot.modules;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import asylum.nursebot.NurseNoakes;
import asylum.nursebot.Sender;
import asylum.nursebot.commands.CommandCategory;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.commands.CommandInterpreter;
import asylum.nursebot.exceptions.WhatTheFuckException;
import asylum.nursebot.loader.AutoModule;
import asylum.nursebot.loader.ModuleDependencies;
import asylum.nursebot.modules.birthdays.Privacy;
import asylum.nursebot.objects.Locality;
import asylum.nursebot.objects.Module;
import asylum.nursebot.objects.ModuleType;
import asylum.nursebot.objects.Permission;
import asylum.nursebot.objects.Visibility;
import asylum.nursebot.persistence.ModelManager;
import asylum.nursebot.persistence.modules.BirthdaysBirthday;
import asylum.nursebot.persistence.modules.BirthdaysCongratulation;
import asylum.nursebot.utils.StatefulPredicate;
import asylum.nursebot.utils.StringTools;
import asylum.nursebot.utils.ThreadHelper;
import asylum.nursebot.utils.log.Logger;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@AutoModule(load=true)
public class Birthdays implements Module {
	@Inject
	private CommandHandler commandHandler;

	@Inject
	private NurseNoakes nurse;

	@Inject
	private ModuleDependencies moduleDependencies;

	private Logger logger = Logger.getModuleLogger("Birthdays");

	private CommandCategory category;

	private Timer timer;

	private static final String[] CONGRATULATIONS = {
			"\\*Party-Hut aufsetz\\*\n" +
					"%s hat heute Geburtstag. \\o/ Los, alle gratulieren.\n" +
					"\n" +
					"Alles Gute, ich hoffe, du hast einen tollen Tag. : )",

			"Happy Birthday to you!\n" +
					"Happy birthday to you!\n" +
					"Happy birthday, dear %s!\n" +
					"Happy birthday to you.",

			"Wie schön, dass du geboren bist,\n" +
					"wir hätten dich sonst sehr vermisst.\n" +
					"Wie schön, dass wir beisammen sind,\n" +
					"wir gratulieren dir, Geburtstagskind!\n" +
					"\n" +
					"Alles Gute, %s. : )",

			"For they're a jolly good fellow, for they're a jolly good fellow.\n" +
					"For they're a jolly good felloooowwwwww, and so say all of us!\n" +
					"\n" +
					"Happy Birthday, %s. Feier schön. : )",

			"Ich kleines Vöglein hat mir gezwitschert, dass %s heute Geburtstag hat.\n" +
					"Einen tollen Geburtstag wünsche ich dir. : )\n" +
					"\n" +
					"https://www.youtube.com/watch?v=iqj0PAvYrpI",

			"\\*auf den Kalender schau\\*\n" +
					"Oha... \\*schnell in die Küche geh\\*\n" +
					"\\*mit einer riesigen Torte wieder zurück komm\\*\n" +
					"\n" +
					"%s, ich hoffe dir gefällt deine Geburtstagstorte. : )",

			"\\*setzt %s einen Partyhut auf\\*\n" +
					"Alles Gute zum Geburtstag!\n" +
					"Fühl dich ganz doll geknuddelt. :3",

			"\\*stößt die Tür auf\\*\n" +
					"ÜBERRASCHUNGS-PARTY für %s!!!\n" +
					"\n" +
					"https://www.youtube.com/watch?v=jqYxyd1iSNk"
	};

	private class BirthdayTimerTask extends TimerTask {
		private Module module;
		private Sender sender;

		public BirthdayTimerTask(Module module, Sender sender) {
			this.module = module;
			this.sender = sender;
		}

		@Override
		public void run() {
			try {
				nurse.getConnector().connectThread();

				if (!nurse.isActive(module))
					return;

				List<BirthdaysBirthday> birthdays = BirthdaysBirthday.findByDate(LocalDate.now());

				logger.debug("Size of birthday list: " + birthdays.size());

				for (BirthdaysBirthday birthday : birthdays) {
					if (birthday.getPrivacy() == Privacy.PRIVATE)
						continue;

					UserLookup lookup = moduleDependencies.get(UserLookup.class);
					if (lookup == null) {
						logger.error("Couldn't get UserLookup instance.");
						return;
					}
					User user = lookup.getUser(birthday.getUserId());
					if (user == null) {
						logger.error("Couldn't find User object.");
						break;
					}

					List<BirthdaysCongratulation> congratulations = BirthdaysCongratulation.findByUserId(birthday.getUserId());

					logger.debug("Size of congratulations list: " + congratulations.size());

					for (BirthdaysCongratulation congratulation : congratulations) {
						Random random = ThreadLocalRandom.current();
						String text = String.format(CONGRATULATIONS[random.nextInt(CONGRATULATIONS.length)], StringTools.makeMention(user));

						ThreadHelper.ignore(TelegramApiException.class, () -> sender.send(congratulation.getChatId(), text, true));
					}
				}
			} catch (Exception e) {
				logger.error("Got execption in timer.");
				logger.exception(e);
			} finally {
				nurse.getConnector().disconnectThread();
			}
		}
	}

	public Birthdays() {
		category = new CommandCategory("Geburtstage");

		ModelManager.build(BirthdaysCongratulation.class);
		ModelManager.build(BirthdaysBirthday.class);
	}

	@Override
	public String getName() {
		return "Birthdays";
	}

	@Override
	public ModuleType getType() {
		return new ModuleType().set(ModuleType.COMMAND_MODULE);
	}

	private String info(BirthdaysBirthday birthdayObj, String name, boolean self, boolean overridePrivacy) {
		Privacy privacy = birthdayObj.getPrivacy();

		if (overridePrivacy)
			privacy = Privacy.PUBLIC;

		DateTimeFormatter dateOnly = DateTimeFormatter.ofPattern("d. M.");
		LocalDate birthday = birthdayObj.getBirthday();

		String date;

		switch (privacy) {
			case PRIVATE:
				return null;
			case PUBLIC:
				date = birthday.toString();
				if (self)
					return "Du hast am " + date + " Geburtstag.";
				else
					return name + " hat am " + date + "Geburtstag.";
			case PUBLIC_DATE:
				date = birthday.format(dateOnly);
				if (self)
					return "Du hast am " + date + " Geburtstag.";
				else
					return name + "hat am " + date + "Geburtstag.";
			case PUBLIC_AGE:
				date = String.valueOf(birthday.until(LocalDate.now()).getYears());
				if (self)
					return "Du bist " + date + " Jahre alt.";
				else
					return name + " ist " + date + "Jahre alt.";
			default:
				throw new WhatTheFuckException("No such enum state");
		}
	}

	@Override
	public void init() {

		Calendar midnightTomorrow = Calendar.getInstance();
		midnightTomorrow.add(Calendar.DATE, 1);
		midnightTomorrow.set(Calendar.HOUR_OF_DAY, 1);
		midnightTomorrow.set(Calendar.MINUTE, 1);

		Duration oneDay = Duration.ofDays(1);

		timer = new Timer();

		timer.scheduleAtFixedRate(new BirthdayTimerTask(this, new Sender(null, nurse)), midnightTomorrow.getTime(), oneDay.toMillis());

		commandHandler.add(new CommandInterpreter(this)
				.setName("setbirthday")
				.setInfo("setzt den eigenen Geburtstag")
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setLocality(Locality.EVERYWHERE)
				.setCategory(category)
				.setAction(c -> {
					final String synopsis = "Synopsis: /setbirthday YYYY-MM-DD [PRIVACY]\n\nPRIVACY := private|public|public-age|public-date\n\nDefault-Privacy ist private.";

					List<String> arguments = StringTools.tokenize(c.getParameter());

					Privacy privacy = null;

					if (arguments.size() == 1) {
						privacy = Privacy.PRIVATE;
					} else if (arguments.size() == 2) {
						privacy = Privacy.fromString(arguments.get(1));
					}

					if (privacy == null) {
						c.getSender().reply(synopsis, c.getMessage());
						return;
					}

					LocalDate date;

					try {
						date = LocalDate.parse(arguments.get(0));
					} catch (DateTimeParseException e) {
						c.getSender().reply(synopsis, c.getMessage());
						return;
					}

					int userid = c.getMessage().getFrom().getId();

					BirthdaysBirthday birthday = BirthdaysBirthday.findByUserid(userid);
					if (birthday == null) {
						birthday = new BirthdaysBirthday(userid);
					}
					birthday.setBirthday(date);
					birthday.setPrivacy(privacy);
					birthday.saveIt();

					c.getSender().reply("Dein Geburtstag steht jetzt im Kalender. \\o/", c.getMessage());
				}));

		commandHandler.add(new CommandInterpreter(this)
				.setName("deletebirthday")
				.setInfo("löscht den eigenen Geburtstag")
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setLocality(Locality.EVERYWHERE)
				.setCategory(category)
				.setAction(c -> {
					BirthdaysBirthday birthday = BirthdaysBirthday.findByUserid(c.getMessage().getFrom().getId());
					if (birthday == null) {
						c.getSender().reply("Aber ich kenne deinen Geburtstag doch nicht mal. D:", c.getMessage());
						return;
					}

					birthday.delete();

					c.getSender().reply("*deinen Geburtstag im Kalender durchstreich*", c.getMessage());
				}));

		commandHandler.add(new CommandInterpreter(this)
				.setName("enablebirthday")
				.setInfo("aktiviert Geburtstags-Gratulationen in Gruppen")
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setLocality(Locality.GROUPS)
				.setCategory(category)
				.setAction(c -> {
					int userid = c.getMessage().getFrom().getId();
					long  chatid = c.getMessage().getChat().getId();

					BirthdaysBirthday birthday = BirthdaysBirthday.findByUserid(userid);
					if (birthday == null) {
						c.getSender().reply("Ich würde dir ja gerne gratulieren, aber ich weiß nicht, wann du Geburtstag hast. : (\n" +
								"Mit /setbirthday kannst du es mir verraten (übrigens auch in Private Chats mit mir, wenn du ihn geheim halten willst).", c.getMessage());
						return;
					}

					if (birthday.getPrivacy() == Privacy.PRIVATE) {
						c.getSender().reply("Du hast mir gesagt, dein Geburtstag wäre privat. Bitte ändere das, und versuch es nochmal.", c.getMessage());
						return;
					}

					BirthdaysCongratulation congratulation = BirthdaysCongratulation.find(userid, chatid);
					if (congratulation != null) {
						c.getSender().reply("Das mache ich doch schon...", c.getMessage());
						return;
					}
					congratulation = new BirthdaysCongratulation(userid, chatid);
					congratulation.saveIt();

					c.getSender().reply("Okay, an deinem nächsten Geburtstag gratuliere ich dir. : )", c.getMessage());

					if (birthday.getBirthday().isEqual(LocalDate.now())) {
						ThreadHelper.delay(() -> {
							c.getSender().reply("Oh, by the way: Happy Birthday! ; )", c.getMessage());
						}, 2000);
					}
				}));

		commandHandler.add(new CommandInterpreter(this)
				.setName("disablebirthday")
				.setInfo("deaktiviert Geburtstags-Gratulationen")
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setLocality(Locality.GROUPS)
				.setCategory(category)
				.setAction(c -> {
					int userid = c.getMessage().getFrom().getId();
					long  chatid = c.getMessage().getChat().getId();

					BirthdaysCongratulation congratulation = BirthdaysCongratulation.find(userid, chatid);
					if (congratulation == null) {
						c.getSender().reply("Aber... äh... Gut, von mir aus: ich werde dir hier (wie bisher) nicht zum Geburtstag gratulieren.", c.getMessage());
						return;
					}
					congratulation.delete();

					c.getSender().reply("Ich werde dir hier ab jetzt nicht mehr zun Geburtstag gratulieren.", c.getMessage());
				}));

		commandHandler.add(new CommandInterpreter(this)
				.setName("showbirthday")
				.setInfo("zeigt die Geburtstage der erwähnten Personen an")
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setLocality(Locality.EVERYWHERE)
				.setCategory(category)
				.setAction(c -> {
					final String synopsis = "Synopsis: /showbirthday [all|{MENTION}]";

					User user = c.getMessage().getFrom();
					Chat chat = c.getMessage().getChat();

					boolean isGroupChat = chat.isGroupChat() || chat.isSuperGroupChat();

					List<String> parameters = StringTools.tokenize(c.getParameter());
					if (parameters.size() == 0) {
						BirthdaysBirthday birthday = BirthdaysBirthday.findByUserid(user.getId());
						if (birthday == null) {
							c.getSender().reply("Ich kenne leider deinen Geburtstag nicht.", c.getMessage());
							return;
						}

						if (isGroupChat && birthday.getPrivacy() == Privacy.PRIVATE) {
							c.getSender().reply("Du hast mir gesagt, dein Geburtstag sei privat. Wenn du wissen willst, wann er eingetragen ist, frag mich in einem privaten Chat.", c.getMessage());
							return;
						}

						BirthdaysCongratulation congratulation = BirthdaysCongratulation.find(user.getId(), chat.getId());

						StringBuilder builder = new StringBuilder();
						builder.append(info(birthday, null, true, !isGroupChat));

						if (isGroupChat && congratulation != null) {
							builder.append("\nIn diesem Chat gratuliere ich dir zum Geburtstag.");
						}

						c.getSender().reply(builder.toString(), c.getMessage());
					} else if (parameters.get(0).equalsIgnoreCase("all")) {
						if (!isGroupChat) {
							c.getSender().reply("Diese Funktion ist nur in Gruppenchats sinnvoll.", c.getMessage());
							return;
						}

						UserLookup lookup = moduleDependencies.get(UserLookup.class);
						if (lookup == null) {
							logger.error("Couldn't get UserLookup instance.");
							c.getSender().reply("Da ist etwas schief gelaufen. Details sind im Log-File.", c.getMessage());
							return;
						}

						List<BirthdaysCongratulation> congratulations = BirthdaysCongratulation.findByChatId(chat.getId());

						if (congratulations.isEmpty()) {
							c.getSender().reply("In diesem Chat gratuliere ich im Moment niemandem zum Geburtstag.\nWenn du die erste Person sein willst, denn benutze /enablebirthday.", c.getMessage());
							return;
						}

						StringBuilder builder = new StringBuilder();
						builder.append("In diesem Chat gratuliere ich im Moment folgenden Leuten:\n");

						for(BirthdaysCongratulation congratulation : congratulations) {
							BirthdaysBirthday birthday = BirthdaysBirthday.findByUserid(congratulation.getUserId());
							if (birthday == null)
								continue;
							User target = lookup.getUser(birthday.getUserId());
							if (target == null)
								continue;
							String format = info(birthday, target.getFirstName(), user.getId().equals(target.getId()), false);
							if (format == null)
								continue;
							builder.append(format).append("\n");
						}

						c.getSender().reply(builder.toString(), c.getMessage());
					} else {
						UserLookup lookup = moduleDependencies.get(UserLookup.class);
						if (lookup == null) {
							logger.error("Couldn't get UserLookup instance.");
							c.getSender().reply("Ohje, da ist wohl ein Problem aufgetreten. Details stehen im Log-File.", c.getMessage());
							return;
						}

						Set<User> users = lookup.getMentions(c.getMessage()).stream()
								.filter(new StatefulPredicate<Set<Integer>, User>(
										new HashSet<>(),
										(s, e) -> !(s.add(e.getId())))
								).collect(Collectors.toSet());


						if (users.size() > 5) {
							c.getSender().reply("Das sind etwas viele Namen, findest du nicht?", c.getMessage());
							return;
						}

						StringBuilder builder = new StringBuilder();

						for (User target : users) {
							BirthdaysBirthday birthday = BirthdaysBirthday.findByUserid(target.getId());
							if (birthday == null) {
								builder.append("Für " + user.getFirstName() + " ist kein Geburtstag eingetragen.\n");
								continue;
							}
							if (birthday.getPrivacy() == Privacy.PRIVATE) {
								builder.append("Der Geburtstag von " + user.getFirstName() + " ist privat.\n");
								continue;
							}
							builder.append(info(birthday, target.getFirstName(), user.getId().equals(target.getId()), false)).append("\n");
						}

						if (users.size() > 3) {
							builder.append("\nWofür brauchst du so viele Geburtstage?");
						}

						c.getSender().reply(builder.toString(), c.getMessage());
					}
				}));
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
