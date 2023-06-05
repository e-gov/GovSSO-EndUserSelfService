package ee.ria.govsso.enduserselfservice.configuration.thymeleaf;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

@RequiredArgsConstructor
public class PrettyDatesExpression {

    private static final String DATETIME_FORMAT_KEY = "format.datetime";

    private final MessageSource messageSource;

    public String dateTime(TemporalAccessor dateTime) {
        return getFormatter().format(dateTime);
    }

    public DateTimeFormatter getFormatter() {
        Locale locale = LocaleContextHolder.getLocale();
        String format = messageSource.getMessage(DATETIME_FORMAT_KEY, null, locale);
        return DateTimeFormatter.ofPattern(format, locale);
    }

}
