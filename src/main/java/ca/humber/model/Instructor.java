package ca.humber.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "INSTRUCTOR_SAS")
public class Instructor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "INSTRUCTOR_ID")
	private Integer instructorId;

	@Column(name = "NAME", nullable = false, length = 100)
	private String name;

	@Column(name = "EMAIL", nullable = false, unique = true, length = 100)
	private String email;

	@Column(name = "DEPARTMENT", nullable = false, length = 100)
	private String department;

	@OneToMany(mappedBy = "instructor", cascade = CascadeType.PERSIST, orphanRemoval = false)
	private List<Course> courses;

	// Set INSTRUCTOR_ID = NULL before deletion
	@PreRemove
	private void preRemove() {
		for (Course course : courses) {
			course.setInstructor(null);
		}
	}

	public Integer getInstructorId() {
		return instructorId;
	}

	public void setInstructorId(Integer instructorId) {
		this.instructorId = instructorId;
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

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public List<Course> getCourses() {
		return courses;
	}

	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}

	@Override
	public String toString() {
		return name;
	}

}
