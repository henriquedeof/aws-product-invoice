package com.myorg;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;

import java.util.HashMap;
import java.util.Map;


public class Service02Stack extends Stack {

    public Service02Stack(final Construct scope, final String id, Cluster cluster, SnsTopic productEventsTopic, Table productEventsDdb) {
        this(scope, id, null, cluster, productEventsTopic, productEventsDdb);
    }

    public Service02Stack(final Construct scope, final String id, final StackProps props, Cluster cluster, SnsTopic productEventsTopic, Table productEventsDdb) {
        super(scope, id, props);

        //I am creating a SQS inside of this Stack but I should create it as a new Class and then pass it to this class as an argument.

        //Creating Dead Letter Queue: Items that were not retrieved in 3 attempts (for business logic or even exception), must go to this list
        Queue productEventsDlq = Queue.Builder.create(this, "ProductEventsDlq").queueName("product-events-dlq").build();
        DeadLetterQueue deadLetterQueue = DeadLetterQueue.builder().queue(productEventsDlq).maxReceiveCount(3).build();

        //Creating the main queue
        Queue productEventsQueue = Queue.Builder.create(this, "ProductEvents").queueName("product-events").deadLetterQueue(deadLetterQueue).build();

        //Adding subscription
        SqsSubscription sqsSubscription = SqsSubscription.Builder.create(productEventsQueue).build();
        productEventsTopic.getTopic().addSubscription(sqsSubscription);


        //This map stores all the information to access the DB using Spring.
        Map<String, String> envVariables = new HashMap<>();
        envVariables.put("AWS_REGION", "ap-southeast-2");
        envVariables.put("AWS_SQS_QUEUE_PRODUCT_EVENTS_NAME", productEventsQueue.getQueueName());

        ApplicationLoadBalancedFargateService service02 = ApplicationLoadBalancedFargateService.Builder.create(this, "ALB02")
                .serviceName("service-02") //This name will appear on cluster
                .cluster(cluster)
                .cpu(512)
                .memoryLimitMiB(1024)
                .desiredCount(2) //Qty of instances at the moment of initialization
                .listenerPort(9090) //Port for external access

                //Specifying what image will be used.
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("aws_project02")
                                .image(ContainerImage.fromRegistry("henriquedeof/curso_aws_project02:1.5.0")) //Application image (Docker Hub)
                                .containerPort(9090) //Call 9090 port of my application on Docker
                                // Lines below set where the logs will be found. In this case, CloudWatch
                                .logDriver(
                                        LogDriver.awsLogs(
                                                AwsLogDriverProps.builder().logGroup(
                                                        LogGroup.Builder.create(this, "Service02LogGroup")
                                                        .logGroupName("Log-Service02") //Name that group logs
                                                        .removalPolicy(RemovalPolicy.DESTROY)
                                                        .build()
                                                )
                                                .streamPrefix("Log-Service02") //Small files retrieved in a period of time
                                                .build()
                                        )//LogDriver.awsLogs
                                ) //logDriver end
                                .environment(envVariables)//Setting the Environment variables that will be used by the Spring application.
                                .build()//ApplicationLoadBalancedTaskImageOptions
                )
                .publicLoadBalancer(true)
                .build();//service02

        //Code could be in a different method.
        service02.getTargetGroup().configureHealthCheck(
                new HealthCheck.Builder()
                    .path("/actuator/health")
                    .port("9090")
                    .healthyHttpCodes("200")
                    .build()
        );


        ScalableTaskCount scalableTaskCount = service02.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build());

        scalableTaskCount.scaleOnCpuUtilization("Service02AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());

        productEventsQueue.grantConsumeMessages(service02.getTaskDefinition().getTaskRole());//Adding permission to consume messages from this queue.
        productEventsDdb.grantReadWriteData(service02.getTaskDefinition().getTaskRole());//Granting access to to write and read the DynamoDB table.

    }


}
