package org.seamcat.model.plugin.builder;

/**
 * @deprecated use Factory.prototype(), Factory.when(), and Factory.build() instead
 */
@Deprecated
public interface Builder<T> {

    /**
     * used to specify return values for the to-be created
     * instance of T.
     * <p>
     *     <emph>Example:</emph> If T has a method <code>double temperature()</code>, instruct
     *     the builder of the return value for <code>temperature</code> like
     *     this: <code>builder.returnValue( 16.3 ).temperature();</code><br>
     *         After <code>build()</code> is invoked, the instance returned will return 16.3 when calling
     *         <code>t.temperature();</code>.
     * </p>
     * @param value the value to be returned by the subsequent method invoked on the instance T
     * @return Tracker instance of T that will react to which method will be invoked
     */
    T returnValue(Object value);

    /**
     * method that returns an immutable instance of <code>T</code>
     * @return immutable instance of T
     */
    T build();
}

