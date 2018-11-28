package asylum.nursebot.objects;


import org.telegram.telegrambots.meta.api.objects.Chat;

public class Locality {
	private int value = 0;
	
	public static final int USER = 1 << 1;
	public static final int GROUP = 1 << 2;
	public static final int SUPERGROUP = 1 << 3;

	public static final Locality USERS = new Locality(GROUP).or(USER);
	public static final Locality GROUPS = new Locality(GROUP).or(SUPERGROUP);
	public static final Locality EVERYWHERE = new Locality(GROUPS).or(USER);

	public Locality(Locality loc) {
		this.value = loc.value;
	}

	public Locality(int loc) {
		this.value = loc;
	}

	public Locality or(int loc) {
		value |= loc;
		return this;
	}
	
	public Locality and(int loc) {
		value &= loc;
		return this;
	}
	
	public boolean check(int loc) {
		return (value & loc) > 0;
	}
	
	public boolean check(Chat chat) {
		int loc = 0;
		if (chat.isSuperGroupChat())
			loc = SUPERGROUP;
		else if (chat.isGroupChat())
			loc = GROUP;
		else if (chat.isUserChat())
			loc = USER;
		
		return check(loc);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if ((value & USER) != 0)
			builder.append("U");
		if ((value & GROUP) != 0)
			builder.append("G");
		if ((value & SUPERGROUP) != 0)
			builder.append("S");
		
		return builder.toString();
	}
}
