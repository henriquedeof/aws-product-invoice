package com.myorg;

import software.amazon.awscdk.core.App;

public class AwsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        // If you don't specify 'env', this stack will be environment-agnostic.
        // Account/Region-dependent features and context lookups will not work,
        // but a single synthesized template can be deployed anywhere.
        // new AwsCdkStack(app, "AwsCdkStack");

        // Replace the above stack intialization with the following to specialize
        // this stack for the AWS Account and Region that are implied by the current
        // CLI configuration.
        /*
        new AwsCdkStack(app, "AwsCdkStack", StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build());
        */

        // Replace the above stack initialization with the following if you know exactly
        // what Account and Region you want to deploy the stack to.
        /*
        new AwsCdkStack(app, "AwsCdkStack", StackProps.builder()
                .env(Environment.builder()
                        .account("123456789012")
                        .region("us-east-1")
                        .build())

                .build());
        */
        // For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html


        VpcStack vpcStack = new VpcStack(app, "Vpc");

        ClusterStack clusterStack = new ClusterStack(app, "Cluster", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        RdsStack rdsStack = new RdsStack(app, "Rds", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack);

        SnsStack snsStack = new SnsStack(app, "Sns");

        InvoiceAppStack invoiceAppStack = new InvoiceAppStack(app, "InvoiceApp");

        Service01Stack service01Stack = new Service01Stack(app, "Service01", clusterStack.getCluster(),
                snsStack.getProductEventsTopic(), invoiceAppStack.getBucket(), invoiceAppStack.getS3InvoiceQueue());
        service01Stack.addDependency(clusterStack);
        service01Stack.addDependency(rdsStack);
        service01Stack.addDependency(snsStack);
        service01Stack.addDependency(invoiceAppStack);

        DdbStack ddbStack = new DdbStack(app, "Ddb");

        Service02Stack service02Stack = new Service02Stack(app, "Service02", clusterStack.getCluster(), snsStack.getProductEventsTopic(), ddbStack.getProductEventsDdb());
        service02Stack.addDependency(clusterStack);
        service02Stack.addDependency(snsStack);
        service02Stack.addDependency(ddbStack);

        //To run the application via cdk deploy, execute the command: cdk deploy --parameters Rds:databasePassword=mypassword Vpc Cluster Ddb Sns Rds Service01 Service02

        app.synth();
    }
}
