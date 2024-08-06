package org.hibernate.benchmark.immutable;

import java.util.List;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Id;
import jakarta.persistence.Persistence;
import jakarta.persistence.Table;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
public class QueryImmutableEntity {

	protected EntityManagerFactory entityManagerFactory;
	protected EntityManager em;

	@Setup
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory("QueryImmutableEntity");

		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.createQuery("delete Fortune").executeUpdate();
		for (int i = 0; i < 1000; i++) {
			populateData(em, i);
		}
		em.getTransaction().commit();
		em.close();

		em = entityManagerFactory.createEntityManager();
		em.setFlushMode( FlushModeType.COMMIT );
		em.getTransaction().begin();
		queryFortune( null, null );
	}

	@TearDown
	public void destroy() {
		em.getTransaction().commit();
		em.close();
		entityManagerFactory.close();
	}

	@State(Scope.Thread)
	@AuxCounters(AuxCounters.Type.OPERATIONS)
	public static class EventCounters {
		public long queries;
	}

	@Benchmark
	public void single(Blackhole bh, EventCounters counters) {
		em.clear();
		queryFortune( bh, counters );
	}

	protected void queryFortune(Blackhole bh, EventCounters counters) {
		final List<Fortune> fortunes = em.createQuery( "from Fortune", Fortune.class ).getResultList();
		for ( Fortune fortune : fortunes ) {
			if ( bh != null ) {
				bh.consume( fortune );
			}
		}
		if ( counters != null ) {
			counters.queries += fortunes.size();
		}
	}

	public void populateData(EntityManager entityManager, int i) {
		final Fortune fortune = new Fortune();
		fortune.id = (long) i;
		fortune.name = "HTTP Definitive guide";
		entityManager.persist(fortune);
	}

	@Entity(name = "Fortune")
	@Table(name = "Fortune")
	@Immutable
	public static class Fortune {
		@Id
		public Long id;

		@Column
		public String name;

	}
	public static void main(String[] args) {
		QueryImmutableEntity jpaBenchmark = new QueryImmutableEntity();
		jpaBenchmark.setup();

		for ( int i = 0; i < 5; i++ ) {
			jpaBenchmark.single(null, null);
		}

		jpaBenchmark.destroy();
	}
}
