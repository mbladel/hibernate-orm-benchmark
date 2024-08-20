package org.hibernate.benchmark.flush;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
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
public class PartialFlush {

	private static final Integer EGGPLANT_ID = Integer.valueOf(42);
	private static final Integer PASSION_FRUIT_ID = Integer.valueOf(43);
	private static final Integer PEACH_TYPE_ID = Integer.valueOf(44);

	private int idSequence = 0;

	protected EntityManagerFactory entityManagerFactory;
	protected EntityManager em;

	@Setup
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory("PartialFlush");

		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();

		Eggplant eggplant = createEggplant(em);
		Rockmelon rockmelon = createRockmelon(em);
		Strawberry strawberry = createStrawberry(em, rockmelon);
		createStrawberryEggplantLink(em, strawberry, eggplant);
		createPassionFruit(em);
		createPeachType(em);

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
		public long objects;
	}

	@Benchmark
	public void single(Blackhole bh, EventCounters counters) {
		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		try {
			writeEntities( bh, counters );
		}
		finally {
			em.getTransaction().commit();
			em.close();
		}

		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		try {
			em.createQuery("delete from Apricot").executeUpdate();
			em.createQuery("delete from Apple").executeUpdate();
		}
		finally {
			em.getTransaction().commit();
			em.close();
		}
	}

	protected void writeEntities(Blackhole bh, EventCounters counters) {
		Eggplant eggplant = em.find(Eggplant.class, EGGPLANT_ID);
		Collection<Apple> apples = createApples(em);
		for (Apple apple : apples) {
			List<?> apricots = em.createQuery( "select a from Apricot a" ).getResultList();
			if ( bh != null ) {
				bh.consume( apricots );
			}
			createApricot(em, eggplant, apple);
		}
		if ( counters != null ) {
			counters.objects += apples.size();
		}
	}

	public static void main(String[] args) {
		PartialFlush jpaBenchmark = new PartialFlush();
		jpaBenchmark.setup();

		for ( int i = 0; i < 5; i++ ) {
			jpaBenchmark.single(null, null);
		}

		jpaBenchmark.destroy();
	}

	private void createApricot(EntityManager em, Eggplant eggplant, Apple apple) {
		Apricot apricot = new Apricot();
		apricot.setId(++idSequence);
		apricot.setRaspberry(eggplant);
		apricot.setApple(apple);
		apricot.setValidity(apple.getTimeRange());
		apricot.setPassionFruit(apple.getPassionFruit());
		apricot.setValue5("Hello World");
		apricot.getApple().setApricot(apricot);
		em.persist(apricot);
	}

	private Eggplant createEggplant(EntityManager em) {
		Eggplant eggplant = new Eggplant();
		eggplant.setId(EGGPLANT_ID);
		eggplant.setProducerNumber("007");
		eggplant.setHarvestDate(new Date());
		eggplant.setValidity(new TimeRange(makeDate(2010, 1, 1), null));
		em.persist(eggplant);
		return eggplant;
	}

	private Rockmelon createRockmelon(EntityManager em) {
		String name = "rockmelon";
		Rockmelon rockmelon = new Rockmelon();
		rockmelon.setId(++idSequence);
		rockmelon.setName(new Name(name, name, name));
		em.persist(rockmelon);
		return rockmelon;
	}

	private Strawberry createStrawberry(EntityManager em, Rockmelon rockmelon) {
		Strawberry strawberry = new Strawberry();
		strawberry.setId(++idSequence);
		strawberry.setRockmelon(rockmelon);
		strawberry.setNumber(1);
		em.persist(strawberry);
		return strawberry;
	}

	private void createStrawberryEggplantLink(EntityManager em, Strawberry strawberry, Eggplant eggplant) {
		StrawberryEggplantLink link = new StrawberryEggplantLink(strawberry, eggplant, TimeRange.unbound());
		link.setId(++idSequence);
		Set<StrawberryEggplantLink> newLinks = new HashSet<>(eggplant.getStrawberryEggplantLinks());
		newLinks.add(link);
		eggplant.setStrawberryEggplantLinks(newLinks);
		em.persist(link);
	}

	private void createPassionFruit(EntityManager em) {
		String name = "pf";
		PassionFruit passionFruit = new PassionFruit(1);
		passionFruit.setId(PASSION_FRUIT_ID);
		passionFruit.setName(new Name(name, name, name));
		passionFruit.setRaspberryType(RaspberryType.EGGPLANT);
		em.persist(passionFruit);
	}

	private void createPeachType(EntityManager em) {
		String name = "type";
		PeachType type = new PeachType();
		type.setId(PEACH_TYPE_ID);
		type.setName(new Name(name, name, name));
		type.setValidity(TimeRange.unbound());
		em.persist(type);
	}

	private Collection<Apple> createApples(EntityManager em) {
		PassionFruit pf = em.find(PassionFruit.class, PASSION_FRUIT_ID);
		PeachType pt = em.find(PeachType.class, PEACH_TYPE_ID);
		LocalDate baseDate = LocalDate.of( 2024, 8, 7);

		Collection<Apple> apples = new ArrayList<>();
		for (int i = 0; i < 1000; ++i) {
			apples.add(createPeach(em, pf, pt, baseDate.plusDays(i)));
		}
		return apples;
	}

	private Apple createPeach(EntityManager em, PassionFruit pf, PeachType pt, LocalDate date) {
		Peach peach = new Peach();
		peach.setId(++idSequence);
		peach.setAppleType(pt);
		peach.setName(pt.getName());
		peach.setPassionFruit(pf);
		peach.setTimeRange(makePeachTimeRange(date));
		em.persist(peach);
		return peach;
	}

	private TimeRange makePeachTimeRange(LocalDate date) {
		Date start = toDate(date.atTime(8, 0));
		Date end = toDate(date.atTime(17, 0));
		return new TimeRange(start, end);
	}

	private Date makeDate(int year, int month, int day) {
		return toDate(LocalDate.of(year, month, day).atStartOfDay());
	}

	private Date toDate(LocalDateTime from) {
		return Date.from(from.toInstant( ZoneOffset.ofHours( 0)));
	}


	@jakarta.persistence.MappedSuperclass
	public static abstract class BaseEntity {

		@jakarta.persistence.Id
		private Integer id;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			if (!this.getClass().isInstance(o)) {
				return false;
			}
			BaseEntity entity = (BaseEntity) o;
			if (entity.getId() != null && getId() != null) {
				return entity.getId().equals(getId());
			}

			return entity == this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (id == null ? 0 : id.hashCode());
			return result;
		}
	}

	@jakarta.persistence.Entity(name = "Apple")
	@DiscriminatorColumn(length = 255)
	public static abstract class Apple
			extends BaseEntity {

		@jakarta.persistence.OneToMany(mappedBy = "apple")
		private Set<Apricot> apricots = new HashSet<>();

		@jakarta.persistence.Embedded
		private TimeRange timeRange;

		@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
		private PassionFruit passionFruit;

		private Boolean flag1 = Boolean.FALSE;
		private Boolean flag2 = Boolean.FALSE;

		protected Apple() {
			super();
		}

		protected Apple(PassionFruit passionFruit, TimeRange timeRange) {
			this.passionFruit = passionFruit;
			this.timeRange = timeRange;
		}

		public Apricot getApricot() {
			if (apricots == null || apricots.isEmpty()) {
				return null;
			}
			return apricots.iterator().next();
		}

		public Boolean getFlag2() {
			return flag2;
		}

		public PassionFruit getPassionFruit() {
			return passionFruit;
		}

		public Boolean getFlag1() {
			return flag1;
		}

		public TimeRange getTimeRange() {
			return timeRange == null ? TimeRange.unbound() : timeRange;
		}

		public void setApricot(Apricot apricot) {
			if (apricot == null) {
				updateApricots(new HashSet<>());
			} else {
				updateApricots( Collections.singleton( apricot));
			}
		}

		private void updateApricots(Collection<Apricot> updatedApricots) {
			if (apricots == updatedApricots) {
				return;
			}
			apricots.clear();
			if (updatedApricots != null) {
				apricots.addAll(updatedApricots);
			}
		}

		public void setFlag2(Boolean flag2) {
			this.flag2 = flag2;
		}

		public void setPassionFruit(PassionFruit passionFruit) {
			this.passionFruit = passionFruit;
		}

		public void setFlag1(Boolean flag1) {
			this.flag1 = flag1;
		}

		public void setTimeRange(TimeRange timeRange) {
			this.timeRange = timeRange;
		}
	}

	@jakarta.persistence.Entity
	@BatchSize(size = 1000)
	@DiscriminatorColumn(length = 255)
	public static abstract class AppleType
			extends NamedEntity {

		@jakarta.persistence.Embedded
		private TimeRange validity;

		public TimeRange getValidity() {
			return validity == null ? TimeRange.unbound() : validity;
		}

		public void setValidity(TimeRange validity) {
			this.validity = TimeRange.unbound().equals(validity) ? null : validity;
		}
	}

	@jakarta.persistence.Entity(name = "Apricot")
	public static class Apricot
			extends BaseEntity {

		@jakarta.persistence.ManyToOne
		private Raspberry raspberry;

		@jakarta.persistence.ManyToOne
		private Apple apple;

		@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
		private PassionFruit passionFruit;

		@jakarta.persistence.Embedded
		private TimeRange validity;

		private String value1;

		private String value2;

		private Integer value3;

		private Integer value4;

		private String value5;

		private Integer value6;

		public String getValue1() {
			return value1;
		}

		public void setValue1(String value) {
			this.value1 = value;
		}

		public PassionFruit getPassionFruit() {
			return passionFruit;
		}

		public void setPassionFruit(PassionFruit passionFruit) {
			this.passionFruit = passionFruit;
		}

		public Raspberry getRaspberry() {
			return raspberry;
		}

		public void setRaspberry(Raspberry raspberry) {
			this.raspberry = raspberry;
		}

		public TimeRange getValidity() {
			return validity == null ? TimeRange.unbound() : validity;
		}

		public void setValidity(TimeRange zeitintervall) {
			this.validity = zeitintervall;
		}

		public Apple getApple() {
			return apple;
		}

		public void setApple(Apple apple) {
			this.apple = apple;
			if (apple != null) {
				this.validity = apple.getTimeRange();
			}
		}

		public String getValue5() {
			return value5;
		}

		public void setValue5(String value) {
			this.value5 = value;
		}

		public String getValue2() {
			return value2;
		}

		public void setValue2(String value) {
			this.value2 = value;
		}

		public Integer getValue4() {
			return value4;
		}

		public void setValue4(Integer value) {
			this.value4 = value;
		}

		public Integer getValue3() {
			return value3;
		}

		public void setValue3(Integer value) {
			this.value3 = value;
		}

		public Integer getValue6() {
			return value6;
		}

		public void setValue6(Integer value) {
			this.value6 = value;
		}
	}

	@jakarta.persistence.Entity
	@BatchSize(size = 1000)
	public static class Eggplant
			extends Raspberry {

		private Long number;

		@jakarta.persistence.Temporal(jakarta.persistence.TemporalType.DATE)
		private Date harvestDate;

		private String producer;

		private String producerNumber;

		@jakarta.persistence.Embedded
		private TimeRange validity;

		private String country;

		@jakarta.persistence.OneToMany(cascade = jakarta.persistence.CascadeType.ALL, mappedBy = "eggplant")
		@Fetch(FetchMode.SUBSELECT)
		private Set<StrawberryEggplantLink> strawberryEggplantLinks = new HashSet<>();

		public Eggplant() {
			this(null, null);
		}

		public Eggplant(String producer, String country) {
			this.producer = producer;
			this.country = country;
		}

		public Long getNumber() {
			return number;
		}

		public void setNumber(Long number) {
			this.number = number;
		}

		public Date getHarvestDate() {
			return harvestDate;
		}

		public void setHarvestDate(Date harvestDate) {
			this.harvestDate = harvestDate;
		}

		public String getProducer() {
			return producer;
		}

		public void setProducer(String producer) {
			this.producer = producer;
		}

		public String getProducerNumber() {
			return producerNumber;
		}

		public void setProducerNumber(String producerNumber) {
			this.producerNumber = producerNumber;
		}

		public TimeRange getValidity() {
			return validity == null ? TimeRange.unbound() : validity;
		}

		public void setValidity(TimeRange validity) {
			this.validity = validity;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public Set<StrawberryEggplantLink> getStrawberryEggplantLinks() {
			return Collections.unmodifiableSet(strawberryEggplantLinks);
		}

		public void setStrawberryEggplantLinks(Collection<StrawberryEggplantLink> strawberryEggplantLinks) {
			if (this.strawberryEggplantLinks == strawberryEggplantLinks) {
				return;
			}
			this.strawberryEggplantLinks.clear();
			if (strawberryEggplantLinks != null) {
				this.strawberryEggplantLinks.addAll(strawberryEggplantLinks);
			}
		}

		@Override
		public RaspberryType getRaspberryType() {
			return RaspberryType.EGGPLANT;
		}
	}

	@jakarta.persistence.MappedSuperclass
	public static abstract class GeneralizedEggplantLink
			extends RaspberryValidityLink<Eggplant> {

		@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
		@jakarta.persistence.JoinColumn(name = "linkEggplant_id")
		private Eggplant eggplant;

		@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
		private Strawberry strawberry;

		private Boolean flag;

		public GeneralizedEggplantLink() {
		}

		public GeneralizedEggplantLink(Strawberry strawberry, Eggplant eggplant, TimeRange validity, boolean flag) {
			setValidity(validity);
			this.eggplant = eggplant;
			this.strawberry = strawberry;
			this.flag = flag;
		}

		public Strawberry getStrawberry() {
			return strawberry;
		}

		public void setStrawberry(Strawberry strawberry) {
			this.strawberry = strawberry;
		}

		@Override
		public Eggplant getRaspberry() {
			return eggplant;
		}

		@Override
		public void setRaspberry(Eggplant eggplant) {
			this.eggplant = eggplant;
		}

		public Boolean getFlag() {
			return flag;
		}

		public void setFlag(Boolean flag) {
			this.flag = flag;
		}
	}
	@jakarta.persistence.Embeddable
	public static class Name
			implements Serializable, Cloneable {

		private static final long serialVersionUID = 5395498566881573424L;

		private String abbreviation;
		private String description;
		private String longName;

		public Name() {
		}

		public Name(String abbreviation) {
			this(abbreviation, null, null);
		}

		public Name(String abbreviation, String longName, String description) {
			this.abbreviation = abbreviation;
			this.longName = longName;
			this.description = description;
		}

		@Override
		public Object clone() {
			Object result = null;
			try {
				result = super.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null) {
				return false;
			}
			if (o.getClass() != getClass()) {
				return false;
			}
			Name castedObj = (Name) o;
			return ((this.abbreviation == null ? castedObj.abbreviation == null : this.abbreviation
					.equals(castedObj.abbreviation))
					&& (this.longName == null ? castedObj.longName == null : this.longName.equals(castedObj.longName))
					&& (this.description == null
					? castedObj.description == null
					: this.description.equals(castedObj.description)));
		}

		public String getAbbreviation() {
			return abbreviation;
		}

		public String getDescription() {
			return description;
		}

		public String getLongName() {
			return longName;
		}

		@Override
		public int hashCode() {
			int hashCode = 1;
			hashCode = 31 * hashCode + (int) (+serialVersionUID ^ (serialVersionUID >>> 32));
			hashCode = 31 * hashCode + (abbreviation == null ? 0 : abbreviation.hashCode());
			hashCode = 31 * hashCode + (longName == null ? 0 : longName.hashCode());
			hashCode = 31 * hashCode + (description == null ? 0 : description.hashCode());
			return hashCode;
		}
	}

	@jakarta.persistence.MappedSuperclass
	public static abstract class NamedEntity
			extends BaseEntity {

		@jakarta.persistence.Embedded
		private Name name = new Name();

		public Name getName() {
			return name == null ? new Name() : name;
		}

		public void setName(Name name) {
			this.name = name;
		}
	}
	@jakarta.persistence.Entity
	@BatchSize(size = 1000)
	public static class PassionFruit
			extends NamedEntity {

		@jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
		private RaspberryType raspberryType;

		private Integer orderNumber;

		@jakarta.persistence.OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
		private PassionFruit historyPassionFruit;

		@jakarta.persistence.OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
		private PassionFruit referencePassionFruit;

		public PassionFruit() {
			this(null);
		}

		public PassionFruit(Integer odernumber) {
			orderNumber = odernumber;
		}

		public RaspberryType getRaspberryType() {
			return raspberryType;
		}

		public void setRaspberryType(RaspberryType raspberryType) {
			this.raspberryType = raspberryType;
		}

		public PassionFruit getHistoryPassionFruit() {
			return historyPassionFruit;
		}

		public void setHistoryPassionFruit(PassionFruit historyPassionFruit) {
			this.historyPassionFruit = historyPassionFruit;
		}

		public PassionFruit getReferencePassionFruit() {
			return referencePassionFruit;
		}

		public void setReferencePassionFruit(PassionFruit referencePassionFruit) {
			this.referencePassionFruit = referencePassionFruit;
		}

		public Integer getOrderNumber() {
			return orderNumber;
		}
	}

	@jakarta.persistence.Entity
	public static class Peach
			extends Tangerine {

		@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
		private AppleType appleType;

		public Peach() {
			super();
		}

		public Peach(PassionFruit passionFruit, PeachType peachType, TimeRange timeRange) {
			super(passionFruit, timeRange);
			this.appleType = peachType;
		}

		public PeachType getAppleType() {
			return (PeachType) appleType;
		}

		public void setAppleType(PeachType peachType) {
			this.appleType = peachType;
		}
	}

	@jakarta.persistence.Entity
	@BatchSize(size = 1000)
	public static class PeachType
			extends AppleType {

		private Boolean flag;

		private Long value1;

		private Long value2;

		private Long value3;

		public PeachType() {
			super();
			setFlag(true);
		}

		public Boolean getFlag() {
			return flag == null ? Boolean.FALSE : flag;
		}

		public void setFlag(Boolean flag) {
			this.flag = flag;
		}

		public Long getValue3() {
			return value3;
		}

		public void setValue3(Long value) {
			this.value3 = value;
		}

		public Long getValue2() {
			return value2;
		}

		public void setValue2(Long value) {
			this.value2 = value;
		}

		public Long getValue1() {
			return value1;
		}

		public void setValue1(Long value) {
			this.value1 = value;
		}
	}

	@jakarta.persistence.Entity
	@BatchSize(size = 1000)
	@DiscriminatorColumn(length = 255)
	public static abstract class Raspberry
			extends BaseEntity {

		@Column(name = "val")
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public abstract RaspberryType getRaspberryType();
	}

	public enum RaspberryType {
		EGGPLANT,
		STRAWBERRY;
	}

	@jakarta.persistence.Entity
	@DiscriminatorOptions(force = true)
	@DiscriminatorColumn(length = 255)
	public static abstract class RaspberryValidityLink<T extends Raspberry>
			extends BaseEntity {

		@jakarta.persistence.Embedded
		private TimeRange validity;

		public TimeRange getValidity() {
			return validity == null ? TimeRange.unbound() : validity;
		}

		public void setValidity(TimeRange validity) {
			this.validity = validity.equals(TimeRange.unbound()) ? null : validity;
		}

		public abstract T getRaspberry();

		public abstract void setRaspberry(T raspberry);
	}

	@jakarta.persistence.Entity
	@BatchSize(size = 1000)
	public static class Rockmelon
			extends NamedEntity {

		private Integer number;

		@jakarta.persistence.Embedded
		private TimeRange validity;

		public Integer getNumber() {
			return number;
		}

		public void setNumber(Integer number) {
			this.number = number;
		}

		public TimeRange getValidity() {
			return validity == null ? TimeRange.unbound() : validity;
		}

		public void setValidity(TimeRange zeitintervall) {
			this.validity = zeitintervall;
		}
	}

	@jakarta.persistence.Entity
	@BatchSize(size = 1000)
	public static class Strawberry
			extends Raspberry
			implements Comparable<Strawberry> {

		private Integer number;

		@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY,
				cascade = jakarta.persistence.CascadeType.MERGE)
		private Rockmelon rockmelon;

		public Rockmelon getRockmelon() {
			return rockmelon;
		}

		public void setRockmelon(Rockmelon rockmelon) {
			this.rockmelon = rockmelon;
		}

		public Integer getNumber() {
			return number;
		}

		public void setNumber(Integer nummer) {
			this.number = nummer;
		}

		@Override
		public int compareTo(Strawberry other) {
			return getNumber().compareTo(other.getNumber());
		}

		@Override
		public RaspberryType getRaspberryType() {
			return RaspberryType.STRAWBERRY;
		}
	}

	@jakarta.persistence.Entity
	public static class StrawberryEggplantLink
			extends GeneralizedEggplantLink {

		@jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
		private StrawberryEggplantLinkType type;

		public StrawberryEggplantLink() {
		}

		public StrawberryEggplantLink(Strawberry strawberry, Eggplant eggplant, TimeRange validity) {
			this(strawberry, eggplant, validity, false, null);
		}

		public StrawberryEggplantLink(Strawberry strawberry, Eggplant eggplant, TimeRange validity,
									  boolean flag, StrawberryEggplantLinkType type) {
			super(strawberry, eggplant, validity, flag);
			this.type = type;
		}

		public StrawberryEggplantLinkType getType() {
			return type;
		}

		public void setType(StrawberryEggplantLinkType type) {
			this.type = type;
		}
	}

	public enum StrawberryEggplantLinkType {
		OPTION1,
		OPTION2,
		OPTION3,
		OPTION4,
		OPTION5;
	}

	@jakarta.persistence.Entity
	public static abstract class Tangerine
			extends Apple {

		@jakarta.persistence.Embedded
		private Name name;

		private Date validityDay;

		@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
		@jakarta.persistence.JoinColumn(name = "ROCKMELON_ID")
		private Rockmelon rockmelon;

		private Integer referenceNumber;

		private Long value1;

		private Long value2;

		private Long value3;

		private Long value4;

		public Tangerine() {
			super();
		}

		public Tangerine(PassionFruit passionFruit, TimeRange timeRange) {
			super(passionFruit, timeRange);
		}

		public Name getName() {
			return name == null ? new Name() : name;
		}

		public Date getValidityDay() {
			return validityDay;
		}

		public Rockmelon getRockmelon() {
			return rockmelon;
		}

		public Integer getReferenceNumber() {
			return referenceNumber;
		}

		public void setName(Name bezeichnung) {
			this.name = bezeichnung;
		}

		public void setValidityDay(Date validityDay) {
			this.validityDay = validityDay;
		}

		public void setRockmelon(Rockmelon rockmelon) {
			this.rockmelon = rockmelon;
		}

		public void setReferenceNumber(Integer referenceNumber) {
			this.referenceNumber = referenceNumber;
		}

		public Long getValue1() {
			return value1;
		}

		public void setValue1(Long value) {
			this.value1 = value;
		}

		public Long getValue2() {
			return value2;
		}

		public void setValue2(Long value) {
			this.value2 = value;
		}

		public Long getValue3() {
			return value3;
		}

		public void setValue3(Long value) {
			this.value3 = value;
		}

		public Long getValue4() {
			return value4;
		}

		public void setValue4(Long value) {
			this.value4 = value;
		}
	}

	@jakarta.persistence.Embeddable
	public static class TimeRange
			implements Serializable, Cloneable, Comparable<TimeRange> {

		public static TimeRange unbound() {
			return new TimeRange(null, null);
		}

		private static final long serialVersionUID = 2003724643784903043L;

		@jakarta.persistence.Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
		private Date startTime;

		@jakarta.persistence.Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
		private Date endTime;

		private static void checkValidility(Date startTime, Date endTime) {
			assert startTime == null || endTime == null
					|| startTime.getTime() <= endTime.getTime() : "start time must not lie after end time: " + startTime + " - "
					+ endTime;
		}

		public TimeRange() {
			this(null, null);
		}

		public TimeRange(Date anfangszeitpunkt, Date endzeitpunkt) {
			checkValidility(anfangszeitpunkt, endzeitpunkt);
			this.startTime = cloneDate(anfangszeitpunkt);
			this.endTime = cloneDate(endzeitpunkt);
		}

		public TimeRange(Date anfangszeitpunkt, long dauer) {
			Date start = cloneDate(anfangszeitpunkt);
			Date end = cloneDate(anfangszeitpunkt.getTime() + dauer);
			checkValidility(start, end);
			this.startTime = start;
			this.endTime = end;
		}

		public TimeRange(TimeRange other) {
			this(other.getStartTime(), other.getEndTime());
		}

		@Override
		public TimeRange clone() {
			return new TimeRange(getStartTime(), getEndTime());
		}

		@Override
		public int compareTo(TimeRange other) {
			return 0;
		}

		public boolean contains(Date date) {
			return (startTime == null || !date.before(startTime)) && (endTime == null || date.before(endTime));
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof TimeRange)) {
				return false;
			}
			TimeRange z = (TimeRange) o;
			return compareTo(z) == 0;
		}

		@jakarta.persistence.Transient
		public long getLength() {
			if (startTime == null || endTime == null) {
				return Long.MAX_VALUE;
			}
			return endTime.getTime() - startTime.getTime();
		}

		public Date getEndTime() {
			return cloneDate(endTime);
		}

		public TimeRange getImmutableTimeRange() {
			return this;
		}

		public Date getStartTime() {
			return cloneDate(startTime);
		}

		@Override
		public int hashCode() {
			int result = 0;
			result += startTime != null ? startTime.hashCode() : 0;
			result += endTime != null ? endTime.hashCode() : 0;
			return result;
		}

		public boolean isBounded() {
			return startTime != null && endTime != null;
		}

		private static Date cloneDate(Date date) {
			if (date == null) {
				return null;
			}
			return new Date(date.getTime());
		}

		private static Date cloneDate(long time) {
			return new Date(time);
		}
	}

}
