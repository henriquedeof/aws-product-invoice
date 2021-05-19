package au.com.xpto.aws_project02.model;

import au.com.xpto.aws_project02.enums.EventType;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import org.springframework.data.annotation.Id;

@DynamoDBTable(tableName = "product-events")
//product-events is the name of the Dynamo table that was created on the cdk-project. They must match.
public class ProductEventLog {

    //This class is mapped on DynamoDB.

    public ProductEventLog() {
    }

    @Id
    //I can't have get and set for this @Id attribute. To do so, I have to use the pk and sk. See the get and set below.
    private ProductEventKey productEventKey;

    @DynamoDBTypeConvertedEnum
    //Maintaining this attribute as Enum. This annotation converts the value of this Enum into String
    @DynamoDBAttribute(attributeName = "eventType")
    //Annotation not needed as the attributeName has the same name as the attribute.
    private EventType eventType;

    @DynamoDBAttribute(attributeName = "productId")
    private long productId;

    @DynamoDBAttribute(attributeName = "username")
    private String username;

    @DynamoDBAttribute(attributeName = "timestamp")
    private long timestamp; //value in Timestamp. It defines when the item on Dynamo should be destroyed.

    @DynamoDBAttribute(attributeName = "ttl")
    private long ttl;

    @DynamoDBAttribute(attributeName = "messageId")
    private String messageId;

    @DynamoDBHashKey(attributeName = "pk")
    public String getPk() {
        return this.productEventKey != null ? this.productEventKey.getPk() : null;
    }

    public void setPk(String pk) {
        if (this.productEventKey == null) {
            this.productEventKey = new ProductEventKey();
        }
        this.productEventKey.setPk(pk);
    }

    @DynamoDBRangeKey(attributeName = "sk")
    public String getSk() {
        return this.productEventKey != null ? this.productEventKey.getSk() : null;
    }

    public void setSk(String sk) {
        if (this.productEventKey == null) {
            this.productEventKey = new ProductEventKey();
        }
        this.productEventKey.setSk(sk);
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
