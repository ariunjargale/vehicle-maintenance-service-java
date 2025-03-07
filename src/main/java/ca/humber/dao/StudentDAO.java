/**
 *
 * @author Ariunjargal Erdenebaatar
 * @created Mar 6, 2025
 */
package ca.humber.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import ca.humber.model.Student;
import ca.humber.utils.HibernateUtil;

public class StudentDAO {

	public static List<Student> getStudents() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<Student> query = session.createQuery("FROM Student", Student.class);
			List<Student> students = query.list();
			return students;
		}
	}

}
