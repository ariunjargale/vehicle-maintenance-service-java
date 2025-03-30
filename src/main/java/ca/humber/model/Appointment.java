package ca.humber.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "APPOINTMENT")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPOINTMENT_ID")
    private Integer appointmentId;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "VEHICLE_ID", nullable = false)
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "SERVICE_ID", nullable = false)
    private Service service;

    @ManyToOne
    @JoinColumn(name = "MECHANIC_ID")
    private Mechanic mechanic;

    @Column(name = "APPOINTMENT_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date appointmentDate;

    @Column(name = "STATUS_ID", nullable = false, length = 1)
    private String statusId;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    public Appointment() {
    }

    public Appointment(Customer customer, Vehicle vehicle, Service service, Mechanic mechanic, Date appointmentDate, String statusId) {
        this.customer = customer;
        this.vehicle = vehicle;
        this.service = service;
        this.mechanic = mechanic;
        this.appointmentDate = appointmentDate;
        this.statusId = statusId;
        this.isActive = true;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(Mechanic mechanic) {
        this.mechanic = mechanic;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "Appointment #" + appointmentId + " - " + 
               customer.getName() + " - " + 
               vehicle.getMake() + " " + vehicle.getModel() + " - " + 
               appointmentDate;
    }
}
