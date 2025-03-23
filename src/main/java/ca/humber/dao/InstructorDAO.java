package ca.humber.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import ca.humber.model.Instructor;
import ca.humber.util.HibernateUtil;

public class InstructorDAO {

	public static List<Instructor> getInstructors() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<Instructor> query = session.createQuery("FROM Instructor", Instructor.class);
			return query.list();
		}
	}

	public static Instructor getInstructorById(int id) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.get(Instructor.class, id);
		}
	}

	@SuppressWarnings("deprecation")
	public static void insertInstructor(Instructor instructor) {
		Transaction tx = null;
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			session.save(instructor);

			tx.commit();
			System.out.println("Instructor added successfully!");
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
	public static boolean updateInstructor(Instructor instructor) {
		Transaction tx = null;
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			session.update(instructor);

			tx.commit();
			System.out.println("Instructor updated successfully!");
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
	public static boolean deleteInstructor(int instructorId) {
		Transaction tx = null;
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			Instructor instructor = session.get(Instructor.class, instructorId);
			if (instructor != null) {
				session.delete(instructor);
				tx.commit();
				System.out.println("Instructor deleted successfully!");
				return true;
			} else {
				System.out.println("Instructor with ID " + instructorId + " not found.");
				return false;
			}
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
