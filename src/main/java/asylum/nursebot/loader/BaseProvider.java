package asylum.nursebot.loader;

import asylum.nursebot.NurseNoakes;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.semantics.SemanticsHandler;

public class BaseProvider extends Provider {
	
	private NurseNoakes nurse;
	private CommandHandler commandHandler;
	private SemanticsHandler semanticsHandler;
	private ModuleDependencies dependencies;

	public BaseProvider(NurseNoakes nurse, CommandHandler commandHandler, SemanticsHandler semanticsHandler, ModuleDependencies dependencies) {
		this.nurse = nurse;
		this.commandHandler = commandHandler;
		this.semanticsHandler = semanticsHandler;
		this.dependencies = dependencies;
	}

	@Override
	protected void configure() {
		bind(NurseNoakes.class).toInstance(nurse);
		bind(CommandHandler.class).toInstance(commandHandler);
		bind(SemanticsHandler.class).toInstance(semanticsHandler);
		bind(ModuleDependencies.class).toInstance(dependencies);
	}

}
