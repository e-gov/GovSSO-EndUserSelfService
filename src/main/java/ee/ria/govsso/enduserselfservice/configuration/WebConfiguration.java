package ee.ria.govsso.enduserselfservice.configuration;

import ee.ria.govsso.enduserselfservice.configuration.util.SupportedLocaleContextResolver;
import lombok.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;
import java.util.Set;

@Configuration
public class WebConfiguration {

    public static final Locale ESTONIAN = new Locale("et");
    public static final Locale ENGLISH = new Locale("en");
    public static final Locale RUSSIAN = new Locale("ru");
    public static final Locale DEFAULT_LOCALE = ESTONIAN;

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setCookieName("__Host-LOCALE");
        resolver.setCookieSecure(true);
        resolver.setCookieMaxAge(365 * 24 * 60 * 60);

        // Setting default locale prevents CookieLocaleResolver from falling back to request.getLocale()
        resolver.setDefaultLocale(DEFAULT_LOCALE);

        return new SupportedLocaleContextResolver(resolver, Set.of(ESTONIAN, ENGLISH, RUSSIAN), DEFAULT_LOCALE);
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(DEFAULT_LOCALE);
        return messageSource;
    }

    @Bean
    public WebMvcConfigurer localeChangeInterceptorConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(localeChangeInterceptor());
            }

            public LocaleChangeInterceptor localeChangeInterceptor() {
                LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
                lci.setParamName("lang");
                return lci;
            }

        };
    }

}
