package ca.humber.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class StudentCourseId implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "STUDENT_ID")
	private Integer studentId;

	@Column(name = "COURSE_ID")
	private Integer courseId;

	public StudentCourseId() {
	}

	public StudentCourseId(Integer studentId, Integer courseId) {
		this.studentId = studentId;
		this.courseId = courseId;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public Integer getCourseId() {
		return courseId;
	}

	@Override
	public int hashCode() {
		return studentId.hashCode() + courseId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		StudentCourseId other = (StudentCourseId) obj;
		return studentId.equals(other.studentId) && courseId.equals(other.courseId);
	}
}
