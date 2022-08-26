package org.hibernate.bugs;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
				Collections.singletonMap("hibernate.session_factory.statement_inspector", StatementCollector.class)
		);
	}

	@After
	public void destroy() {
		entityManagerFactory.close();
	}

	@Test
	public void hhh123Test() throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			entityManager.createQuery("UPDATE Parent p SET p.someField = :value WHERE p.id = :id")
					.setParameter("value", "someValue")
					.setParameter("id", 1L)
					.executeUpdate();
		} finally {
			entityManager.getTransaction().rollback();
			entityManager.close();
		}
		String statement = StatementCollector.statements.stream()
				.filter(it -> it.contains("someField"))
				.findFirst().get();

		assertFalse(statement.toLowerCase().contains("join"), () -> "Query should not contain `join`: `" + statement + "`");
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
