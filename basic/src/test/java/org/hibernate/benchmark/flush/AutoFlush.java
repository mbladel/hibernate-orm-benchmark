package org.hibernate.benchmark.flush;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Persistence;
import jakarta.persistence.Temporal;
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
public class AutoFlush {

	protected EntityManagerFactory entityManagerFactory;
	protected EntityManager em;

	@Setup
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory("AutoFlush");

		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.createQuery("delete Foo").executeUpdate();
		for (int i = 0; i < 50000; i++) {
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
		try {
			query( bh, counters );
		}
		finally {
			em.getTransaction().commit();
			em.close();
		}
	}

	protected void query(Blackhole bh, EventCounters counters) {
		// empty result, empty session
		final List<Object> results1 = em.createQuery( "select b from Bar b" ).getResultList();
		for ( Object o : results1 ) {
			if ( bh != null ) {
				bh.consume( o );
			}
		}
		if ( counters != null ) {
			counters.objects += results1.size();
		}
		// many results
		final List<Object> results2 = em.createQuery( "select b from Foo b" ).getResultList();
		for ( Object o : results2 ) {
			if ( bh != null ) {
				bh.consume( o );
			}
		}
		if ( counters != null ) {
			counters.objects += results2.size();
		}
		// empty result, full session
		final List<Object> results3 = em.createQuery( "select b from Bar b" ).getResultList();
		for ( Object o : results3 ) {
			if ( bh != null ) {
				bh.consume( o );
			}
		}
		if ( counters != null ) {
			counters.objects += results3.size();
		}
	}

	public void populateData(EntityManager entityManager, int i) {
		Foo foo = new Foo();
		foo.setId(i);
		entityManager.persist(foo);
	}

	public static void main(String[] args) {
		AutoFlush jpaBenchmark = new AutoFlush();
		jpaBenchmark.setup();

		for ( int i = 0; i < 5; i++ ) {
			jpaBenchmark.single(null, null);
		}

		jpaBenchmark.destroy();
	}

	@Entity(name = "Foo")
	public static class Foo {

		@Id
		private Integer id;

		private Embed embed1;

		@AttributeOverride(name = "date", column = @jakarta.persistence.Column(name = "date1"))
		private Embed embed2;

		@AttributeOverride(name = "date", column = @jakarta.persistence.Column(name = "date2"))
		private Embed embed3;

		private String val1;

		private String val2;

		private String val3;

		private String val4;

		private String val5;

		private String val6;

		private String val7;

		private String val8;

		private String val9;

		private String val10;

		private String val11;

		private String val12;

		@OneToMany(mappedBy = "foo1")
		private Collection<Bar> bars1 = new HashSet<>();

		@OneToMany(mappedBy = "foo2")
		private Collection<Bar> bars2 = new HashSet<>();

		@OneToMany(mappedBy = "foo3")
		private Collection<Bar> bars3 = new HashSet<>();

		@OneToMany(mappedBy = "foo4")
		private Collection<Bar> bars4 = new HashSet<>();

		@OneToMany(mappedBy = "foo5")
		private Collection<Bar> bars5 = new HashSet<>();

		@OneToMany(mappedBy = "foo6")
		private Collection<Bar> bars6 = new HashSet<>();

		@OneToMany(mappedBy = "foo7")
		private Collection<Bar> bars7 = new HashSet<>();

		@OneToMany(mappedBy = "foo8")
		private Collection<Bar> bars8 = new HashSet<>();

		@OneToMany(mappedBy = "foo9")
		private Collection<Bar> bars9 = new HashSet<>();

		@OneToMany(mappedBy = "foo10")
		private Collection<Bar> bars10 = new HashSet<>();

		@OneToMany(mappedBy = "foo11")
		private Collection<Bar> bars11 = new HashSet<>();

		@OneToMany(mappedBy = "foo12")
		private Collection<Bar> bars12 = new HashSet<>();

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Collection<Bar> getBars1() {
			return bars1;
		}

		public Collection<Bar> getBars2() {
			return bars2;
		}

