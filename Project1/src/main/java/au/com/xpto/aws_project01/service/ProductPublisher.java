package au.com.xpto.aws_project01.service;

import au.com.xpto.aws_project01.enums.EventType;
import au.com.xpto.aws_project01.model.Envelop;
import au.com.xpto.aws_project01.model.Product;
import au.com.xpto.aws_project01.model.ProductEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(ProductPublisher.class);

    private final AmazonSNS snsClient;
    private final Topic productEventsTopic;
    private final ObjectMapper objectMapper;

    public ProductPublisher(AmazonSNS snsClient, Topic productEventsTopic, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.productEventsTopic = productEventsTopic;
        this.objectMapper = objectMapper;
    }

    public void publishProductEvent(Product product, EventType eventType, String username){
        ProductEvent productEvent = new ProductEvent();
        productEvent.setProductId(product.getId());
        productEvent.setCode(product.getCode());
        productEvent.setUsername(username);

        Envelop envelop = new Envelop();
        envelop.setEventType(eventType);

        try {
            envelop.setData(this.objectMapper.writeValueAsString(productEvent));

            PublishResult publishResult = this.snsClient.publish(this.productEventsTopic.getTopicArn(), objectMapper.writeValueAsString(envelop));

            //Creating 'publishResult.getMessageId()' that will share the same MessageId on Project02. It helps to create a link between these two applications
            //resulting in finding log information that has the same MessageId on cloudwatch >> Insights
            LOG.info("Product event sent - Event: {} - ProductId: {} - MessageId: {}",
                    envelop.getEventType(), productEvent.getProductId(), publishResult.getMessageId());

        } catch (JsonProcessingException e) {
            LOG.error("Failed to create product event message", e);
            e.printStackTrace();
        }
    }


}
