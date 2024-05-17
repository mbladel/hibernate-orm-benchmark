package org.hibernate.benchmark.queryl1hit;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Persistence;
import jakarta.persistence.Table;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
public class QueryEntityLazyInitCollectionBase {

	protected EntityManagerFactory entityManagerFactory;
	protected EntityManager em;

	@Setup
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory("bench1");

		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.createQuery("delete Book").executeUpdate();
		em.createQuery("delete Author").executeUpdate();
		em.createQuery("delete AuthorDetails").executeUpdate();
		for (int i = 0; i < 1000; i++) {
			populateData(em);
		}
		em.getTransaction().commit();
		em.close();

		em = entityManagerFactory.createEntityManager();
	}

	@TearDown
	public void destroy() {
		em.close();
		entityManagerFactory.close();
	}

	@State(Scope.Thread)
	@AuxCounters(AuxCounters.Type.OPERATIONS)
	public static class EventCounters {
		public long queries;
	}

	protected void queryAuthors(Blackhole bh, EventCounters counters) {
		final List<Author> authors = em.createQuery( "from Author", Author.class ).getResultList();
		for ( Author author : authors ) {
			boolean empty = author.books.isEmpty();
			if ( bh != null ) {
				bh.consume( author );
				bh.consume( empty );
			}
		}
		if ( counters != null ) {
			counters.queries += authors.size();
		}
	}

	private static void assertFalse(boolean empty) {
		if (empty) {
			throw new AssertionError();
		}
	}

	public void test() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void populateData(EntityManager entityManager) {
		final Book book = new Book();
		book.name = "HTTP Definitive guide";

		final Author author = new Author();
		author.name = "David Gourley";

		final AuthorDetails details = new AuthorDetails();
		details.name = "Author Details";
		details.author = author;
		author.details = details;

		author.books.add(book);
		book.author = author;

		entityManager.persist(author);
		entityManager.persist(book);
	}

	@Entity(name = "Author")
	@Table(name = "Author")
	public static class Author {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		public Long authorId;

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
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		public Long detailsId;

		@Column
		public String name;

		@OneToOne(fetch = FetchType.LAZY, mappedBy = "details", optional = false)
		public Author author;
	}

	@Entity(name = "Book")
	@Table(name = "Book")
	public static class Book {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		public Long bookId;

		@Column
		public String name;

		@ManyToOne(fetch = FetchType.LAZY, optional = false)
		@JoinColumn(name = "author_id", nullable = false)
		public Author author;
	}
}
