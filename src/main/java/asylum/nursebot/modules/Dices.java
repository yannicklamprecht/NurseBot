package asylum.nursebot.modules;

import asylum.nursebot.commands.CommandCategory;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.commands.CommandInterpreter;
import asylum.nursebot.loader.AutoModule;
import asylum.nursebot.modules.dices.D6;
import asylum.nursebot.modules.dices.Dice;
import asylum.nursebot.modules.dices.Distribution;
import asylum.nursebot.modules.dices.NormalDistribution;
import asylum.nursebot.objects.*;
import com.google.inject.Inject;

import java.util.Random;

@AutoModule(load=true)
public class Dices implements Module {
    @Inject
    private CommandHandler commandHandler;

    private CommandCategory category;

    public Dices() {
        category = new CommandCategory("Würfel");
    }

    @Override
    public String getName() {
        return "Dices";
    }

    @Override
    public ModuleType getType() {
        return new ModuleType()
                .set(ModuleType.COMMAND_MODULE);
    }

    @Override
    public void init() {
        commandHandler.add(new CommandInterpreter(this)
            .setName("dice")
            .setInfo("würfelt")
            .setPermission(Permission.ANY)
            .setVisibility(Visibility.PUBLIC)
            .setLocality(Locality.EVERYWHERE)
            .setCategory(category)
            .setAction(c -> {
                Dice dice = null;
                if (c.getParameter().isEmpty()) {
                    dice = new D6();
                } else {
                    c.getSender().reply("Das kann ich leider noch nicht.", c.getMessage());
                    return;
                }

                if (dice == null)
                    return;

		        dice.roll();

                c.getSender().reply(dice.display(), c.getMessage());
            }));

        commandHandler.add(new CommandInterpreter(this)
                .setName("distribution")
                .setInfo("macht was mit Verteilungen")
                .setPermission(Permission.ANY)
                .setVisibility(Visibility.PUBLIC)
                .setLocality(Locality.EVERYWHERE)
                .setCategory(category)
                .setAction(c -> {
                    Distribution distribution = null;
                    if (c.getParameter().isEmpty()) {
                        distribution = new NormalDistribution(0, 1);
                    } else {
                        c.getSender().reply("Das kann ich leider noch nicht.", c.getMessage());
                        return;
                    }

                    distribution.setRandom(new Random());

                    String string = distribution.getName() + ": " + distribution.getParameters() + "\n" +
                            "Result: " + distribution.generateValue();

                    c.getSender().reply(string, c.getMessage());
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
