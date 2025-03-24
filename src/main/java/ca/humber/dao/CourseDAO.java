package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Course;
import ca.humber.util.HibernateUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import java.util.List;

public class CourseDAO {
    public static List<Course> getCoursesList() {
        return HibernateUtil.executeWithResult(session -> {
            Query<Course> query = session.createQuery("FROM Course", Course.class);
            return query.list();
        });
    }

    public static Course getCourseById(int id) {
        return HibernateUtil.executeWithResult(session -> session.get(Course.class, id));
    }

    public static void insertCourse(Course course) {
        HibernateUtil.executeInsideTransaction(session -> session.save(course));
        System.out.println("Course added successfully.");
    }

    public static boolean updateCourse(Course course) {
        try {
            HibernateUtil.executeInsideTransaction(session -> session.update(course));
            System.out.println("Course updated successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteCourse(int courseId) throws ConstraintException {
        Course course = HibernateUtil.executeWithResult(session -> session.get(Course.class, courseId));

        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return false;
        }

        try {
            HibernateUtil.executeInsideTransaction(session -> session.delete(course));
            System.out.println("Course deleted successfully.");
            return true;
        } catch (ConstraintViolationException e) {
            throw new ConstraintException("Cannot delete course. Students are enrolled in this course.");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
