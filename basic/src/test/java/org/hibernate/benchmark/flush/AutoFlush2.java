package org.hibernate.benchmark.flush;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Persistence;
import jakarta.persistence.Table;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 3, time = 5)
public class AutoFlush2 {

	protected EntityManagerFactory entityManagerFactory;
	protected EntityManager em;

	@Setup
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory("AutoFlush2");

		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.createQuery("delete Book").executeUpdate();
		em.createQuery("delete Author").executeUpdate();
		em.createQuery("delete AuthorDetails ").executeUpdate();
		for (int i = 0; i < 1000; i++) {
			populateData(em, i);
		}
		em.getTransaction().commit();
		em.close();
	}

	@TearDown
	public void destroy() {
		entityManagerFactory.close();
	}

	@State(Scope.Thread)
	@AuxCounters(AuxCounters.Type.OPERATIONS)
	public static class EventCounters {
		public long objects;
	}

	@Benchmark
	public void single(Blackhole bh, EventCounters counters) {
		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.setFlushMode( FlushModeType.COMMIT);
		try {
			query( bh, counters );
		}
		finally {
			em.getTransaction().commit();
			em.close();
		}
	}

	protected void query(Blackhole bh, EventCounters counters) {
		for (int i = 0; i < 1_000; i++) {
			final List<Author> authors = em.createQuery("from Author", Author.class).getResultList();
			for ( Author author : authors ) {
				int booksCount = author.books.size();
				if ( bh != null ) {
					bh.consume( author );
					bh.consume( booksCount );
				}
				if ( counters != null ) {
					counters.objects += booksCount;
				}
			}
			if ( counters != null ) {
				counters.objects += authors.size();
			}
		}
	}

	public void populateData(EntityManager entityManager, int i) {
		final Author author = new Author();
		author.authorId = i;
		author.name = "David Gourley";

		final AuthorDetails details = new AuthorDetails();
		details.detailsId = i;
		details.name = "Author Details";
		details.author = author;
		author.details = details;
		entityManager.persist(author);

		for (int j = 0; j < 5; j++) {
			final Book book = new Book();
			book.bookId = i * 5 + j;
			book.name = "HTTP Definitive guide " + book.bookId;
			book.author = author;
			entityManager.persist(book);
			author.books.add(book);
		}
	}

	public static void main(String[] args) {
		AutoFlush2 jpaBenchmark = new AutoFlush2();
		jpaBenchmark.setup();

		for ( int i = 0; i < 5; i++ ) {
			jpaBenchmark.single(null, null);
		}

		jpaBenchmark.destroy();
	}

	@Entity(name = "Author")
	public static class Author {

		@Id
		public Integer authorId;

		@Column
		public String name;

		@OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
		public List<Book> books = new ArrayList<>();

		@OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
		public AuthorDetails details;

	}

	@Entity(name = "AuthorDetails")
	@Table(name = "AuthorDetails")
	public static class AuthorDetails {
		@Id
		public Integer detailsId;

		@Column
		public String name;

		@OneToOne(fetch = FetchType.LAZY, mappedBy = "details", optional = false)
		public Author author;
	}

	@Entity(name = "Book")
	public static class Book {

		@Id
		public Integer bookId;

		@Column
		public String name;

		@ManyToOne(fetch = FetchType.LAZY, optional = false)
		@JoinColumn(name = "author_id", nullable = false)
		public Author author;
	}
}
