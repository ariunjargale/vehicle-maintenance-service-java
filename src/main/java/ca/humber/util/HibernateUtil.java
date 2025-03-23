package ca.humber.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ca.humber.model.Course;
import ca.humber.model.Instructor;
import ca.humber.model.Student;
import ca.humber.model.StudentCourse;
import ca.humber.model.StudentCourseId;

public class HibernateUtil {
	private static final SessionFactory sessionFactory = buildSessionFactory();

	private static SessionFactory buildSessionFactory() {
		try {
			return new Configuration().configure("hibernate.cfg.xml").addAnnotatedClass(Instructor.class)
					.addAnnotatedClass(Student.class).addAnnotatedClass(Course.class).addAnnotatedClass(StudentCourseId.class)
					.addAnnotatedClass(StudentCourse.class).buildSessionFactory();
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}