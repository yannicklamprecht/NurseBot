package asylum.nursebot.modules;

import java.util.LinkedList;
import java.util.List;

import com.google.inject.Inject;

import asylum.nursebot.NurseNoakes;
import asylum.nursebot.commands.CommandCategory;
import asylum.nursebot.commands.CommandHandler;
import asylum.nursebot.commands.CommandInterpreter;
import asylum.nursebot.exceptions.NurseException;
import asylum.nursebot.exceptions.ParsingException;
import asylum.nursebot.loader.AutoModule;
import asylum.nursebot.loader.ModuleDependencies;
import asylum.nursebot.objects.Locality;
import asylum.nursebot.objects.Module;
import asylum.nursebot.objects.ModuleType;
import asylum.nursebot.objects.Permission;
import asylum.nursebot.objects.Visibility;
import asylum.nursebot.persistence.ModelManager;
import asylum.nursebot.persistence.modules.BusinessCardsCard;
import asylum.nursebot.persistence.modules.BusinessCardsEntry;
import asylum.nursebot.persistence.modules.BusinessCardsField;
import asylum.nursebot.utils.StringTools;
import org.javalite.activejdbc.Base;
import org.telegram.telegrambots.meta.api.objects.User;

@AutoModule(load=true)
public class BusinessCards implements Module {

	@Inject
	private CommandHandler commandHandler;
	
	@Inject
	private ModuleDependencies moduleDependencies;

	@Inject
	private NurseNoakes nurse;
	
	private CommandCategory category;
	
	@Override
	public String getName() {
		return "Business Cards";
	}

	@Override
	public ModuleType getType() {
		return new ModuleType().set(ModuleType.COMMAND_MODULE);
	}

	public BusinessCards() {
		ModelManager.build(BusinessCardsCard.class);
		ModelManager.build(BusinessCardsField.class);
		ModelManager.build(BusinessCardsEntry.class);
		
		category = new CommandCategory("Visitenkarten");
	}
	
	private List<BusinessCardsField> getDefinedFields() {
		return BusinessCardsField.findAll();
	}
	
	private void modifyFields(BusinessCardsCard card, List<String> args) throws ParsingException {
		List<BusinessCardsField> definedFields = getDefinedFields();
		
		List<BusinessCardsField> used = new LinkedList<>();
		
		for (String arg : args) {
			int position = arg.indexOf("=");
			if (position < 0)
				throw new ParsingException("Fehlendes = in einer Felddefinition");
			String key = arg.substring(0, position);
			String value = arg.substring(position + 1);
			value = String.join(" ", StringTools.tokenize(value));
			
			BusinessCardsField foundField = null;
			for (BusinessCardsField field : definedFields) {
				if (field.getName().equals(key)) {
					foundField = field;
					break;
				}
			}
			if (foundField == null)
				throw new ParsingException("Unbekannter Feldname. Die verfügbaren Felden können mit /showcardfields angezeigt werden. Neue Felder können mit /createcardfield erstellt werden.");
			
			if (used.contains(foundField))
				throw new ParsingException("Feld " + foundField.getName() + " ist mehrfach definiert");
			
			used.add(foundField);
			
			BusinessCardsEntry entry = new BusinessCardsEntry();
			entry.setValue(value);
			card.add(entry);
			foundField.add(entry);
			entry.saveIt();
		}
	}
	
