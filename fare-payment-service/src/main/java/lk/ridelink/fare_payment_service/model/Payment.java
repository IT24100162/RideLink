package lk.ridelink.fare_payment_service.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "payments")
public class Payment {
    @Id private String id;
    private String rideId;
    private String passengerAccountId;
    private String driverAccountId;
    private String currency;
    private BigDecimal straightLineDistanceKm;
    private BigDecimal billableDistanceKm;
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal serviceFee;
    private BigDecimal totalFare;
    private PaymentStatus status;
    private String failureMessage;
    private Instant createdAt;
    private Receipt receipt;

    public Payment() {}
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }
    public String getPassengerAccountId() { return passengerAccountId; }
    public void setPassengerAccountId(String passengerAccountId) { this.passengerAccountId = passengerAccountId; }
    public String getDriverAccountId() { return driverAccountId; }
    public void setDriverAccountId(String driverAccountId) { this.driverAccountId = driverAccountId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getStraightLineDistanceKm() { return straightLineDistanceKm; }
    public void setStraightLineDistanceKm(BigDecimal straightLineDistanceKm) { this.straightLineDistanceKm = straightLineDistanceKm; }
    public BigDecimal getBillableDistanceKm() { return billableDistanceKm; }
    public void setBillableDistanceKm(BigDecimal billableDistanceKm) { this.billableDistanceKm = billableDistanceKm; }
    public BigDecimal getBaseFare() { return baseFare; }
    public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }
    public BigDecimal getDistanceFare() { return distanceFare; }
    public void setDistanceFare(BigDecimal distanceFare) { this.distanceFare = distanceFare; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }
    public BigDecimal getTotalFare() { return totalFare; }
    public void setTotalFare(BigDecimal totalFare) { this.totalFare = totalFare; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getFailureMessage() { return failureMessage; }
    public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Receipt getReceipt() { return receipt; }
    public void setReceipt(Receipt receipt) { this.receipt = receipt; }

    public static class Receipt {
        private String receiptNumber;
        private Instant issuedAt;
        public Receipt() {}
        public Receipt(String receiptNumber, Instant issuedAt) { this.receiptNumber = receiptNumber; this.issuedAt = issuedAt; }
        public String getReceiptNumber() { return receiptNumber; }
        public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
        public Instant getIssuedAt() { return issuedAt; }
        public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }
    }
}
