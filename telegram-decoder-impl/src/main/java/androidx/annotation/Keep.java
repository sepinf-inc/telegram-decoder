package androidx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that the annotated element should not be removed when
 * the code is minified at build time.
 *
 * <p>For example, this can be used to ensure that methods or classes
 * which are accessed via reflection are not removed.
 *
 * @see android.annotation.Keep
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface Keep {
}