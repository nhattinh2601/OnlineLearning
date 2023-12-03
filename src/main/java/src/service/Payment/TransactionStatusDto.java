package src.service.Payment;

import lombok.Data;

import java.io.Serializable;
@Data
public class TransactionStatusDto implements Serializable {
    private String status;
    private String message;
    private String data;
}
