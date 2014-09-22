package uk.co.strangeskies.utilities;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MultiException extends RuntimeException {
	private static final long serialVersionUID = -5252945414846966968L;

	private final List<Throwable> causes;

	public MultiException(String message, Collection<? extends Throwable> causes) {
		super(message, causes.stream().findFirst().get());
		this.causes = new ArrayList<>(causes);
	}

	public MultiException(Collection<? extends Throwable> causes) {
		super(causes.stream().findFirst().get());
		this.causes = new ArrayList<>(causes);
	}

	public MultiException(String message, Throwable... causes) {
		this(Arrays.asList(causes));
	}

	public MultiException(Throwable... causes) {
		this(Arrays.asList(causes));
	}

	public MultiException(String message) {
		super(message);
		causes = Collections.emptyList();
	}

	public MultiException() {
		super();
		causes = Collections.emptyList();
	}

	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	@Override
	public void printStackTrace(PrintStream output) {
		printStackTrace(new PrintWriter(output));
	}

	@Override
	public void printStackTrace(PrintWriter output) {
		super.printStackTrace(output);
		for (int i = 2; i <= causes.size(); i++) {
			output.println("Caused by (" + i + " of " + causes.size() + "):");
			causes.get(i).printStackTrace(output);
			output.flush();
		}
	}
}
