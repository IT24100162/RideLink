package lk.ridelink.ride_management_service.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rides")
public class Ride {

    @Id
    private String id;
    private String passengerAccountId;
    private String assignedDriverAccountId;
    private Place pickup;
    private Place destination;
    private RideStatus status;
    private Double assignmentDistanceKm;
    private String cancellationReason;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant assignedAt;
    private Instant acceptedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    public Ride() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPassengerAccountId() { return passengerAccountId; }
    public void setPassengerAccountId(String passengerAccountId) { this.passengerAccountId = passengerAccountId; }
    public String getAssignedDriverAccountId() { return assignedDriverAccountId; }
    public void setAssignedDriverAccountId(String assignedDriverAccountId) { this.assignedDriverAccountId = assignedDriverAccountId; }
    public Place getPickup() { return pickup; }
    public void setPickup(Place pickup) { this.pickup = pickup; }
    public Place getDestination() { return destination; }
    public void setDestination(Place destination) { this.destination = destination; }
    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }
    public Double getAssignmentDistanceKm() { return assignmentDistanceKm; }
    public void setAssignmentDistanceKm(Double assignmentDistanceKm) { this.assignmentDistanceKm = assignmentDistanceKm; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Instant acceptedAt) { this.acceptedAt = acceptedAt; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }

    public static class Place {
        private String address;
        private double latitude;
        private double longitude;

        public Place() {
        }

        public Place(String address, double latitude, double longitude) {
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }
}
