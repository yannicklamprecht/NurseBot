package asylum.nursebot.persistence.selfbuilding;

public class Column {
	private String name;
	private Type type;
	private boolean nullFlag = false;
	private boolean notNullFlag = false;
	private String defaultValue = null;
	private boolean autoincrement = false;
	private Key key = Key.NONE;
	private String comment = null;
	private ColumnFormat columnFormat = ColumnFormat.NA;
	private Storage storage = Storage.NA;
	private String referenceDefinition = null;
	
	public Column(String name, Type type) {
		this.name = name;
		this.type = type;
	}
	
	
	
	public Column setNullFlag(boolean nullFlag) {
		this.nullFlag = nullFlag;
		return this;
	}
	public Column setNotNullFlag(boolean notNullFlag) {
		this.notNullFlag = notNullFlag;
		return this;
	}
	public Column setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	public Column setAutoincrement(boolean autoincrement) {
		this.autoincrement = autoincrement;
		return this;
	}
	public Column setKey(Key key) {
		this.key = key;
		return this;
	}
	public Column setComment(String comment) {
		this.comment = comment;
		return this;
	}
	public Column setColumnFormat(ColumnFormat columnFormat) {
		this.columnFormat = columnFormat;
		return this;
	}
	public Column setStorage(Storage storage) {
		this.storage = storage;
		return this;
	}
	public Column setReferenceDefinition(String referenceDefinition) {
		this.referenceDefinition = referenceDefinition;
		return this;
	}



	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("`").append(name).append("` ");
		builder.append(type.toString()).append(" ");
		
		if (nullFlag) {
			builder.append("NULL ");
		} else if (notNullFlag) {
			builder.append("NOT NULL ");
		}
		
		if (defaultValue != null) {
			builder.append("DEFAULT ").append(defaultValue).append(" ");
		}
		
		if (autoincrement) {
			builder.append("AUTO_INCREMENT ");
		}
		
		builder.append(key.toString()).append(" ");
		
		if (comment != null) {
			builder.append("COMMENT '").append(comment).append("' ");
		}
		
		if (columnFormat != ColumnFormat.NA) {
			builder.append("COLUMN_FORMAT ").append(columnFormat.toString()).append(" ");
		}
		if (storage != Storage.NA) {
			builder.append("STORAGE ").append(storage.toString()).append(" ");
		}
		
		if (referenceDefinition != null) {
			builder.append(referenceDefinition).append(" ");
		}
		
		return builder.toString();
	}
}
