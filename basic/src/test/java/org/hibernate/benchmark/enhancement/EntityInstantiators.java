package org.hibernate.benchmark.enhancement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.bytecode.enhance.spi.EnhancementContext;
import org.hibernate.bytecode.enhance.spi.Enhancer;
import org.hibernate.bytecode.internal.bytebuddy.BytecodeProviderImpl;
import org.hibernate.bytecode.spi.BasicProxyFactory;
import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.bytecode.spi.ProxyFactoryFactory;
import org.hibernate.bytecode.spi.ReflectionOptimizer;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.internal.EntityInstantiatorPojoOptimized;
import org.hibernate.metamodel.internal.EntityInstantiatorPojoStandard;
import org.hibernate.metamodel.spi.EntityInstantiator;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.type.CompositeType;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import static org.hibernate.benchmark.enhancement.EnhancementUtils.buildEnhancerClassLoader;

/**
 * @author Marco Belladelli
 */
@State(Scope.Thread)
public class EntityInstantiators {

	public enum Instantiation {
		STANDARD, OPTIMIZED
	}

	public enum Morphism {
		MONO, BI, TRI, QUAD;

		int types() {
			return switch ( this ) {
				case MONO -> 1;
				case BI -> 2;
				case TRI -> 3;
				case QUAD -> 4;
			};
		}
	}

	@Param
	private Instantiation instantiation;

	@Param
	private Morphism morphism;

	// WHen true, we make JIT compile all Morphism versions of the method but then run only 1 type
	@Param({ "false", "true" })
	private boolean polluteAtWarmup;

	@Param({ "100" })
	private int count;

	private SessionFactory sessionFactory;
	private Session session;

	int nextEntityTypeId;
	int[] entityTypes;

	@Setup
	public void setup(Blackhole bh) {
		int types = morphism.types();
		if ( types == 1 && polluteAtWarmup ) {
			// this is fairly useless
			throw new IllegalStateException( "Cannot pollute with monomorphic types" );
		}
		switch ( instantiation ) {
			case OPTIMIZED -> sessionFactory = getSessionFactory(
					new InstantiationOnlyBytecodeProvider(),
					EntityInstantiatorPojoOptimized.class,
					types
			);
			case STANDARD -> sessionFactory = getSessionFactory(
					new org.hibernate.bytecode.internal.none.BytecodeProviderImpl(),
					EntityInstantiatorPojoStandard.class,
					types
			);
		}
		populateData( sessionFactory, count, types );
		session = sessionFactory.openSession();
		session.getTransaction().begin();

		nextEntityTypeId = 0;
		entityTypes = new int[types];
		for ( int i = 0; i < types; i++ ) {
			entityTypes[i] = i;
		}
		if ( polluteAtWarmup ) {
			// this is ensuring that every each queryFortuneX method is called enough to be fully compiled
			for ( int i = 0; i < 10_000; i++ ) {
				switch ( types ) {
					case 4:
						queryFortune3( session, bh );
					case 3:
						queryFortune2( session, bh );
					case 2:
						queryFortune1( session, bh );
					case 1:
						queryFortune0( session, bh );
				}
			}
			// force to make it monomorphic now
			entityTypes = new int[] { 0 };
			nextEntityTypeId = 0;
		}
	}

	private int nextEntityTypeId() {
		var entityTypes = this.entityTypes;
		if ( nextEntityTypeId >= entityTypes.length ) {
			nextEntityTypeId = 1;
			return entityTypes[0];
		}
		else {
			return entityTypes[nextEntityTypeId++];
		}
	}

	private void populateData(SessionFactory sf, int count, int types) {
		if ( types == 0 || types > 4 ) {
			throw new IllegalArgumentException( "Invalid types" );
		}
		final SessionImplementor session = (SessionImplementor) sf.openSession();
		session.getTransaction().begin();
		switch ( types ) {
			case 4:
				for ( int i = 0; i < count; i++ ) {
					session.persist( instantiate( Fortune3.class, i, session ) );
				}
			case 3:
				for ( int i = 0; i < count; i++ ) {
					session.persist( instantiate( Fortune2.class, i, session ) );
				}
			case 2:
				for ( int i = 0; i < count; i++ ) {
					session.persist( instantiate( Fortune1.class, i, session ) );
				}
			case 1:
				for ( int i = 0; i < count; i++ ) {
					session.persist( instantiate( Fortune0.class, i, session ) );
				}
		}
		session.getTransaction().commit();
		session.close();
	}

	private Object instantiate(Class<?> clazz, int id, SessionImplementor session) {
		final SessionFactoryImplementor sf = session.getSessionFactory();
		final EntityPersister entityDescriptor = sf.getMappingMetamodel().getEntityDescriptor( clazz );
		final Object instance = entityDescriptor.getRepresentationStrategy().getInstantiator().instantiate( sf );
		entityDescriptor.setIdentifier( instance, id, session );
		return instance;
	}

