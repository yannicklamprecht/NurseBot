package asylum.nursebot.objects;

public interface Module {
	String getName();
	ModuleType getType();
	
	void init();
	void activate();
	void deactivate();
	void shutdown();
}