		public Collection<Bar> getBars3() {
			return bars3;
		}

		public Collection<Bar> getBars4() {
			return bars4;
		}

		public Collection<Bar> getBars5() {
			return bars5;
		}

		public Collection<Bar> getBars6() {
			return bars6;
		}

		public Collection<Bar> getBars7() {
			return bars7;
		}

		public Collection<Bar> getBars8() {
			return bars8;
		}

		public Collection<Bar> getBars9() {
			return bars9;
		}

		public Collection<Bar> getBars10() {
			return bars10;
		}

		public Collection<Bar> getBars11() {
			return bars11;
		}

		public Collection<Bar> getBars12() {
			return bars12;
		}

		public Embed getEmbed1() {
			return embed1;
		}

		public void setEmbed1(Embed embed1) {
			this.embed1 = embed1;
		}

		public Embed getEmbed2() {
			return embed2;
		}

		public void setEmbed2(Embed embed2) {
			this.embed2 = embed2;
		}

		public Embed getEmbed3() {
			return embed3;
		}

		public void setEmbed3(Embed embed3) {
			this.embed3 = embed3;
		}

		public String getVal1() {
			return val1;
		}

		public void setVal1(String val1) {
			this.val1 = val1;
		}

		public String getVal2() {
			return val2;
		}

		public void setVal2(String val2) {
			this.val2 = val2;
		}

		public String getVal3() {
			return val3;
		}

		public void setVal3(String val3) {
			this.val3 = val3;
		}

		public String getVal4() {
			return val4;
		}

		public void setVal4(String val4) {
			this.val4 = val4;
		}

		public String getVal5() {
			return val5;
		}

		public void setVal5(String val5) {
			this.val5 = val5;
		}

		public String getVal6() {
			return val6;
		}

		public void setVal6(String val6) {
			this.val6 = val6;
		}

		public String getVal7() {
			return val7;
		}

		public void setVal7(String val7) {
			this.val7 = val7;
		}

		public String getVal8() {
			return val8;
		}

		public void setVal8(String val8) {
			this.val8 = val8;
		}

		public String getVal9() {
			return val9;
		}

		public void setVal9(String val9) {
			this.val9 = val9;
		}

		public String getVal10() {
			return val10;
		}

		public void setVal10(String val10) {
			this.val10 = val10;
		}

		public String getVal11() {
			return val11;
		}

		public void setVal11(String val11) {
			this.val11 = val11;
		}

		public String getVal12() {
			return val12;
		}

		public void setVal12(String val12) {
			this.val12 = val12;
		}
	}
	@Entity(name = "Bar")
	public static class Bar {

		@Id
		private Integer id;

		private String name;

		@ManyToOne
		private Foo foo1;

		@ManyToOne
		private Foo foo2;

		@ManyToOne
		private Foo foo3;

		@ManyToOne
		private Foo foo4;

		@ManyToOne
		private Foo foo5;

		@ManyToOne
		private Foo foo6;

		@ManyToOne
		private Foo foo7;

		@ManyToOne
		private Foo foo8;

		@ManyToOne
		private Foo foo9;

		@ManyToOne
		private Foo foo10;

		@ManyToOne
		private Foo foo11;

		@ManyToOne
		private Foo foo12;

		public Bar(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setFoo(int i, Foo foo) {
			switch (i) {
				case 1:
					foo1 = foo;
					break;
				case 2:
					foo2 = foo;
					break;
				case 3:
					foo3 = foo;
					break;
				case 4:
					foo4 = foo;
					break;
				case 5:
					foo5 = foo;
					break;
				case 6:
					foo6 = foo;
					break;
				case 7:
					foo7 = foo;
					break;
				case 8:
					foo8 = foo;
					break;
				case 9:
					foo9 = foo;
					break;
				case 10:
					foo10 = foo;
					break;
				case 11:
					foo11 = foo;
					break;
				case 12:
					foo12 = foo;
					break;
			}
		}
	}

	@Embeddable
	public static class Embed {

		@Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
		private Date date;

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}
	}
}
