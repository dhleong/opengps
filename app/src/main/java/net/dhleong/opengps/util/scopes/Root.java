package net.dhleong.opengps.util.scopes;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * @author dhleong
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Root {
}

