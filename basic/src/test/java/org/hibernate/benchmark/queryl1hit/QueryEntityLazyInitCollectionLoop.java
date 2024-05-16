package org.hibernate.benchmark.queryl1hit;

import java.io.IOException;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Query an entity and initialize a lazy collection in a loop to hit the L1 cache with repeatedly.
 */
@State(Scope.Thread)
public class QueryEntityLazyInitCollectionLoop extends QueryEntityLazyInitCollectionBase {

	@Benchmark
	public void test() {
		final EntityManager em = entityManagerFactory.createEntityManager();
		em.setFlushMode( FlushModeType.COMMIT );
		em.getTransaction().begin();
		for ( int i = 0; i < 100_000; i++ ) {
			queryAuthors( em );
		}
		em.getTransaction().commit();
		em.close();
	}

	public static void main(String[] args) {
		QueryEntityLazyInitCollectionLoop jpaBenchmark = new QueryEntityLazyInitCollectionLoop();
		jpaBenchmark.setup();

		for ( int i = 0; i < 5; i++ ) {
			jpaBenchmark.test();
		}

		jpaBenchmark.destroy();
	}

	public static void main1(String[] args) throws RunnerException, IOException {
		if ( args.length == 0 ) {
			final Options opt = new OptionsBuilder()
					.include( ".*" + QueryEntityLazyInitCollectionLoop.class.getSimpleName() + ".*" )
					.warmupIterations( 3 )
					.warmupTime( TimeValue.seconds( 3 ) )
					.measurementIterations( 3 )
					.measurementTime( TimeValue.seconds( 5 ) )
					.threads( 1 )
					.addProfiler( "gc" )
					.forks( 2 )
					.build();
			new Runner( opt ).run();
		}
		else {
			Main.main( args );
		}
	}
}
