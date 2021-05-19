package au.com.xpto.aws_project02.repository;

import au.com.xpto.aws_project02.model.ProductEventKey;
import au.com.xpto.aws_project02.model.ProductEventLog;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan //Making this (interface) Repository visible to the DynamoDB
public interface ProductEventLogRepository extends CrudRepository<ProductEventLog, ProductEventKey> {

    List<ProductEventLog> findAllByPk(String code);

    List<ProductEventLog> findAllByPkAndSkStartingWith(String code, String eventType);

}
