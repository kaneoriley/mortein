package kaneoriley.mortein;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@SuppressWarnings("unused")
@Target({FIELD, METHOD})
@Retention(RUNTIME)
public @interface DebugLong {
    long value();

    boolean enabled() default true;
}
