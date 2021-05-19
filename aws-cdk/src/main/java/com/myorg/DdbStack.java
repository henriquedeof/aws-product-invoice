package com.myorg;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.dynamodb.*;

public class DdbStack extends Stack {

    private final Table productEventsDdb;

    public DdbStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public DdbStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        this.productEventsDdb = Table.Builder.create(this, "ProductEventsDb")
                .tableName("product-events")
                //.billingMode(BillingMode.PAY_PER_REQUEST)
                .billingMode(BillingMode.PROVISIONED) //provisioned mode, delimit read/write on th table and avoid extra money charge
                .readCapacity(1) //I should only use this attribute when BillingMode.PROVISIONED. Otherwise, remove it.
                .writeCapacity(1) ////I should only use this attribute when BillingMode.PROVISIONED. Otherwise, remove it.
                //My primary key is the combination of the 'Partition Key and Sort Key'.
                .partitionKey(Attribute.builder()
                        .name("pk")// I can give any name for this partition key (pk)
                        .type(AttributeType.STRING)
                        .build()
                )
                .sortKey(Attribute.builder()
                        .name("sk")
                        .type(AttributeType.STRING)
                        .build()
                )
                .timeToLiveAttribute("ttl")
                .removalPolicy(RemovalPolicy.DESTROY) //If my stack is destroyed, my table will also be (for studies). In real world it is normally used RETAIN
                .build();


        //Adjusting auto-scaling. ONLY USED, when billingMode(BillingMode.PROVISIONED). Otherwise, delete this configuration
        this.productEventsDdb.autoScaleReadCapacity(
                EnableScalingProps.builder().minCapacity(1).maxCapacity(4).build()
        )
        .scaleOnUtilization(UtilizationScalingProps.builder()
                .targetUtilizationPercent(50) //When 50% of utilization, start scaling.
                .scaleInCooldown(Duration.seconds(30)) //Period after a scale in  activity completes before another scale in   activity can start.
                .scaleOutCooldown(Duration.seconds(30)) //Period after a scale out activity completes before another scale out activity can start.
                .build()
        );

        //Adjusting auto-scaling. ONLY USED, when billingMode(BillingMode.PROVISIONED). Otherwise, delete this configuration
        this.productEventsDdb.autoScaleWriteCapacity(
                EnableScalingProps.builder().minCapacity(1).maxCapacity(4).build()
        )
                .scaleOnUtilization(UtilizationScalingProps.builder()
                        .targetUtilizationPercent(50) //When 50% of utilization, start scaling.
                        .scaleInCooldown(Duration.seconds(30)) //Period after a scale in  activity completes before another scale in   activity can start.
                        .scaleOutCooldown(Duration.seconds(30)) //Period after a scale out activity completes before another scale out activity can start.
                        .build()
                );

    }

    public Table getProductEventsDdb() {
        return productEventsDdb;
    }
}
