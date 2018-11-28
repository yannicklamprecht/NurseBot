package asylum.nursebot.modules;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import asylum.nursebot.commands.CommandCategory;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.commands.CommandInterpreter;
import asylum.nursebot.loader.AutoDependency;
import asylum.nursebot.loader.AutoModule;
import asylum.nursebot.objects.Locality;
import asylum.nursebot.objects.Module;
import asylum.nursebot.objects.ModuleType;
import asylum.nursebot.objects.Permission;
import asylum.nursebot.objects.Visibility;
import asylum.nursebot.persistence.ModelManager;
import asylum.nursebot.persistence.modules.UserLookupEntry;
import asylum.nursebot.semantics.SemanticInterpreter;
import asylum.nursebot.semantics.SemanticsHandler;
import asylum.nursebot.semantics.WakeWord;
import asylum.nursebot.semantics.WakeWordType;
import asylum.nursebot.utils.StringTools;
import org.javalite.activejdbc.Base;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.User;

@AutoModule(load=true)
@AutoDependency
public class UserLookup implements Module {

	class MinimalUser extends User {
		private static final long serialVersionUID = 2210503080750916556L;
		
		private String name;
		private int id;
		
		public MinimalUser(int id, String name) {
			this.name = name;
			this.id = id;
		}
		
		@Override
		public String getUserName() {
			if (name.startsWith("@"))
				return name.substring(1);
			return null;
		}
		
		@Override
		public String getFirstName() {
			return name;
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
	
	@Inject
	private SemanticsHandler semanticsHandler;
	
	@Inject
	private CommandHandler commandHandler;
	
	private CommandCategory category;
	
	@Override
	public String getName() {
		return "User Lookup";
	}

	@Override
	public ModuleType getType() {
		return new ModuleType()
				.set(ModuleType.SEMANTIC_MODULE)
				.set(ModuleType.COMMAND_MODULE)
				.set(ModuleType.DEPENDENCY_MODULE);
	}

	public UserLookup() {
		category = new CommandCategory("User Lookup");
		
		ModelManager.build(UserLookupEntry.class);
	}
	
	@Override
	public void init() {
		semanticsHandler.add(new SemanticInterpreter(this)
				.addWakeWord(new WakeWord(null, WakeWordType.ANY_MESSAGE))
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setAction(c -> {
					
					User user = c.getMessage().getFrom();
					if (user.getUserName() == null)
						return;
					
					Base.openTransaction();
					UserLookupEntry entry = getUser(user.getId());
					if (entry == null)
						addUser(user);
					else if (!(entry.getUsername().equals(user.getUserName())))
						updateUser(entry, user);
					Base.commitTransaction();
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("id")
				.setInfo("zeigt die UserID für einen Benutzernamen an")
				.setLocality(Locality.EVERYWHERE)
				.setPermission(Permission.ANY)
				.setVisibility(Visibility.PRIVATE)
				.setCategory(category)
				.setAction(c -> {
					User user;
					
					if (c.getMessage().isReply()) {
						user = c.getMessage().getReplyToMessage().getFrom();
					} else {
						List<User> users = getMentions(c.getMessage());
						if (users.size() == 0) {
							user = c.getMessage().getFrom();	
						} else {
							user = users.get(0);
						}
					}
					
					c.getSender().reply("Der User " + StringTools.makeMention(user) + " hat die ID " + user.getId() + ".", c.getMessage(), true);
					
				}));
	}

	private void updateUser(UserLookupEntry entry, User user) {
		entry.setUsername(user.getUserName());
		entry.saveIt();
	}

	private void addUser(User user) {
		UserLookupEntry entry = new UserLookupEntry();
		entry.setUserid(user.getId());
		entry.setUsername(user.getUserName());
		entry.saveIt();
	}

	private UserLookupEntry getUser(int id) {
		return UserLookupEntry.getByUserid(id);
	}

	public String getUsername(int id) {
		UserLookupEntry entry = getUser(id);
		if (entry == null)
			return null;
		return entry.getUsername();
	}
	
	public Integer getUserId(String username) {
		if (username.startsWith("@"))
			username = username.substring(1);
		
		UserLookupEntry entry = UserLookupEntry.getByUsername(username);
		if (entry == null)
			return null;
		return (Integer) entry.getUserid();
	}
	
	public List<Integer> getMentionIds(Message message) {
		List<Integer> list = getUsernameMentionIds(message);
		list.addAll(getNonUsernameMentionIds(message));
		return list;
	}
	
	public List<User> getMentions(Message message) {
		List<User> list = getUsernameMentions(message);
		list.addAll(getNonUsernameMentions(message));
		return list;
	}
	
	private List<User> getUsernameMentions(Message message) {
		List<User> list = new LinkedList<>();
		
		for (MessageEntity entity : message.getEntities()) {
			if (!entity.getType().equals("mention"))
				continue;
			
			Integer user = getUserId(entity.getText());
			if (user != null)
				list.add(new MinimalUser(user, entity.getText()));
		}
		
		return list;
	}
	
	private List<Integer> getUsernameMentionIds(Message message) {
		List<Integer> list = new LinkedList<>();
		
		for (MessageEntity entity : message.getEntities()) {
			if (!entity.getType().equals("mention"))
				continue;
			
			Integer user = getUserId(entity.getText());
			if (user != null)
				list.add(user);
		}
		
		return list;
	}
	
	private List<User> getNonUsernameMentions(Message message) {
		List<User> list = new LinkedList<>();
		
		for (MessageEntity entity : message.getEntities()) {
			if (!entity.getType().equals("text_mention"))
				continue;
			list.add(entity.getUser());
		}
		
		return list;
	}
	
	private List<Integer> getNonUsernameMentionIds(Message message) {
		return getNonUsernameMentions(message).stream().map(u -> u.getId()).collect(Collectors.toList());
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
