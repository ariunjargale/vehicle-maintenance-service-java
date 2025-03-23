package ca.humber.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Course;
import ca.humber.util.HibernateUtil;

public class CourseDAO {

	public static List<Course> getCourses() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<Course> query = session.createQuery("FROM Course", Course.class);
			List<Course> courses = query.list();
			return courses;
		}
	}

	public static Course getCourseById(int id) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Course course = session.get(Course.class, id);
			return course;
		}
	}

	@SuppressWarnings("deprecation")
	public static void insertCourse(Course course) {
		Transaction tx = null;
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			session.save(course);

			tx.commit();
			System.out.println("Course added successfully!");
		} catch (Exception e) {
			if (tx != null && tx.getStatus().canRollback()) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean updateCourse(Course course) {
		Transaction tx = null;
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			session.update(course);

			tx.commit();
			System.out.println("Course updated successfully!");
			return true;
		} catch (Exception e) {
			if (tx != null && tx.getStatus().canRollback()) {
				tx.rollback();
			}
			e.printStackTrace();
			return false;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean deleteCourse(int courseId) throws ConstraintException {
		Transaction tx = null;
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			Course course = session.get(Course.class, courseId);
			if (course != null) {
				session.delete(course);
				tx.commit();
				System.out.println("Course deleted successfully!");
				return true;
			} else {
				System.out.println("Course with ID " + courseId + " not found.");
				return false;
			}
		} catch (ConstraintViolationException e) {
			if (tx != null && tx.getStatus().canRollback()) {
				tx.rollback();
			}
			throw new ConstraintException("Cannot delete course. Students are enrolled in this course.");
		} catch (Exception e) {
			if (tx != null && tx.getStatus().canRollback()) {
				tx.rollback();
			}
			e.printStackTrace();
			return false;
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
}
