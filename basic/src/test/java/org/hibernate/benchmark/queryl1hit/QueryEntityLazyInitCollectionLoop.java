package org.hibernate.benchmark.queryl1hit;

import java.io.IOException;

import jakarta.persistence.FlushModeType;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Query an entity and initialize a lazy collection in a loop to hit the L1 cache with repeatedly.
 */
@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 1)
public class QueryEntityLazyInitCollectionLoop extends QueryEntityLazyInitCollectionBase {

	@Override
	public void setup() {
		super.setup();
		em.setFlushMode( FlushModeType.COMMIT );
		em.getTransaction().begin();
		queryAuthors( null, null );
	}

	@Override
	public void destroy() {
		em.getTransaction().commit();
		super.destroy();
	}

	@Benchmark
	public void single(Blackhole bh, EventCounters counters) {
		em.clear();
		queryAuthors( bh, counters );
	}

	@Benchmark
	public void loop(Blackhole bh, EventCounters counters) {
		queryAuthors( bh, counters );
	}

	public static void main(String[] args) {
		QueryEntityLazyInitCollectionLoop jpaBenchmark = new QueryEntityLazyInitCollectionLoop();
		jpaBenchmark.setup();

		for ( int i = 0; i < 5; i++ ) {
			jpaBenchmark.loop(null, null);
		}

		jpaBenchmark.destroy();
	}

}
