package org.hibernate.benchmark.queryl1hit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 2)
public class QueryEntityQueryReferringEntityAll extends QueryEntityQueryReferringEntityBase {

	@Override
	public void setup() {
		super.setup();
		em.getTransaction().begin();
	}

	@Override
	public void destroy() {
		em.getTransaction().commit();
		super.destroy();
	}

	@Benchmark
	public void includeQueryAccess(Blackhole bh, EventCounters counters) {
		queryEmployees( true, bh, counters );
	}

	@Benchmark
	public void noQueryAccess(Blackhole bh, EventCounters counters) {
		queryEmployees( false, bh, counters );
	}

	public static void main(String[] args) {
		QueryEntityQueryReferringEntityAll jpaBenchmark = new QueryEntityQueryReferringEntityAll();
		jpaBenchmark.setup();

		for ( int i = 0; i < 10; i++ ) {
			jpaBenchmark.includeQueryAccess(null, null);
		}

		jpaBenchmark.destroy();
	}

}
