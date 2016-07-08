package uk.co.strangeskies.text.parsing;

import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

public class DateTimeParser<T> implements AbstractParser<T> {
	private final DateTimeFormatter format;
	private final Function<TemporalAccessor, T> accessorFunction;

	protected DateTimeParser(DateTimeFormatter format, Function<TemporalAccessor, T> accessorFunction) {
		this.format = format;
		this.accessorFunction = accessorFunction;
	}

	public static DateTimeParser<LocalDate> overIsoLocalDate() {
		return over(DateTimeFormatter.ISO_LOCAL_DATE, LocalDate::from);
	}

	public static DateTimeParser<LocalDate> over(DateTimeFormatter format) {
		return over(format, LocalDate::from);
	}

	public static <T> DateTimeParser<T> over(DateTimeFormatter format, Function<TemporalAccessor, T> accessorFunction) {
		return new DateTimeParser<>(format, accessorFunction);
	}

	@Override
	public ParseResult<T> parseSubstringImpl(ParseState currentState) {
		ParsePosition position = new ParsePosition(currentState.fromIndex());

		try {
			TemporalAccessor accessor = format.parse(currentState.literal(), position);

			return currentState.parseTo(position.getIndex(), s -> accessorFunction.apply(accessor));
		} catch (Exception e) {
			throw currentState.addException("Cannot parse temporal accessor",
					position.getErrorIndex() > 0 ? position.getErrorIndex() : position.getIndex(), e).getException();
		}
	}

	public static void main(String... args) {
		System.out.println(Parser.list(overIsoLocalDate(), ",").parse("4567-01-23,010-01-23"));
	}
}
