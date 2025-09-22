package tech.aurasoftware.candoreco.configuration.serialization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields that should be ignored during serialization.
 * Fields marked with this annotation will not be included in the configuration
 * when the object is serialized to YAML.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Ignored {
}