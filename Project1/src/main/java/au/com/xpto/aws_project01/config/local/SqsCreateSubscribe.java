package au.com.xpto.aws_project01.config.local;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local") //When my profile HAS a value 'local', then I can run it.
public class SqsCreateSubscribe {

    private static final Logger LOG = LoggerFactory.getLogger(SqsCreateSubscribe.class);


    public SqsCreateSubscribe(AmazonSNS snsClient, Topic productEventsTopic) {
        AmazonSQS sqsClient = AmazonSQSClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", Regions.AP_SOUTHEAST_2.getName())
                )
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        //Creating SQS
        String productEventsQueueUrl = sqsClient.createQueue(new CreateQueueRequest("product-events")).getQueueUrl();

        //Subscribing SQS into a SNS Topic
        Topics.subscribeQueue(snsClient, sqsClient, productEventsTopic.getTopicArn(), productEventsQueueUrl);
    }
}
