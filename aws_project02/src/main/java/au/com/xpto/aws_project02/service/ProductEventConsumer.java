package au.com.xpto.aws_project02.service;

import au.com.xpto.aws_project02.model.Envelop;
import au.com.xpto.aws_project02.model.ProductEvent;
import au.com.xpto.aws_project02.model.ProductEventLog;
import au.com.xpto.aws_project02.model.SnsMessage;
import au.com.xpto.aws_project02.repository.ProductEventLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Service
public class ProductEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ProductEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final ProductEventLogRepository productEventLogRepository;

    public ProductEventConsumer(ObjectMapper objectMapper, ProductEventLogRepository productEventLogRepository) {
        this.objectMapper = objectMapper;
        this.productEventLogRepository = productEventLogRepository;
    }

    @JmsListener(destination = "${aws.sqs.queue.product.events.name}") //Mapping which SQS this method must integrate.
    public void receiveProductEvent(TextMessage textMessage) throws JMSException, IOException {
        //Deserializing the information that comes from the SQS.
        SnsMessage snsMessage = this.objectMapper.readValue(textMessage.getText(), SnsMessage.class);
        Envelop envelop = this.objectMapper.readValue(snsMessage.getMessage(), Envelop.class);
        ProductEvent productEvent = this.objectMapper.readValue(envelop.getData(), ProductEvent.class);

        //Creating 'snsMessage.getMessageId()' that will share the same MessageId on Project01. It helps to create a link between these two applications
        //resulting in finding log information that has the same MessageId on cloudwatch >> Insights
        LOG.info("Product event received - Event: {} - ProductId: {} - MessageId: {}",
                envelop.getEventType(), productEvent.getProductId(), snsMessage.getMessageId());

        this.productEventLogRepository.save(this.buildProductEventLog(envelop, productEvent, snsMessage.getMessageId()));

    }

    private ProductEventLog buildProductEventLog(Envelop envelop, ProductEvent productEvent, String messageId){
        long timestamp = Instant.now().toEpochMilli();

        ProductEventLog productEventLog = new ProductEventLog();
        productEventLog.setPk(productEvent.getCode());
        productEventLog.setSk(envelop.getEventType() + "_" + timestamp);
        productEventLog.setEventType(envelop.getEventType());
        productEventLog.setProductId(productEvent.getProductId());
        productEventLog.setUsername(productEvent.getUsername());
        productEventLog.setTimestamp(timestamp);
        productEventLog.setMessageId(messageId);
        productEventLog.setTtl(Instant.now().plus(Duration.ofMinutes(10)).getEpochSecond());

        return productEventLog;
    }



}
