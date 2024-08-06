package org.hibernate.benchmark.queryl2hit;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Persistence;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 3, time = 5)
public class QueryCacheEntityWithAssociation {

	private static final int BOOKS = 5;

	protected EntityManagerFactory entityManagerFactory;
	protected EntityManager em;

	@Param({"SHALLOW", "FULL"})
	public String cacheLayout;

	@Setup
	public void setup() {
		Map<String, Object> settings = Map.of( "hibernate.cache.query_cache_layout", cacheLayout );
		entityManagerFactory = Persistence.createEntityManagerFactory( "QueryCacheEntityWithAssociation", settings );

		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.createQuery("delete Book").executeUpdate();
		em.createQuery("delete Author ").executeUpdate();
		em.createQuery("delete AuthorDetails ").executeUpdate();
		for (int i = 0; i < 1000; i++) {
			populateData( em, i, BOOKS );
		}
		em.getTransaction().commit();
		em.close();
	}

	@TearDown
	public void destroy() {
		entityManagerFactory.close();
	}

	@State(Scope.Thread)
	@AuxCounters(AuxCounters.Type.EVENTS)
	public static class EventCounters {
		public long books;
	}

	@Benchmark
	public void single(Blackhole bh, EventCounters counters) {
		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		final Author author = em.createQuery( "from Author", Author.class ).setMaxResults( 1 ).getSingleResult();
		for (int i = 0; i < 1000; i++) {
			final List<Book> books = em.createQuery( "from Book b where b.author = :author", Book.class )
					.setParameter( "author", author )
					.setHint( "org.hibernate.cacheable", "true" )
					.getResultList();
			for ( Book book : books ) {
				if ( bh != null ) {
					bh.consume( book );
				}
			}
			if ( counters != null ) {
				counters.books += books.size();
			}
		}
		em.getTransaction().commit();
		em.close();
	}

	public void populateData(EntityManager entityManager, int i, int books) {
		final Author author = new Author();
		author.authorId = (long) i;
		author.name = "David Gourley";

		final AuthorDetails details = new AuthorDetails();
		details.detailsId = (long) i;
		details.name = "Author Details";
		details.author = author;
		author.details = details;
		for ( int j = 0; j < books; j++ ) {
			final Book book = new Book();
			book.bookId = j + (long) i * books;
			book.name = "HTTP Definitive guide " + j;
			book.author = author;
			author.books.add( book );
		}
		entityManager.persist( author );
	}

	@Entity(name = "Author")
	public static class Author {
		@Id
		public Long authorId;
		@Column
		public String name;
		@OneToMany(mappedBy = "author", cascade = CascadeType.PERSIST)
		public Set<Book> books = new HashSet<>();

		@OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
		public AuthorDetails details;
	}

	@Entity(name = "AuthorDetails")
	public static class AuthorDetails {
		@Id
		public Long detailsId;

		@Column
		public String name;

		@OneToOne(fetch = FetchType.LAZY, mappedBy = "details", optional = false)
		public Author author;
	}

	@Entity(name = "Book")
	@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
	public static class Book {
		@Id
		public Long bookId;
		@Column
		public String name;
		@ManyToOne(fetch = FetchType.LAZY, optional = false)
		public Author author;
	}

	public static void main(String[] args) {
		QueryCacheEntityWithAssociation jpaBenchmark = new QueryCacheEntityWithAssociation();
		jpaBenchmark.cacheLayout = "FULL";
		jpaBenchmark.setup();

		for ( int i = 0; i < 5; i++ ) {
			jpaBenchmark.single(null, null);
		}

		jpaBenchmark.destroy();
	}
}
