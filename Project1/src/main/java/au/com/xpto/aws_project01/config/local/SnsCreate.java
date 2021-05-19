package au.com.xpto.aws_project01.config.local;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local") //When my profile HAS a value 'local', then I can run it.
public class SnsCreate {

    private static final Logger LOG = LoggerFactory.getLogger(SnsCreate.class);

    private final String productEventsTopic;
    private final AmazonSNS snsClient;

    public SnsCreate() {
        this.snsClient = AmazonSNSClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", Regions.AP_SOUTHEAST_2.getName())
                )
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        CreateTopicRequest topicRequest = new CreateTopicRequest("product-events");
        this.productEventsTopic = this.snsClient.createTopic(topicRequest).getTopicArn();
    }

    @Bean
    public AmazonSNS snsClient(){
        return this.snsClient;
    }

    @Bean(name = "productEventsTopic")
    public Topic snsProductEventsTopic(){
        return new Topic().withTopicArn(productEventsTopic);
    }

}
