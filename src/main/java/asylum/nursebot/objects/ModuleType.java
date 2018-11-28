package asylum.nursebot.objects;

public class ModuleType {
	public static final int COMMAND_MODULE = 1 << 0;
	public static final int SEMANTIC_MODULE = 1 << 1;
	public static final int DEPENDENCY_MODULE = 1 << 2;
	public static final int TEST_MODULE = 1 << 3;
	public static final int META_MODULE = 1 << 4;
	
	private int value = 0;
	
	public ModuleType set(int flag) {
		this.value |= flag;
		return this;
	}
	
	public boolean is(int flag) {
		return ((this.value & flag) != 0);
	}
	
	private char getTypeChar(int position) {
		switch (position) {
		case 0:
			return 'C';
		case 1:
			return 'S';
		case 2:
			return 'D';
		case 3:
			return 'T';
		case 4:
			return 'M';
		default:
			return '?';
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; 1 << i <= value; i++) {
			if ((1 << i & value) != 0)
				builder.append(getTypeChar(i));
		}
		
		return builder.toString();
	}
}
