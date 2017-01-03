package org.seamcat.objectutils;

import org.apache.log4j.Logger;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.generic.SeamcatInvocationHandler;
import org.seamcat.model.types.Description;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeepEquals {
	private static final Logger LOG = Logger.getLogger(DeepEquals.class);

	private static final Set<Class<?>> basicTypes;
	static {
		basicTypes = new HashSet<Class<?>>();
		basicTypes.add( Double.class);
		basicTypes.add( String.class );
		basicTypes.add( Integer.class );
		basicTypes.add( Float.class );
		basicTypes.add( Boolean.class );
		basicTypes.add( Long.class );
	}

	public static boolean equals( Object a, Object b ) {
		return new DeepEquals().compare(a,b);
	}

	public boolean compare( Object a, Object b ) {
		//compared = new ArrayList<Pair>();
		compared = new HashSet<Pair>();
		return deepCompare(a, b);
	}

	protected boolean deepCompare(Object a, Object b) {
		if (a == null && b != null || a != null && b == null) {
			return false;
		}
		if ( a == null ) return true;
        if ( a instanceof Description && b instanceof Description ) {
            // special case. Description has an implementation
            return ((Description) a).name().equals( ((Description) b).name())
                    && ((Description) a).description().equals(((Description) b).description());

        }
        if ( a instanceof Proxy && b instanceof Proxy ) {
            SeamcatInvocationHandler ihA = ProxyHelper.getHandler( a );
            SeamcatInvocationHandler ihB = ProxyHelper.getHandler( b );

            return deepCompare(ihA.getValues(), ihB.getValues() );
        }
        if ( a.getClass() != b.getClass() ) {
			return false;
		}
		if ( a == b ) return true;
		if ( basicType( a) ) {
			return a.equals(b);
		}


		if ( Iterable.class.isAssignableFrom(a.getClass())) {
			if ( needCompare( a, b )) {
				return new IterablesEquals( this, ((Iterable<?>)a).iterator(), ((Iterable<?>)b).iterator() ).equals();
			}
		} else if ( Map.class.isAssignableFrom(a.getClass())) {
			if ( needCompare( a, b )) {
				return new MapEquals(this, (Map<?,?>) a, (Map<?,?>) b).mapEquals();
			}
		} else if ( a.getClass().isArray() ) {
			if ( needCompare( a, b )) {
				return new ArrayEquals( this, a, b ).equals();
			}
		}

		if ( needCompare( a, b ) ) {
			return classCompare( a.getClass(), a, b );
		}
		return true;
	}

	class Pair {
		private Object a;
		private Object b;
		public Pair( Object a, Object b ) {
			this.a = a;
			this.b = b;
		}
		@Override
		public boolean equals(Object o) {
			if ( o == null ) return false;
			if ( !(o instanceof  Pair) ) return false;
			Pair p = (Pair) o;
			return p.hashCode() == this.hashCode();
			//return a == p.a && b == p.b;
		}

		@Override
		public int hashCode() {
			int ia = System.identityHashCode(a);
			int ib = System.identityHashCode(b);
			if ( ia < ib ) {
				return ia * 31 + ib * 37;
			} else {
				return ib * 31 + ia * 37;
			}
		}
	}

	private Set<Pair> compared;
	//private List<Pair> compared;

	protected boolean classCompare( Class<?> clazz, Object a, Object b ) {
		for (Field field : clazz.getDeclaredFields()) {
			if (Modifier.isTransient( field.getModifiers() ) ) continue;
            field.setAccessible( true );
			try {
				Object aField = field.get(a);
				Object bField = field.get(b);
				if  (!deepCompare(aField, bField) ) {
					LOG.info( "Field '"+field.getName()+"' in class "+clazz.getSimpleName()+" differs (" + aField + " != " + bField + ")" );
					return false;
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
        if ( clazz.getSuperclass() == null ) return true;
		return classCompare(clazz.getSuperclass(), a, b);
	}
	
	private boolean needCompare( Object a, Object b ) {
		Pair pair = new Pair(a, b);
		if (!compared.contains( pair )) {
			compared.add( pair );
			return true;
		}
		return false;
	}

	private static boolean basicType( Object a) {
		return basicTypes.contains( a.getClass() );
	}

}
