package org.hibernate.benchmark.onetomanyfetch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Persistence;
import jakarta.persistence.Table;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
public class QueryOneToManyFetch {

	private static final boolean IS_ORM_5 = GenerationType.values().length == 4;
	private static final String ORDER_QUERY = ( IS_ORM_5 ? "select distinct o " : "") + "from Order o join fetch o.lines";

	protected EntityManagerFactory entityManagerFactory;
	protected EntityManager em;

	@Param({"1", "50"})
	public int orderLines;

	@Setup
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory("QueryOneToManyFetch");

		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.createQuery("delete OrderLine").executeUpdate();
		em.createQuery("delete Order").executeUpdate();
		for (int i = 0; i < 100; i++) {
			populateData(em, i, orderLines);
		}
		em.getTransaction().commit();
		em.close();

		em = entityManagerFactory.createEntityManager();
		em.setFlushMode( FlushModeType.COMMIT );
		em.getTransaction().begin();
		queryOrders( null, null );
	}

	@TearDown
	public void destroy() {
		em.getTransaction().commit();
		em.close();
		entityManagerFactory.close();
	}

	@State(Scope.Thread)
	@AuxCounters(AuxCounters.Type.EVENTS)
	public static class EventCounters {
		public long orderLines;
	}

	@Benchmark
	public void single(Blackhole bh, EventCounters counters) {
		em.clear();
		queryOrders( bh, counters );
	}

	protected void queryOrders(Blackhole bh, EventCounters counters) {
		final var query = em.createQuery( ORDER_QUERY, Order.class );
		if ( IS_ORM_5 ) {
			query.setHint( "hibernate.query.passDistinctThrough", false );
		}
		final List<Order> orders = query.getResultList();
		for ( Order order : orders ) {
			if ( bh != null ) {
				bh.consume( order );
			}
			if ( counters != null ) {
				counters.orderLines += order.lines.size();
			}
		}
	}

	public void populateData(EntityManager entityManager, int i, int lines) {
		final Order order = new Order();
		order.id = (long) i;
		order.customer = "Customer";
		for ( int j = 0; j < lines; j++ ) {
			final OrderLine orderLine = new OrderLine();
			orderLine.id = j + (long) i * lines;
			orderLine.product = "Product" + j;
			orderLine.order = order;
			order.lines.add( orderLine );
		}
		entityManager.persist( order );
	}

	@Entity(name = "Order")
	@Table(name = "ord_tbl")
	public static class Order {
		@Id
		public Long id;
		@Column
		public String customer;
		@OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
		public Set<OrderLine> lines = new HashSet<>();
	}

	@Entity(name = "OrderLine")
	@Table(name = "line_tbl")
	public static class OrderLine {
		@Id
		public Long id;
		@ManyToOne(fetch = FetchType.LAZY)
		public Order order;
		@Column
		public String product;
	}

	public static void main(String[] args) {
		QueryOneToManyFetch jpaBenchmark = new QueryOneToManyFetch();
		jpaBenchmark.orderLines = 50;
		jpaBenchmark.setup();

		for ( int i = 0; i < 5; i++ ) {
			jpaBenchmark.single(null, null);
		}

		jpaBenchmark.destroy();
	}
}
