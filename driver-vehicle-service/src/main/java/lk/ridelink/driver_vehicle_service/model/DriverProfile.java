package lk.ridelink.driver_vehicle_service.model;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "driver_profiles")
public class DriverProfile {

    @Id
    private String id;

    private String accountId;
    private String driverLicenseNumber;
    private LocalDate licenseExpiryDate;
    private DriverAvailability availability = DriverAvailability.UNAVAILABLE;
    private ServiceArea serviceArea;
    private Location currentLocation;
    private Vehicle vehicle;
    private Instant createdAt;
    private Instant updatedAt;

    public DriverProfile() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getDriverLicenseNumber() {
        return driverLicenseNumber;
    }

    public void setDriverLicenseNumber(String driverLicenseNumber) {
        this.driverLicenseNumber = driverLicenseNumber;
    }

    public LocalDate getLicenseExpiryDate() {
        return licenseExpiryDate;
    }

    public void setLicenseExpiryDate(LocalDate licenseExpiryDate) {
        this.licenseExpiryDate = licenseExpiryDate;
    }

    public DriverAvailability getAvailability() {
        return availability;
    }

    public void setAvailability(DriverAvailability availability) {
        this.availability = availability;
    }

    public ServiceArea getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(ServiceArea serviceArea) {
        this.serviceArea = serviceArea;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Location {
        private double latitude;
        private double longitude;

        public Location() {
        }

        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    public static class ServiceArea {
        private Location center;
        private double radiusKm;

        public ServiceArea() {
        }

        public ServiceArea(Location center, double radiusKm) {
            this.center = center;
            this.radiusKm = radiusKm;
        }

        public Location getCenter() {
            return center;
        }

        public void setCenter(Location center) {
            this.center = center;
        }

        public double getRadiusKm() {
            return radiusKm;
        }

        public void setRadiusKm(double radiusKm) {
            this.radiusKm = radiusKm;
        }
    }

    public static class Vehicle {
        private String registrationNumber;
        private VehicleType type;
        private String make;
        private String model;
        private int year;
        private String color;
        private int seatingCapacity;

        public Vehicle() {
        }

        public Vehicle(
                String registrationNumber,
                VehicleType type,
                String make,
                String model,
                int year,
                String color,
                int seatingCapacity) {
            this.registrationNumber = registrationNumber;
            this.type = type;
            this.make = make;
            this.model = model;
            this.year = year;
            this.color = color;
            this.seatingCapacity = seatingCapacity;
        }

        public String getRegistrationNumber() {
            return registrationNumber;
        }

        public void setRegistrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
        }

        public VehicleType getType() {
            return type;
        }

        public void setType(VehicleType type) {
            this.type = type;
        }

        public String getMake() {
            return make;
        }

        public void setMake(String make) {
            this.make = make;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public int getSeatingCapacity() {
            return seatingCapacity;
        }

        public void setSeatingCapacity(int seatingCapacity) {
            this.seatingCapacity = seatingCapacity;
        }
    }
}