package ca.humber.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "COURSE_SAS")
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "COURSE_ID")
	private Integer courseId;

	@Column(name = "COURSE_NAME", nullable = false, length = 100)
	private String courseName;

	@Column(name = "CREDITS", nullable = false)
	private Integer credits;

	@ManyToOne
	@JoinColumn(name = "INSTRUCTOR_ID", referencedColumnName = "INSTRUCTOR_ID", nullable = true)
	private Instructor instructor;

	@OneToMany(mappedBy = "course", cascade = CascadeType.PERSIST, orphanRemoval = false)
	private List<StudentCourse> studentCourses;

	// Getters and Setters
	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public Integer getCredits() {
		return credits;
	}

	public void setCredits(Integer credits) {
		this.credits = credits;
	}

	public Instructor getInstructor() {
		return instructor;
	}

	public void setInstructor(Instructor instructor) {
		this.instructor = instructor;
	}

	public List<StudentCourse> getStudentCourses() {
		return studentCourses;
	}

	public void setStudentCourses(List<StudentCourse> studentCourses) {
		this.studentCourses = studentCourses;
	}

	@Override
	public String toString() {
		return courseName;
	}
}
