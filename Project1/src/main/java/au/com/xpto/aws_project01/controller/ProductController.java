package au.com.xpto.aws_project01.controller;

import au.com.xpto.aws_project01.enums.EventType;
import au.com.xpto.aws_project01.model.Product;
import au.com.xpto.aws_project01.repository.ProductRepository;
import au.com.xpto.aws_project01.service.ProductPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductPublisher productPublisher;

    public ProductController(ProductRepository productRepository, ProductPublisher productPublisher) {
        this.productRepository = productRepository;
        this.productPublisher = productPublisher;
    }

    @GetMapping
    public List<Product> findAll(){
        return this.productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable long id){
        return this.productRepository.findById(id).map(product -> new ResponseEntity(product, HttpStatus.OK)).orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody Product product){
        Product productCreated = productRepository.save(product);

        productPublisher.publishProductEvent(productCreated, EventType.PRODUCT_CREATED, "username-create");

        return new ResponseEntity<>(productCreated, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@RequestBody Product product, @PathVariable("id") long id){
        if(this.productRepository.existsById(id)){
            product.setId(id);
            Product ProductUpdated = this.productRepository.save(product);

            productPublisher.publishProductEvent(ProductUpdated, EventType.PRODUCT_UPDATE, "username-update");

            return new ResponseEntity<>(ProductUpdated, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable long id){
        return this.productRepository.findById(id).map(product -> {
            this.productRepository.delete(product);

            productPublisher.publishProductEvent(product, EventType.PRODUCT_DELETED, "username-delete");

            return new ResponseEntity(product, HttpStatus.OK);//I could use HttpStatus.NO_CONTENT as well.

        }).orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/bycode")
    public ResponseEntity<Product> findById(@RequestParam String code){
        return this.productRepository.findByCode(code).map(product -> new ResponseEntity(product, HttpStatus.OK)).orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND));
    }



}
