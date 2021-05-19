package au.com.xpto.aws_project01.controller;

import au.com.xpto.aws_project01.model.Invoice;
import au.com.xpto.aws_project01.model.UrlResponse;
import au.com.xpto.aws_project01.repository.InvoiceRepository;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Value("${aws.s3.bucket.invoice.name}")
    private String bucketName;

    private final AmazonS3 amazonS3;
    private final InvoiceRepository invoiceRepository;

    public InvoiceController(AmazonS3 amazonS3, InvoiceRepository invoiceRepository) {
        this.amazonS3 = amazonS3;
        this.invoiceRepository = invoiceRepository;
    }

    //UUID = criando ID aleatorio grande que vai ser passado pro bucket.

    @PostMapping
    public ResponseEntity<UrlResponse> createInvoiceUrl(){
        UrlResponse urlResponse = new UrlResponse();
        Instant expirationTime = Instant.now().plus(Duration.ofMinutes(5));
        String processId = UUID.randomUUID().toString();

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(this.bucketName, processId)
                    .withMethod(HttpMethod.PUT)
                    .withExpiration(Date.from(expirationTime));

        urlResponse.setExpirationTime(expirationTime.getEpochSecond());
        urlResponse.setUrl(amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString());

        return new ResponseEntity<UrlResponse>(urlResponse, HttpStatus.OK);
    }

    @GetMapping
    public Iterable<Invoice> findAll(){
        return this.invoiceRepository.findAll();
    }

    @GetMapping(path = "/bycustomername")
    public Iterable<Invoice> findByCustomerName(@RequestParam String customerName){
        return this.invoiceRepository.findAllByCustomerName(customerName);
    }


}
