package ca.humber.dao;

import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.humber.model.Course;
import ca.humber.model.Student;
import ca.humber.model.StudentCourse;
import ca.humber.model.StudentCourseId;
import ca.humber.util.HibernateUtil;

public class StudentCourseDAO {

	public static List<StudentCourse> getAllEnrollments() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.createQuery("FROM StudentCourse", StudentCourse.class).list();
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean enrollStudent(int courseId, int studentId) {
		Transaction tx = null;
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			Course course = session.get(Course.class, courseId);
			Student student = session.get(Student.class, studentId);

			if (course != null && student != null) {
				StudentCourseId studentCourseId = new StudentCourseId(studentId, courseId);

				StudentCourse existingEnrollment = session.get(StudentCourse.class, studentCourseId);
				if (existingEnrollment == null) {
					StudentCourse newEnrollment = new StudentCourse(student, course);
					session.save(newEnrollment);
					tx.commit();
					System.out.println("Student enrolled successfully.");
					return true;
				} else {
					System.out.println("Student is already enrolled in this course.");
					return false;
				}
			} else {
				System.out.println("Course or Student not found.");
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

	@SuppressWarnings("deprecation")
	public static boolean dropStudentFromCourse(int courseId, int studentId) {
		Transaction tx = null;
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();

			StudentCourseId studentCourseId = new StudentCourseId(studentId, courseId);
			StudentCourse enrollment = session.get(StudentCourse.class, studentCourseId);

			if (enrollment != null) {
				session.delete(enrollment);
				tx.commit();
				System.out.println("Student removed from course successfully.");
				return true;
			} else {
				System.out.println("Student is not enrolled in this course.");
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
