package ca.humber.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "STUDENT_COURSE_SAS")
public class StudentCourse {
	@EmbeddedId
	private StudentCourseId id;

	@ManyToOne
	@MapsId("studentId")
	@JoinColumn(name = "STUDENT_ID")
	private Student student;

	@ManyToOne
	@MapsId("courseId")
	@JoinColumn(name = "COURSE_ID")
	private Course course;

	public StudentCourse() {
	}

	public StudentCourse(Student student, Course course) {
		this.student = student;
		this.course = course;
		this.id = new StudentCourseId(student.getStudentId(), course.getCourseId());
	}

	public Student getStudent() {
		return student;
	}

	public Course getCourse() {
		return course;
	}
}
