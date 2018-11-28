package asylum.nursebot.loader;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;

import asylum.nursebot.NurseNoakes;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.objects.Module;
import asylum.nursebot.semantics.SemanticsHandler;
import org.reflections.Reflections;

public class ModuleLoader {
	private List<Provider> providers;
	
	private List<Class<? extends Module>> dependencyClasses;
	
	private ModuleDependencies dependencies;
	
	public ModuleLoader(NurseNoakes nurse, CommandHandler commandHandler, SemanticsHandler semanticsHandler) {
		this.providers = new LinkedList<>();
		this.dependencyClasses = new LinkedList<>();
		this.dependencies = new ModuleDependencies();
		
		this.providers.add(new BaseProvider(nurse, commandHandler, semanticsHandler, dependencies));
	}
	
	@SuppressWarnings("unchecked")
	public void loadDependencies() {
		Reflections reflections = new Reflections("asylum.nursebot");
		Set<Class<?>> list = reflections.getTypesAnnotatedWith(AutoDependency.class);
		for (Class<?> clazz : list) {
			if (Module.class.isAssignableFrom(clazz)) {
				dependencyClasses.add((Class<? extends Module>) clazz);
			}
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	public void loadModules(ModuleHandler handler) {
		Injector injector = Guice.createInjector(providers);
		
		Reflections reflections = new Reflections("asylum.nursebot");
		Set<Class<?>> list = reflections.getTypesAnnotatedWith(AutoModule.class);
		for (Class<?> clazz : list) {
			if (!Module.class.isAssignableFrom(clazz))
				throw new RuntimeException(clazz.getCanonicalName() + " is annotated but not a module.");
			AutoModule annotation = clazz.getAnnotation(AutoModule.class);
			if (annotation == null)
				throw new RuntimeException("HOW?! " + clazz.getCanonicalName());
			if (annotation.load()) {
				Module module = (Module) injector.getInstance(clazz);
				handler.handle(module);
				
				if (dependencyClasses.contains(clazz)) {
					System.out.println("Adding " + clazz.getCanonicalName() + " as dependency.");
					dependencies.put((Class<? extends Module>) clazz, module);
				}
			}
		}
	}
}