	@Override
	public void init() {
		commandHandler.add(new CommandInterpreter(this)
				.setName("createcard")
				.setInfo("erstellt eine neue Visitenkarte")
				.setLocality(Locality.USERS)
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setCategory(category)
				.setAction(c -> {
					String help = "Synopsis: /createcard CARDNAME [public] {FIELD=VALUE ...}";
					List<String> args = StringTools.tokenize(c.getParameter());
					if (args.size() < 2) {
						c.getSender().send(help);
						return;
					}
					try {
						Base.openTransaction();
						
						String cardname = args.get(0);
					
						List<?> tmp = BusinessCardsCard.getByName(cardname, c.getMessage().getFrom().getId().intValue());
						if (tmp != null && !tmp.isEmpty()) {
							throw new ParsingException("Dieser Name existiert bereits.\nExistierende Karten können mit /deletecard gelöscht, und mit /modifycard verändert werden.");
						}
						
						args = args.subList(1, args.size());
						boolean isPublic = args.get(0).toLowerCase().equals("public");
						if (isPublic)
							args = args.subList(1, args.size());
						
						BusinessCardsCard card = new BusinessCardsCard();
						card.setName(cardname);
						card.setUserid(c.getMessage().getFrom().getId());
						card.setPublic(isPublic);
						card.saveIt();
						
						modifyFields(card, args);
						
						c.getSender().send("Die Visitenkarte " + cardname + " wurde erfolgreich angelegt.");
						
						Base.commitTransaction();
					} catch (NurseException e) {
						Base.rollbackTransaction();
						c.getSender().send(help + "\n\n" + e.getMessage());
					}
				}));
			commandHandler.add(new CommandInterpreter(this)
					.setName("changecard")
					.setInfo("verändert eine Visitenkarte")
					.setLocality(Locality.USERS)
					.setVisibility(Visibility.PUBLIC)
					.setPermission(Permission.ANY)
					.setCategory(category)
					.setAction(c -> {
						String help = "Synopsis: /changecard CARDNAME [public] {FIELD=VALUE ...}\n\nAlle Felder werden gelöscht, und die neuen Felder hinzugefügt.";
						List<String> args = StringTools.tokenize(c.getParameter());
						if (args.size() < 2) {
							c.getSender().send(help);
							return;
						}
						try {
							Base.openTransaction();
							
							String cardname = args.get(0);
						
							List<BusinessCardsCard> tmp = BusinessCardsCard.getByName(cardname, c.getMessage().getFrom().getId().intValue());
							if (tmp == null || tmp.isEmpty()) {
								throw new ParsingException("Diese Karte existiert nicht.");
							}
							
							args = args.subList(1, args.size());
							boolean isPublic = args.get(0).toLowerCase().equals("public");
							if (isPublic)
								args = args.subList(1, args.size());
							
							BusinessCardsCard card = tmp.get(0);
							card.setPublic(isPublic);
							card.saveIt();
							
							List<BusinessCardsEntry> entries = card.getAll(BusinessCardsEntry.class);
							entries.forEach(e -> e.delete());
							
							modifyFields(card, args);
							
							c.getSender().send("Die Visitenkarte " + cardname + " wurde erfolgreich verändert.");
							
							Base.commitTransaction();
						} catch (NurseException e) {
							Base.rollbackTransaction();
							c.getSender().send(help + "\n\n" + e.getMessage());
						}
					}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("deletecard")
				.setInfo("löscht eine Visitenkarte")
				.setLocality(Locality.USERS)
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setCategory(category)
				.setAction(c -> {
					String help = "Synopsis: /deletecard CARDNAME";
					List<String> args = StringTools.tokenize(c.getParameter());
					if (args.size() != 1) {
						c.getSender().send(help);
						return;
					}
					
					try {
						String cardname = args.get(0);
						
						List<BusinessCardsCard> tmp = BusinessCardsCard.getByName(cardname, c.getMessage().getFrom().getId().intValue());
						if (tmp == null || tmp.isEmpty()) {
							throw new ParsingException("Es wurde keine Karte mit diesem Namen gefunden.");
						}
						BusinessCardsCard card = tmp.get(0);
						
						card.getAll(BusinessCardsEntry.class).forEach(e -> e.delete());
						card.delete();
						
						c.getSender().send("Visitenkarte " + cardname + " wurde erfolgreich gelösch.");
					} catch(NurseException e) {
						c.getSender().send(help + "\n\n" + e.getMessage());
					}
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("showcard")
				.setInfo("zeigt eine Visitenkarte")
				.setLocality(Locality.EVERYWHERE)
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setCategory(category)
				.setAction(c -> {
					String help = "Synopsis: /showcard CARDNAME";
					List<String> args = StringTools.tokenize(c.getParameter());
					if (args.size() < 1) {
						c.getSender().send(help);
						return;
					}
					StringBuilder builder = new StringBuilder();
					try {
						String cardname = args.get(0);
						User user = c.getMessage().getFrom();
					
						builder.append("Visitenkarte ").append(StringTools.makeMention(user)).append(" ").append(cardname).append(":\n\n");
						
						List<BusinessCardsCard> tmp = BusinessCardsCard.getByName(cardname, c.getMessage().getFrom().getId().intValue());
						if (tmp == null || tmp.isEmpty()) {
							throw new ParsingException("Diese Visitenkarte existiert nicht.\nNeue Karten können mit /createcard hinzugefügt werden.");
						}
						BusinessCardsCard card = tmp.get(0);
						
						List<BusinessCardsEntry> list = card.getAll(BusinessCardsEntry.class);
						for (BusinessCardsEntry entry : list) {
							/*List<BusinessCardsField> fields = entry.getAll(BusinessCardsField.class);
							if ((fields == null) || fields.isEmpty()) {
								throw new WhatTheFuckException("Field object is inconsistent.");
							}
							BusinessCardsField field = fields.get(0);*/
							BusinessCardsField field = entry.parent(BusinessCardsField.class);
							builder.append(field.getLabel()).append(": ");
							builder.append(entry.getValue()).append("\n");
						}
						
						c.getSender().send(builder.toString());
					} catch (NurseException e) {
						c.getSender().send(help + "\n\n" + e.getMessage());
					}
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("givecard")
				.setInfo("gibt einem User eine Visitenkarte")
				.setLocality(Locality.EVERYWHERE)
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setCategory(category)
				.setAction(c -> {
					String help = "Synopsis: /givecard CARDNAME USERNAME";
					List<String> args = StringTools.tokenize(c.getParameter());
					if (args.size() < 1) {
						c.getSender().send(help);
						return;
					}
					StringBuilder builder = new StringBuilder();
					try {
						String cardname = args.get(0);
						
						PrivateNotifier notifier = moduleDependencies.get(PrivateNotifier.class);
						if (notifier == null) {
							c.getSender().reply("Tut mir leid, aber das Private Notifier Modul ist leider deaktivier.", c.getMessage());
							return;
						}

						List<User> users = null;

						UserLookup lookup = moduleDependencies.get(UserLookup.class);
						if (lookup != null) {
							users = lookup.getMentions(c.getMessage());
						}

						if (users == null) {
							users = new LinkedList<>();
							if (c.getMessage().getReplyToMessage() != null) {
								users.add(c.getMessage().getReplyToMessage().getFrom());
							}
						}

						if (users.isEmpty()) {
							throw new ParsingException("Es wurde kein User angegeben.");
						}
						
						builder.append("Visitenkarte ").append(StringTools.makeMention(c.getMessage().getFrom())).append(" ").append(cardname).append(":\n\n");
						
						List<BusinessCardsCard> tmp = BusinessCardsCard.getByName(cardname, c.getMessage().getFrom().getId().intValue());
						if (tmp == null || tmp.isEmpty()) {
							throw new ParsingException("Diese Visitenkarte existiert nicht.\nNeue Karten können mit /createcard hinzugefügt werden.");
						}
						BusinessCardsCard card = tmp.get(0);
						
						List<BusinessCardsEntry> list = card.getAll(BusinessCardsEntry.class);
						for (BusinessCardsEntry entry : list) {
							BusinessCardsField field = entry.parent(BusinessCardsField.class);
							builder.append(field.getLabel()).append(": ");
							builder.append(entry.getValue()).append("\n");
						}
						
						String message = builder.toString();
						
						builder = new StringBuilder();

						for (User user : users) {
							if (notifier.hasPrivateChat(user)) {
								notifier.send(c.getSender(), c.getMessage().getChat(), user, message);
								builder.append("Die Visitenkarte wurde dem User " + StringTools.makeMention(user) + " erfolgreich gesendet.\n");
							} else {
								builder.append("Der User " + StringTools.makeMention(user) + " hat keine privaten Notifications aktiviert.\n");
							}
						}

						c.getSender().reply(builder.toString(), c.getMessage());
					} catch (NurseException e) {
						c.getSender().send(help + "\n\n" + e.getMessage());
					}
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("showcards")
				.setInfo("zeigt eine Liste aller eigenen Visitenkarten")
				.setLocality(Locality.EVERYWHERE)
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setCategory(category)
				.setAction(c -> {
					String help = "Synopsis: /showcards";
					List<String> args = StringTools.tokenize(c.getParameter());
					if (args.size() != 0) {
						c.getSender().send(help);
						return;
					}
					
					List<BusinessCardsCard> tmp = BusinessCardsCard.getByUserid(c.getMessage().getFrom().getId().intValue());
					if (tmp == null || tmp.isEmpty()) {
						c.getSender().send("Keine Visitenkarten gefunden.");
						return;
					}
					
					StringBuilder builder = new StringBuilder();
					
					for (BusinessCardsCard card : tmp) {
						builder.append("- ");
						builder.append(card.getName());
						if (card.isPublic()) {
							builder.append(" (public)");
						}
						builder.append("\n");
					}
					
					c.getSender().send(builder.toString());
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("createcardfield")
				.setInfo("erstellt ein neues Visitenkartenfeld")
				.setLocality(Locality.USERS)
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setCategory(category)
				.setAction(c -> {
					String help = "Synopsis: /createcardfield NAME LABEL";
					List<String> args = StringTools.tokenize(c.getParameter());
					if (args.size() != 2) {
						c.getSender().send(help);
						return;
					}
					
					String name = args.get(0);
					String label = args.get(1);
					
					try {
						Base.openTransaction();
						
						BusinessCardsField field = BusinessCardsField.getByName(name);
						if (field != null) {
							if (field.isApproved())
								throw new NurseException("Dieses Feld existiert bereits.");
							else
								throw new NurseException("Dieses Feld wurde bereits vorgeschlagen.");
						}
						
						field = new BusinessCardsField();
						field.setName(name);
						field.setLabel(label);
						field.saveIt();
						
						Base.commitTransaction();
						
						c.getSender().send("Das Feld " + name + " wurde erfolgreich vorgeschlagen.");
					} catch (NurseException e) {
						Base.rollbackTransaction();
						c.getSender().send(help + "\n\n" + e.getMessage());
					}
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("showcardfields")
				.setInfo("zeigt alle Visitenkartenfelder an")
				.setLocality(Locality.USERS)
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.ANY)
				.setCategory(category)
				.setAction(c -> {
					String help = "Synopsis: /showcardfields";
					List<String> args = StringTools.tokenize(c.getParameter());
					if (args.size() != 0) {
						c.getSender().send(help);
						return;
					}
					
					StringBuilder builder = new StringBuilder();
					
					List<BusinessCardsField> fields = BusinessCardsField.findAll();
					for (BusinessCardsField field : fields) {
						builder.append("- ");
						builder.append(field.getName());
						builder.append(" (\"").append(field.getLabel()).append("\"");
						if (!field.isApproved()) {
							builder.append(", ").append(StringTools.makeItalic("vorgeschlagen")).append(" ");
						}
						builder.append(")\n");
					}
					if (builder.length() == 0) {
						c.getSender().send("Es gibt noch keine Felder. : (");
					} else {
						c.getSender().send(builder.toString(), true);
					}
				}));
		
		commandHandler.add(new CommandInterpreter(this)
				.setName("managecardfield")
				.setInfo("verwaltet Visitenkartenfelder")
				.setLocality(Locality.USERS)
				.setVisibility(Visibility.PUBLIC)
				.setPermission(Permission.USER)
				.setCategory(category)
				.setAction(c -> {
					if (!NurseNoakes.BOT_ADMIN_USERNAMES.contains(c.getMessage().getFrom().getUserName().toLowerCase())) {
						c.getSender().send("Du darfst das leider nicht.");
						return;
					}
					
					String help = "Synopsis: /managecardfield NAME delete|{rename NAME}|{set [+|-approved] [LABEL]}";
					List<String> args = StringTools.tokenize(c.getParameter());
					if (args.size() < 2) {
						c.getSender().send(help);
						return;
					}
					
					String name = args.get(0);
					String command = args.get(1);
					
					Base.openTransaction();
					
					try {
						BusinessCardsField field = BusinessCardsField.getByName(name);
						if (field == null)
							throw new NurseException("Es wurde kein Feld mit diesem Namen gefunden.");
						
						if ("delete".equals(command)) {
							field.getAll(BusinessCardsEntry.class).forEach(e -> e.delete());
							field.delete();
							c.getSender().send("Das Feld " + name + " wurde erfogreich mit allen Abhängigkiten gelöscht.");
						} else if ("set".equals(command)) {
							args = args.subList(2, args.size());
							for (String arg : args) {
								if ("+approved".equals(arg)) {
									field.setApproved(true);
								} else if ("-approved".equals(arg)) {
									field.setApproved(false);
								} else {
									field.setLabel(arg);
								}
							}
							field.saveIt();
							
							c.getSender().send("Das Feld " + name + " wurde erfolgreich verändert.");
						} else if ("rename".equals(command)) {
							if (args.size() != 3) {
								throw new NurseException("Die Anzahl der Parameter passt nicht.");
							}
							field.setName(args.get(2));
							field.saveIt();
							
							c.getSender().send("Das Feld " + name + " wurde erfolgreich in " + args.get(2) + " umgenannt.");
						} else {
							throw new NurseException("Unbekannter Befehl.");
						}
						
						Base.commitTransaction();
					} catch (NurseException e) {
						Base.rollbackTransaction();
						c.getSender().send(help + "\n\n" + e.getMessage());
						return;
					}
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
