package asylum.nursebot.modules;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.google.inject.Inject;

import asylum.nursebot.NurseNoakes;
import asylum.nursebot.Sender;
import asylum.nursebot.commands.CommandCategory;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.commands.CommandInterpreter;
import asylum.nursebot.executor.CallbackContext;
import asylum.nursebot.executor.ErrorCallback;
import asylum.nursebot.executor.ExecuterData;
import asylum.nursebot.executor.ExitCallback;
import asylum.nursebot.executor.ExternalExecuter;
import asylum.nursebot.loader.AutoModule;
import asylum.nursebot.objects.Locality;
import asylum.nursebot.objects.Module;
import asylum.nursebot.objects.ModuleType;
import asylum.nursebot.objects.Permission;
import asylum.nursebot.objects.Visibility;
import static asylum.nursebot.utils.ExceptionIgnorer.ignore;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@AutoModule(load=true)
public class Builder implements Module {
	private final String PREFIX = "/git/buildtools/";
	private final String RUNDIR = System.getProperty("user.dir");
	
	
	@Inject
	private CommandHandler commandHandler;
	
	private CommandCategory category;
	
	@Override
	public String getName() {
		return "Builder";
	}

	@Override
	public ModuleType getType() {
		return new ModuleType()
				.set(ModuleType.COMMAND_MODULE)
				.set(ModuleType.META_MODULE);
	}

	public Builder() {
		category = new CommandCategory("Builder");
	}
	
	private void startScript(String scriptname, List<String> arguments, ExitCallback exit, ErrorCallback error, CallbackContext context) {
		ExternalExecuter executer = new ExternalExecuter(scriptname, arguments, new File(RUNDIR + PREFIX), exit, error, context);
		new Thread(executer).start();
	}
	
	private CallbackContext getContext(Message message, Sender sender) {
		CallbackContext context = new CallbackContext();
		context.put(message);
		context.put(sender);
		return context;
	}
	
	private void update(CallbackContext context) {
		startScript("./update.sh", null, (e, c) -> {
			String msg;
			if (e.getValue() != 0) {
				msg = "Das Aktualisieren ist fehlgeschlagen.";
			} else {
				msg = "Das Aktualisieren war erfolgreich.";
			}
			ignore(TelegramApiException.class, () -> {
				c.get(Sender.class).reply(msg, c.get(Message.class));
			});
		}, (e, c) -> {
			e.printStackTrace();
		}, context);
	}
		
	private void build(CallbackContext context) {
		startScript("./build.sh", Arrays.asList(RUNDIR + "/"), (e, c) -> {
			String msg;
			if (e.getValue() != 0) {
				msg = "Der Build ist fehlgeschlagen mit " + e.getValue() + ".";
			} else {
				msg = "Der Build war erfolgreich.";
			}
			ignore(TelegramApiException.class, () -> {
				c.get(Sender.class).reply(msg, c.get(Message.class));
			});
		}, (e, c) -> {
			e.printStackTrace();
		}, context);
	}
	
	private void deploy(CallbackContext context, String version) {
		startScript("./deploy.sh", Arrays.asList(RUNDIR + "/", version), (e, c) -> {
			String msg;
			if (e.getValue() != 0) {
				msg = "Der Deploy ist fehlgeschlagen.";
			} else {
				msg = "Der Deploy war erfolgreich.";
			}
			ignore(TelegramApiException.class, () -> {
				c.get(Sender.class).reply(msg, c.get(Message.class));
			});
		}, (e, c) -> {
			e.printStackTrace();
		}, context);
	}
	
	private void displayVersion(CallbackContext context) {
		startScript("./versions.sh", Arrays.asList(RUNDIR + "/"), (e, c) -> {
			Scanner scanner = new Scanner(c.get(ExecuterData.class).getStdout());
			StringBuilder builder = new StringBuilder();
			
			builder.append("Versions:\n");
			
			while(scanner.hasNextLine())
				builder.append("- ").append(scanner.nextLine()).append("\n");
			
			ignore(TelegramApiException.class, () -> {
				c.get(Sender.class).reply(builder.toString(), c.get(Message.class));
			});
			
			scanner.close();
		}, (e, c) -> {
			e.printStackTrace();
		}, context);
	}
	
	@Override
	public void init() {
		commandHandler.add(new CommandInterpreter(this)
				.setName("versions")
				.setInfo("zeigt aktuell verfügbare Versionen an")
				.setCategory(category)
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setVisibility(Visibility.PUBLIC)
				.setAction(c -> {
					if (!NurseNoakes.BOT_ADMIN_USERNAMES.contains(c.getMessage().getFrom().getUserName())) {
						c.getSender().reply("Das dürfen nur Bot-Admins.", c.getMessage());
						return;
					}
					
					displayVersion(getContext(c.getMessage(), c.getSender()));
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("update")
				.setInfo("bringt den git Klon auf den neuesten Stand und wechselt auf den development branch")
				.setCategory(category)
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setVisibility(Visibility.PUBLIC)
				.setAction(c -> {
					if (!NurseNoakes.BOT_ADMIN_USERNAMES.contains(c.getMessage().getFrom().getUserName())) {
						c.getSender().reply("Das dürfen nur Bot-Admins.", c.getMessage());
						return;
					}
					
					update(getContext(c.getMessage(), c.getSender()));
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("build")
				.setInfo("compiliert die aktuelle Version")
				.setCategory(category)
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setVisibility(Visibility.PUBLIC)
				.setAction(c -> {
					if (!NurseNoakes.BOT_ADMIN_USERNAMES.contains(c.getMessage().getFrom().getUserName())) {
						c.getSender().reply("Das dürfen nur Bot-Admins.", c.getMessage());
						return;
					}
					
					build(getContext(c.getMessage(), c.getSender()));
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("deploy")
				.setInfo("deployt die angegebene Version")
				.setCategory(category)
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setVisibility(Visibility.PUBLIC)
				.setAction(c -> {
					if (!NurseNoakes.BOT_ADMIN_USERNAMES.contains(c.getMessage().getFrom().getUserName())) {
						c.getSender().reply("Das dürfen nur Bot-Admins.", c.getMessage());
						return;
					}
					
					String version = c.getParameter();
					
					deploy(getContext(c.getMessage(), c.getSender()), version);
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
