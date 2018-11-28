package asylum.nursebot.utils;


import asylum.nursebot.NurseNoakes;
import asylum.nursebot.objects.Permission;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SecurityChecker {
	private NurseNoakes nurse;
	
	public SecurityChecker(NurseNoakes nurse) {
		this.nurse = nurse;
	}
	
	public Permission getRole(Long chatid, User user) throws TelegramApiException {
		GetChatMember getChatMember = new GetChatMember();
		getChatMember.setChatId(chatid);
		getChatMember.setUserId(user.getId());
		
		ChatMember member = nurse.execute(getChatMember);
		
		String status = member.getStatus();
		
		switch (status) {
		case "creator":
			return Permission.OWNER;
		case "administrator":
			return Permission.ADMIN;
		case "member":
			return Permission.USER;
		default: // left, kicked
			System.out.println("User has status " + status);
			return Permission.USER;
		}	
	}
	
	public boolean checkRights(Long chatid, User user, Permission permission) throws TelegramApiException {
		return getRole(chatid, user).compare(permission) >= 0;
	}
}
