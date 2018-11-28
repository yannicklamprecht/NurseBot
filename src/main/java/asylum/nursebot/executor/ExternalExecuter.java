package asylum.nursebot.executor;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class ExternalExecuter implements Runnable {

	private ExitCallback exit;
	private ErrorCallback error;
	private CallbackContext context;
	
	private String program;
	private List<String> arguments;
	private File directory;
	
	private InputStream stdout;
	private OutputStream stdin;
	private InputStream stderr;
	
	public ExternalExecuter(String program, List<String> arguments, File directory, ExitCallback exit, ErrorCallback error, CallbackContext context) {
		
		this.program = program;
		if (arguments != null)
			this.arguments = arguments;
		else
			this.arguments = new LinkedList<>();
		this.exit = exit;
		this.directory = directory;
		this.context = context;
		
		this.exit = exit;
		this.error = error;
	}
	
	@Override
	public void run() {
		try {
			List<String> args = new LinkedList<>(arguments);
			args.add(0, program);
			
			ProcessBuilder builder = new ProcessBuilder(args);
			builder.directory(directory);
			Process process = builder.start();
			
			this.stdout = process.getInputStream();
			this.stderr = process.getErrorStream();
			this.stdin = process.getOutputStream();
			
			context.put(ExecuterData.class, new ExecuterData(stdout, stdin, stderr));
			
			int exitCode = process.waitFor();
			if (exit != null)
				exit.accept(new ExitCode(exitCode), context);
		} catch (Exception e) {
			error.accept(e, context);
		}
	}

	public InputStream getStdout() {
		return stdout;
	}

	public OutputStream getStdin() {
		return stdin;
	}

	public InputStream getStderr() {
		return stderr;
	}
}