	protected SessionFactory getSessionFactory(
			BytecodeProvider bytecodeProvider, Class<?> expectedInstantiatorClass, int types) {
		final List<Class<?>> classes = new ArrayList<>();
		switch ( types ) {
			case 4:
				classes.add( Fortune3.class );
			case 3:
				classes.add( Fortune2.class );
			case 2:
				classes.add( Fortune1.class );
			case 1:
				classes.add( Fortune0.class );
		}

		final Set<String> classNames = classes.stream().map( Class::getName ).collect( Collectors.toSet() );
		final ClassLoader enhancerClassLoader = buildEnhancerClassLoader(
				classNames
		);
		final BootstrapServiceRegistryBuilder bsrb = new BootstrapServiceRegistryBuilder()
				.applyClassLoader( enhancerClassLoader );
		final Configuration config = new Configuration( bsrb.build() );
		for ( String className : classNames ) {
			try {
				config.addAnnotatedClass( enhancerClassLoader.loadClass( className ) );
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException( e );
			}
		}

		final StandardServiceRegistryBuilder srb = config.getStandardServiceRegistryBuilder();
		srb.applySetting( AvailableSettings.SHOW_SQL, false )
				.applySetting(
						AvailableSettings.LOG_SESSION_METRICS,
						false
				)
				.applySetting( AvailableSettings.DIALECT, "org.hibernate.dialect.H2Dialect" )
				.applySetting( AvailableSettings.JAKARTA_JDBC_DRIVER, "org.h2.Driver" )
				.applySetting( AvailableSettings.JAKARTA_JDBC_URL, "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1" )
				.applySetting( AvailableSettings.JAKARTA_JDBC_USER, "sa" )
				.applySetting( AvailableSettings.HBM2DDL_AUTO, "create-drop" );

		// force no runtime bytecode-enhancement
		srb.addService( BytecodeProvider.class, bytecodeProvider );

		final SessionFactoryImplementor sf = (SessionFactoryImplementor) config.buildSessionFactory( srb.build() );
		final EntityInstantiator[] instantiators = new EntityInstantiator[types];
		switch ( types ) {
			case 4:
				instantiators[3] = sf.getMappingMetamodel()
						.getEntityDescriptor( Fortune3.class )
						.getRepresentationStrategy()
						.getInstantiator();
			case 3:
				instantiators[2] = sf.getMappingMetamodel()
						.getEntityDescriptor( Fortune2.class )
						.getRepresentationStrategy()
						.getInstantiator();
			case 2:
				instantiators[1] = sf.getMappingMetamodel()
						.getEntityDescriptor( Fortune1.class )
						.getRepresentationStrategy()
						.getInstantiator();
			case 1:
				instantiators[0] = sf.getMappingMetamodel()
						.getEntityDescriptor( Fortune0.class )
						.getRepresentationStrategy()
						.getInstantiator();
		}
		boolean found = false;
		for ( EntityInstantiator instantiator : instantiators ) {
			found |= expectedInstantiatorClass.isInstance( instantiator );
		}
		if ( !found ) {
			throw new AssertionFailure( "Expected instantiator not found" );
		}
		return sf;
	}

	@TearDown
	public void destroy() {
		session.getTransaction().commit();
		session.close();
	}

	@Benchmark
	public int query(Blackhole bh) {
		int nextEntityTypeId = nextEntityTypeId();
		final int count;
		switch ( nextEntityTypeId ) {
			case 0:
				count = queryFortune0( session, bh );
				break;
			case 1:
				count = queryFortune1( session, bh );
				break;
			case 2:
				count = queryFortune2( session, bh );
				break;
			case 3:
				count = queryFortune3( session, bh );
				break;
			default:
				throw new AssertionError( "it shouldn't happen!" );
		}
		return count;
	}

	@CompilerControl(CompilerControl.Mode.DONT_INLINE)
	private static int queryFortune2(Session session, Blackhole bh) {
		final List<Object> results = session.createQuery( "from Fortune2" ).getResultList();
		int count = 0;
		for ( Object fortune : results ) {
			if ( bh != null ) {
				bh.consume( fortune );
			}
			count++;
		}
		session.clear();
		return count;
	}

	@CompilerControl(CompilerControl.Mode.DONT_INLINE)
	private static int queryFortune3(Session session, Blackhole bh) {
		final List<Object> results = session.createQuery( "from Fortune3" ).getResultList();
		int count = 0;
		for ( Object fortune : results ) {
			if ( bh != null ) {
				bh.consume( fortune );
			}
			count++;
		}
		session.clear();
		return count;
	}

