package org.hibernate.bugs;

import jakarta.persistence.*;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class JPAUnitTestCase {

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory(
				"templatePU",
				Map.of("hibernate.session_factory.statement_inspector", StatementCollector.class)
		);
	}

	@After
	public void destroy() {
		entityManagerFactory.close();
	}

	@Test
	public void hhh123Test() throws Exception {
		// given
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		// when
		entityManager.createQuery("UPDATE Parent p SET p.someField = :value WHERE p.id = :id")
				.setParameter("value", "someValue")
				.setParameter("id", 1L)
				.executeUpdate();

		// cleanup
		entityManager.getTransaction().rollback();
		entityManager.close();

		// then
		String statement = StatementCollector.statements.stream()
				.filter(stmt -> stmt.startsWith("update"))
				.filter(stmt -> stmt.contains("someField"))
				.findFirst().get();
		assertFalse(statement.toLowerCase().contains("select"), () -> "Query should not contain subquery: `" + statement + "`");
	}


	public static class StatementCollector implements StatementInspector {
		static List<String> statements = new ArrayList<>();

		public String inspect(String s) {
			statements.add(s);
			return s;
		}
	}

	@Entity(name = "Parent")
	@Table
	static class Parent {
		@Id
		Long id;
		String someField;
		@ManyToOne
		@JoinTable(name = "parent_child")
		Child child;
	}

	@Entity
	@Table
	static class Child {
		@Id
		Long id;
	}
}
