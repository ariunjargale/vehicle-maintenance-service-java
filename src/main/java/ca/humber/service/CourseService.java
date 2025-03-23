package ca.humber.service;

import java.util.List;

import ca.humber.dao.CourseDAO;
import ca.humber.exceptions.CRUDFailedException;
import ca.humber.exceptions.ConstraintException;
import ca.humber.exceptions.DataNotFoundException;
import ca.humber.model.Course;

public class CourseService {

	public static List<Course> listCourses() throws DataNotFoundException {
		List<Course> courses = CourseDAO.getCourses();
		if (courses.isEmpty()) {
			throw new DataNotFoundException("No courses found.");
		}
		return courses;
	}

	public static Course getCourseById(int courseId) throws DataNotFoundException {
		Course course = CourseDAO.getCourseById(courseId);
		if (course != null) {
			return course;
		}

		throw new DataNotFoundException("Course with ID " + courseId + " not found.");
	}

	public static boolean addCourse(Course course) throws CRUDFailedException {
		try {
			Course newCourse = course;
			CourseDAO.insertCourse(newCourse);

			return true;
		} catch (NumberFormatException e) {
			throw new CRUDFailedException("Error reading input! Ensure correct format.");
		}
	}

	public static boolean updateCourse(Course newCourse) throws CRUDFailedException {
		boolean res = CourseDAO.updateCourse(newCourse);
		if (!res) {
			throw new CRUDFailedException("Course with ID " + newCourse.getCourseId() + " not found.");
		}
		return res;
	}

	public static boolean deleteCourse(int courseId) throws CRUDFailedException, ConstraintException {
		boolean res = CourseDAO.deleteCourse(courseId);
		if (!res) {
			throw new CRUDFailedException("Course with ID " + courseId + " not found.");
		}
		return res;
	}
}
