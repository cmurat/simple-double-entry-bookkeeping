package per.cmurat.other.revolut.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDto {
    private long sendingAccountId;
    private long receivingAccountId;
    private BigDecimal amount;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateTime;

    public long getSendingAccountId() {
        return sendingAccountId;
    }

    public void setSendingAccountId(final long sendingAccountId) {
        this.sendingAccountId = sendingAccountId;
    }

    public long getReceivingAccountId() {
        return receivingAccountId;
    }

    public void setReceivingAccountId(final long receivingAccountId) {
        this.receivingAccountId = receivingAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(final LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
