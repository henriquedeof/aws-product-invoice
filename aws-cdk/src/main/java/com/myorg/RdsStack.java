package com.myorg;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.rds.*;

import java.util.Collections;

public class RdsStack extends Stack {

    public RdsStack(final Construct scope, final String id, Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public RdsStack(final Construct scope, final String id, final StackProps props, Vpc vpc) {
        super(scope, id, props);

        //Input parameter. It will store the DB password that will be passed to this variable from outside of the application (through CMD, in my example).
        CfnParameter databasePassword = CfnParameter.Builder.create(this, "databasePassword")
                .type("String")
                .description("RDS instance password")
                .build();

        // Creating a new rule on Security Group
        ISecurityGroup iSecurityGroup = SecurityGroup.fromSecurityGroupId(this, id, vpc.getVpcDefaultSecurityGroup());
        iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306));//Just resources that belong to the VPC can access the 3306 port.

        //Defining database. Variable 'databaseInstance' the the RDS instance.
        DatabaseInstance databaseInstance = DatabaseInstance.Builder.create(this, "Rds01")
                .instanceIdentifier("aws-project01-db")
                .engine(
                        DatabaseInstanceEngine.mysql(
                                MySqlInstanceEngineProps.builder().version(MysqlEngineVersion.VER_5_6).build()
                        )
                )
                .vpc(vpc)
                .credentials(
                        Credentials.fromUsername("admin", CredentialsFromUsernameOptions.builder() //User for DB is hardcoded.
                                .password(SecretValue.plainText(databasePassword.getValueAsString())) //databasePassword refers to the CfnParameter databasePassword (Input param)
                                .build()
                        )
                )
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .multiAz(false)
                .allocatedStorage(10) //Size in GB
                .securityGroups(Collections.singletonList(iSecurityGroup)) //Adding the new security group created before on this DB Instance
                .vpcSubnets(SubnetSelection.builder().subnets(vpc.getPrivateSubnets()).build()) //Adding this Instance to the subnet previously created (on VPC).
                .build();

        //Output parameter. Exporting this parameter as other Stacks can read this information from this stack.
        CfnOutput.Builder.create(this, "rds-endpoint")
                .exportName("rds-endpoint")
                .value(databaseInstance.getDbInstanceEndpointAddress())
                .build();

        //Output parameter. Exporting this parameter as other Stacks can read this information from this stack.
        CfnOutput.Builder.create(this, "rds-password")
                .exportName("rds-password")
                .value(databasePassword.getValueAsString()) //databasePassword refers to the CfnParameter databasePassword (Input param)
                .build();

    }



}