	@CompilerControl(CompilerControl.Mode.DONT_INLINE)
	private static int queryFortune1(Session session, Blackhole bh) {
		final List<Object> results = session.createQuery( "from Fortune1" ).getResultList();
		int count = 0;
		for ( Object fortune : results ) {
			if ( bh != null ) {
				bh.consume( fortune );
			}
			count++;
		}
		session.clear();
		return count;
	}

	@CompilerControl(CompilerControl.Mode.DONT_INLINE)
	private static int queryFortune0(Session session, Blackhole bh) {
		final List<Object> results = session.createQuery( "from Fortune0" ).getResultList();
		int count = 0;
		for ( Object fortune0 : results ) {
			if ( bh != null ) {
				bh.consume( fortune0 );
			}
			count++;
		}
		session.clear();
		return count;
	}

	@Entity(name = "Fortune0")
	static class Fortune0 {
		@Id
		public Integer id;

		public Fortune0() {
		}

		public Fortune0(Integer id) {
			this.id = id;
		}
	}

	@Entity(name = "Fortune1")
	static class Fortune1 {
		@Id
		public Integer id;

		public Fortune1() {
		}

		public Fortune1(Integer id) {
			this.id = id;
		}
	}

	@Entity(name = "Fortune2")
	static class Fortune2 {
		@Id
		public Integer id;

		public Fortune2() {
		}

		public Fortune2(Integer id) {
			this.id = id;
		}
	}

	@Entity(name = "Fortune3")
	static class Fortune3 {
		@Id
		public Integer id;

		public Fortune3() {
		}

		public Fortune3(Integer id) {
			this.id = id;
		}
	}

	/**
	 * {@link BytecodeProvider} which provides only instantiation optimizers
	 */
	static class InstantiationOnlyBytecodeProvider implements BytecodeProvider {
		private BytecodeProviderImpl delegate = new BytecodeProviderImpl();

		@Override
		public ProxyFactoryFactory getProxyFactoryFactory() {
			return new NoProxyFactoryFactory();
		}

		@SuppressWarnings("removal")
		@Override
		public ReflectionOptimizer getReflectionOptimizer(
				Class clazz, String[] getterNames, String[] setterNames, Class[] types) {
			return null;
		}

		@Override
		public ReflectionOptimizer getReflectionOptimizer(
				Class<?> clazz, Map<String, PropertyAccess> propertyAccessMap) {
			final ReflectionOptimizer original = delegate.getReflectionOptimizer( clazz, propertyAccessMap );
			return new ReflectionOptimizer() {
				@Override
				public InstantiationOptimizer getInstantiationOptimizer() {
					return original.getInstantiationOptimizer();
				}

				@Override
				public AccessOptimizer getAccessOptimizer() {
					return null;
				}
			};
		}

		@Override
		public Enhancer getEnhancer(EnhancementContext enhancementContext) {
			return null;
		}
	}

	/**
	 * Implementation of {@link ReflectionOptimizer.InstantiationOptimizer}
	 * which reflects the generated bytecode in both Hibernate and Quarkus
	 */
	static class Fortune0Instantiator implements ReflectionOptimizer.InstantiationOptimizer {
		@Override
		public Object newInstance() {
			return new Fortune0();
		}
	}

	static class Fortune1Instantiator implements ReflectionOptimizer.InstantiationOptimizer {
		@Override
		public Object newInstance() {
			return new Fortune1();
		}
	}

	static class Fortune2Instantiator implements ReflectionOptimizer.InstantiationOptimizer {
		@Override
		public Object newInstance() {
			return new Fortune2();
		}
	}

	static class Fortune3Instantiator implements ReflectionOptimizer.InstantiationOptimizer {
		@Override
		public Object newInstance() {
			return new Fortune3();
		}
	}

	static class NoProxyFactoryFactory implements ProxyFactoryFactory {
		@Override
		public ProxyFactory buildProxyFactory(SessionFactoryImplementor sessionFactory) {
			return new ProxyFactory() {
				@Override
				public void postInstantiate(
						String entityName,
						Class<?> persistentClass,
						Set<Class<?>> interfaces,
						Method getIdentifierMethod,
						Method setIdentifierMethod,
						CompositeType componentIdType) throws HibernateException {
				}

				@Override
				public HibernateProxy getProxy(Object id, SharedSessionContractImplementor session)
						throws HibernateException {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public BasicProxyFactory buildBasicProxyFactory(Class superClassOrInterface) {
			return new BasicProxyFactory() {
				@Override
				public Object getProxy() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}

	public static void main(String[] args) {
		final EntityInstantiators benchmark = new EntityInstantiators();
		benchmark.count = 100;
		benchmark.morphism = Morphism.QUAD;
		benchmark.polluteAtWarmup = false;
		benchmark.instantiation = Instantiation.OPTIMIZED;

		benchmark.setup( null );

		benchmark.query( null );

		benchmark.destroy();
	}
}
