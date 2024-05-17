package org.hibernate.benchmark.queryl1hit;

import jakarta.persistence.*;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;

/**
 * Query an entity and initialize a lazy collection.
 */
@State(Scope.Thread)
public class QueryEntityLazyInitCollectionSingle extends QueryEntityLazyInitCollectionBase {

	@Benchmark
	public void test() {
		final EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		queryAuthors( em );
		em.getTransaction().commit();
		em.close();
	}

	public static void main1(String[] args) {
		QueryEntityLazyInitCollectionSingle jpaBenchmark = new QueryEntityLazyInitCollectionSingle();
		jpaBenchmark.setup();

		for ( int i = 0; i < 10_000; i++ ) {
			jpaBenchmark.test();
		}

		jpaBenchmark.destroy();
	}

	public static void main(String[] args) throws RunnerException, IOException {
		if ( args.length == 0 ) {
			final Options opt = new OptionsBuilder()
					.include( ".*" + QueryEntityLazyInitCollectionSingle.class.getSimpleName() + ".*" )
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
