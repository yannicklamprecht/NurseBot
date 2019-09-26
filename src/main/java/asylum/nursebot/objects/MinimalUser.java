package asylum.nursebot.objects;

import asylum.nursebot.persistence.modules.UserLookupEntry;
import org.telegram.telegrambots.meta.api.objects.User;


public class MinimalUser extends User {
	private static final long serialVersionUID = 2210503080750916556L;

	private String name;
	private int id;
	private String firstname;
	private String lastname;

	public MinimalUser(UserLookupEntry entry) {
		this.id = entry.getUserid();
		this.name = entry.getUsername();
		this.firstname = entry.getFirstname();
		this.lastname = entry.getSurname();
	}

	public MinimalUser(Integer id, String name, String firstname, String lastname) {
		this.id = id;
		this.name = name;
		this.firstname = firstname;
		this.lastname = lastname;
	}


	@Override
	public String getUserName() {
		return name;
	}

	@Override
	public String getFirstName() {
		return firstname;
	}

	@Override
	public String getLastName() {
		return lastname;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public Boolean getBot() {
		return null;
	}

	@Override
	public String getLanguageCode() {
		return null;
	}
}
