package au.com.xpto.aws_project02.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)//If there is a property I have not mapped in this class, just ignore them.
public class SnsMessage {

    //The purpose of this annotation is to get the value from the field "Message" that comes from the response
    //and set into the attribute String message
    @JsonProperty("Message")
    private String message;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("TopicArn")
    private String topicArn;

    @JsonProperty("Timestamp")
    private String timestamp;

    @JsonProperty("MessageId")
    private String messageId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTopicArn() {
        return topicArn;
    }

    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
