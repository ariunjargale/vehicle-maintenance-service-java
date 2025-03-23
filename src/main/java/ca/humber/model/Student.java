package ca.humber.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "STUDENT_SAS")
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "STUDENT_ID")
	private Integer studentId;

	@Column(name = "NAME", nullable = false, length = 100)
	private String name;

	@Column(name = "EMAIL", nullable = false, unique = true, length = 100)
	private String email;

	@OneToMany(mappedBy = "student", cascade = CascadeType.PERSIST, orphanRemoval = false)
	private List<StudentCourse> studentCourses;

	// Getters and Setters
	public Integer getStudentId() {
		return studentId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<StudentCourse> getStudentCourses() {
		return studentCourses;
	}

	public void setStudentCourses(List<StudentCourse> studentCourses) {
		this.studentCourses = studentCourses;
	}
	@Override
	public String toString() {
		return name;
	}
}
