package asylum.nursebot.objects;

public enum Permission implements Comparable<Permission>{
	ANY, USER, ADMIN, OWNER;
	
	public int compare(Permission permission) {
		if (this == permission)
			return 0;
		if (permission == ANY)
			return 1;
		if (this == OWNER)
			return 1;
		if (this == USER)
			return -1;
		if (permission == USER)
			return 1;
		if (permission == OWNER)
			return -1;
		return 0;
	}
}
