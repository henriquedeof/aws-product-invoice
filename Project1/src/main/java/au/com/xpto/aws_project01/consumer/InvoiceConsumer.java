package au.com.xpto.aws_project01.consumer;

import au.com.xpto.aws_project01.model.Invoice;
import au.com.xpto.aws_project01.model.SnsMessage;
import au.com.xpto.aws_project01.repository.InvoiceRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class InvoiceConsumer {

    //objectmapper pra fazer a deserializacao do objeto de nota fiscal

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceConsumer.class);

    private final ObjectMapper objectMapper;
    private final InvoiceRepository invoiceRepository;
    private final AmazonS3 amazonS3;

    public InvoiceConsumer(ObjectMapper objectMapper, InvoiceRepository invoiceRepository, AmazonS3 amazonS3) {
        this.objectMapper = objectMapper;
        this.invoiceRepository = invoiceRepository;
        this.amazonS3 = amazonS3;
    }

    @JmsListener(destination = "${aws.sqs.queue.invoice.events.name}") //Mapping which SQS this method must integrate.
    public void receiveS3Event(TextMessage textMessage) throws JMSException, IOException {
        SnsMessage snsMessage = this.objectMapper.readValue(textMessage.getText(), SnsMessage.class);
        S3EventNotification s3EventNotification = this.objectMapper.readValue(snsMessage.getMessage(), S3EventNotification.class);

        this.processInvoiceNotification(s3EventNotification);
    }

    private void processInvoiceNotification(S3EventNotification s3EventNotification) throws IOException {

        for (S3EventNotification.S3EventNotificationRecord s3EventNotificationRecord:s3EventNotification.getRecords()) {
            S3EventNotification.S3Entity s3Entity = s3EventNotificationRecord.getS3();

            String bucketName = s3Entity.getBucket().getName();
            String objectKey = s3Entity.getObject().getKey();

            String invoiceFile = this.downloadObject(bucketName, objectKey); //Downloading file (S3 Object) and transforming into String.
            Invoice invoice = this.objectMapper.readValue(invoiceFile, Invoice.class);

            LOG.info("Invoice received: {} " + invoice.getInvoiceNumber());

            this.invoiceRepository.save(invoice);
            this.amazonS3.deleteObject(bucketName, objectKey);
        }

    }

    private String downloadObject(String bucketName, String objectKey) throws IOException {
        String content = null;

        S3Object s3Object = this.amazonS3.getObject(bucketName, objectKey);

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));

        while((content = bufferedReader.readLine()) != null){
            stringBuilder.append(content);
        }

        return stringBuilder.toString();
    }


}
