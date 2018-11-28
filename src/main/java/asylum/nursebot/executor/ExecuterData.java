package asylum.nursebot.executor;

import java.io.InputStream;
import java.io.OutputStream;

public class ExecuterData {

	private InputStream stdout;
	private InputStream stderr;
	private OutputStream stdin;
	
	public ExecuterData(InputStream stdout, OutputStream stdin, InputStream stderr) {
		this.stdout = stdout;
		this.stderr = stderr;
		this.stdin = stdin;
	}

	public InputStream getStdout() {
		return stdout;
	}

	public InputStream getStderr() {
		return stderr;
	}

	public OutputStream getStdin() {
		return stdin;
	}

}
