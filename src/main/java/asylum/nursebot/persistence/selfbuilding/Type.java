package asylum.nursebot.persistence.selfbuilding;

public class Type {
	private DataType type;
	private Integer length = null;
	private Integer decimals = null;
	private boolean unsigned = false;
	private boolean zerofill = false;
	private String charset = null;
	private String collation = null;
	
	public Type(DataType type) {
		this.type = type;
	}
	
	public Type setLength(Integer length) {
		this.length = length;
		return this;
	}
	public Type setDecimals(Integer decimals) {
		this.decimals = decimals;
		return this;
	}
	public Type setUnsigned(boolean unsigned) {
		this.unsigned = unsigned;
		return this;
	}
	public Type setZerofill(boolean zerofill) {
		this.zerofill = zerofill;
		return this;
	}
	public Type setCharset(String charset) {
		this.charset = charset;
		return this;
	}
	public Type setCollation(String collation) {
		this.collation = collation;
		return this;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(type.toString()).append(" ");
		if (length != null) {
			builder.append("(").append(length);
			if (decimals != null) {
				builder.append(", ").append(decimals);
			}
			builder.append(") ");
		}
		if (unsigned) {
			builder.append("UNSIGNED ");
		}
		if (zerofill) {
			builder.append("ZEROFILL ");
		}
		if (charset != null) {
			builder.append("CHARACTER SET ").append(charset).append(" ");
		}
		if (collation != null) {
			builder.append("COLLATE ").append(collation).append(" ");
		}
		
		return builder.toString();
	}
}
