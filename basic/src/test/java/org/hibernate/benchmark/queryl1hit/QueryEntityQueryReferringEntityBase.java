package org.hibernate.benchmark.queryl1hit;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.LongStream;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Persistence;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;

import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
public class QueryEntityQueryReferringEntityBase {

	static final int NUMBER_OF_ENTITIES = 1000;

	protected EntityManagerFactory entityManagerFactory;
	protected EntityManager em;

	@Setup
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "bench2" );

		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.createQuery( "delete ProjectAccess" ).executeUpdate();
		em.createQuery( "delete Project" ).executeUpdate();
		em.createQuery( "delete Employee" ).executeUpdate();
		for ( int i = 0; i < 1; i++ ) {
			populateData( em );
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

	public void queryEmployees(boolean includeQueryAccess, Blackhole bh, EventCounters counters) {
		TypedQuery<Employee> query = em.createQuery( "select e From Employee e", Employee.class );
		List<Employee> employeeList = query.getResultList();
		for ( Employee employee : employeeList ) {
			if ( bh != null ) {
				bh.consume( employee );
			}
			if ( includeQueryAccess ) {
				TypedQuery<ProjectAccess> accessQuery = em
						.createQuery( "select pa From ProjectAccess pa where pa.employee = :employee", ProjectAccess.class )
						.setParameter( "employee", employee );

				List<ProjectAccess> resultList = accessQuery.getResultList();
				if ( bh != null ) {
					bh.consume( resultList );
				}
			}
		}
		if (counters != null ) {
			counters.queries++;
			if ( includeQueryAccess ) {
				counters.queries += employeeList.size();
			}
		}
	}

	public void populateData(EntityManager entityManager) {
		LongStream.range( 1, NUMBER_OF_ENTITIES ).forEach( id -> {
			Employee employee = new Employee();
			employee.setFirstName( "FNAME_" + id );
			employee.setLastName( "LNAME_" + id );
			employee.setEmail( "NAME_" + id + "@email.com" );

			entityManager.persist( employee );
		} );

		LongStream.range( 1, NUMBER_OF_ENTITIES ).forEach( id -> {
			Project project = new Project();
			project.setTitle( "TITLE_" + id );
			project.setBegin( new Timestamp( System.currentTimeMillis() ) );
			entityManager.persist( project );
		} );

		LongStream.range( 1, NUMBER_OF_ENTITIES ).forEach( id -> {
			ProjectAccess projectAccess = new ProjectAccess();
			projectAccess.setEmployee( entityManager.find( Employee.class, id ) );
			projectAccess.setProject( entityManager.find( Project.class, id ) );
			projectAccess.setBegin( new Timestamp( System.currentTimeMillis() ) );
			entityManager.persist( projectAccess );
		} );
	}


	@Entity(name = "Project")
	@Table(name = "t_project")
	public static class Project {

		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE)
		@Column(name = "id", nullable = false)
		private Long id;

		@Column
		private String title;

		@Column
		private String description;

		@Column
		private Timestamp begin;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Timestamp getBegin() {
			return begin;
		}

		public void setBegin(Timestamp begin) {
			this.begin = begin;
		}

		@Override
		public String toString() {
			return "Project [id=" + id + ", title=" + title + ", description=" + description + ", begin=" + begin + "]";
		}
	}

	@Entity(name = "ProjectAccess")
	@Table(name = "t_project_access")
	public static class ProjectAccess {

		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE)
		@Column(name = "id", nullable = false)
		private Long id;

		@ManyToOne
		@JoinColumn(name = "employee_id")
		private Employee employee;


		@ManyToOne
		@JoinColumn(name = "project_id")
		private Project project;

		@Column(name = "begin_time")
		private Timestamp begin;

		@Column(name = "end_time")
		private Timestamp end;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Employee getEmployee() {
			return employee;
		}

		public void setEmployee(Employee employee) {
			this.employee = employee;
		}

		public Project getProject() {
			return project;
		}

		public void setProject(Project project) {
			this.project = project;
		}

		public Timestamp getBegin() {
			return begin;
		}

		public void setBegin(Timestamp begin) {
			this.begin = begin;
		}

		public Timestamp getEnd() {
			return end;
		}

		public void setEnd(Timestamp end) {
			this.end = end;
		}

		@Override
		public String toString() {
			return "ProjectAccess [id=" + id + ", employee=" + employee + ", project=" + project + ", begin=" + begin
					+ ", end=" + end + "]";
		}
	}

	@Entity(name = "Employee")
	@Table(name = "t_employee")
	public static class Employee {

		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE)
		@Column(name = "ID")
		private Long employeeId;

		@Column
		private String email;

		@Column
		private String firstName;

		@Column
		private String lastName;


		public Long getEmployeeId() {
			return employeeId;
		}

		public void setEmployeeId(Long employeeId) {
			this.employeeId = employeeId;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		@Override
		public String toString() {
			return "Employee [employeeId=" + employeeId + ", email=" + email + ", firstName=" + firstName + ", lastName="
					+ lastName + "]";
		}
	}


}
