package au.com.xpto.aws_project01.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.Topic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local") //When my profile does NOT have the value 'local', then I can run it.
public class SnsConfig {
    //This class is executed on AWS. For tests, I am using a profile and create an other class.

    @Value("${aws.region}") //Retrieving value from application.properties
    private String awsRegion;

    @Value("${aws.sns.topic.product.events.arn}")
    private String productEventsTopic;

    @Bean
    public AmazonSNS snsClient(){
        return AmazonSNSClientBuilder.standard()
                .withRegion(awsRegion)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    @Bean(name = "productEventsTopic")
    public Topic snsProductEventsTopic(){
        return new Topic().withTopicArn(productEventsTopic);
    }


}
