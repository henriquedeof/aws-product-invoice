package com.myorg;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.sqs.Queue;

import java.util.HashMap;
import java.util.Map;


public class Service01Stack extends Stack {

    public Service01Stack(final Construct scope, final String id, Cluster cluster, SnsTopic productEventsTopic, Bucket invoiceBucket, Queue invoiceQueue) {
        this(scope, id, null, cluster, productEventsTopic, invoiceBucket, invoiceQueue);
    }

    public Service01Stack(final Construct scope, final String id, final StackProps props, Cluster cluster,
                          SnsTopic productEventsTopic, Bucket invoiceBucket, Queue invoiceQueue) {
        super(scope, id, props);

        //This map stores all the information to access the DB using Spring.
        Map<String, String> envVariables = new HashMap<>();

        //Fn.importValue("rds-endpoint") is the output parameter created on the RDS Stack that exports the MySQL URL and can be accessed through any other Stack.
        envVariables.put("SPRING_DATASOURCE_URL", "jdbc:mariadb://" + Fn.importValue("rds-endpoint") + ":3306/aws_project01?createDatabaseIfNotExist=true");
        envVariables.put("SPRING_DATASOURCE_USERNAME", "admin"); //Hardcoded because justcdk  for studying purposes.
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password")); //Getting this info from the RDS Stack.
        envVariables.put("AWS_REGION", "ap-southeast-2");
        envVariables.put("AWS_SNS_TOPIC_PRODUCT_EVENTS_ARN", productEventsTopic.getTopic().getTopicArn());

        envVariables.put("AWS_S3_BUCKET_INVOICE_NAME", invoiceBucket.getBucketName());
        envVariables.put("AWS_SQS_QUEUE_INVOICE_EVENTS_NAME", invoiceQueue.getQueueName());

        //SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME and SPRING_DATASOURCE_PASSWORD are Environment Variables that already exist on Spring.


        ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService.Builder.create(this, "ALB01")
                .serviceName("service-01") //This name will appear on cluster
                .cluster(cluster)
                .cpu(512)
                .memoryLimitMiB(1024)
                .desiredCount(2) //Qty of instances at the moment of initialization
                .listenerPort(8080) //Port for external access

                //Specifying what image will be used.
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("aws_project01")
                                .image(ContainerImage.fromRegistry("henriquedeof/curso_aws_project01:2.7.0")) //Application image (Docker Hub)
                                .containerPort(8080) //Call 8080 port of my application on Docker
                                // Lines below set where the logs will be found. In this case, CloudWatch
                                .logDriver(
                                        LogDriver.awsLogs(
                                                AwsLogDriverProps.builder().logGroup(
                                                        LogGroup.Builder.create(this, "Service01LogGroup")
                                                        .logGroupName("Log-Service01") //Name that group logs
                                                        .removalPolicy(RemovalPolicy.DESTROY)
                                                        .build()
                                                )
                                                .streamPrefix("Log-Service01") //Small files retrieved in a period of time
                                                .build()
                                        )//LogDriver.awsLogs
                                ) //logDriver end
                                .environment(envVariables)//Setting the Environment variables that will be used by the Spring application.
                                .build()//ApplicationLoadBalancedTaskImageOptions
                )
                .publicLoadBalancer(true)
                .build();//service01

        //Code could be in a different method.
        service01.getTargetGroup().configureHealthCheck(
                new HealthCheck.Builder()
                    .path("/actuator/health")
                    .port("8080")
                    .healthyHttpCodes("200")
                    .build()
        );


        ScalableTaskCount scalableTaskCount = service01.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build());

        scalableTaskCount.scaleOnCpuUtilization("Service01AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());

        productEventsTopic.getTopic().grantPublish(service01.getTaskDefinition().getTaskRole());//Adding permission to add messages on the topics.

        invoiceQueue.grantConsumeMessages(service01.getTaskDefinition().getTaskRole());
        invoiceBucket.grantReadWrite(service01.getTaskDefinition().getTaskRole());


    }


}
