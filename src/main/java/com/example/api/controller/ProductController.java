package com.example.api.controller;

import com.example.api.dto.ProductDTO;
import com.example.api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product", description = "Product management APIs")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping
    @Operation(
        summary = "Get all products",
        description = "Retrieves a list of all products"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        logger.info("Récupération des produits");
        List<ProductDTO> products = productService.getAllProducts();
        logger.info("Nombre total de produits: {}", products.size());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search products",
        description = "Search products by name, minimum price, and/or maximum price"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search results",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)
            )
        )
    })
    public ResponseEntity<List<ProductDTO>> searchProducts(
        @Parameter(description = "Product name to search for (case-insensitive)")
        @RequestParam(required = false) String name,
        
        @Parameter(description = "Minimum price")
        @RequestParam(required = false) Double minPrice,
        
        @Parameter(description = "Maximum price")
        @RequestParam(required = false) Double maxPrice
    ) {
        logger.info("Recherche de produits - Nom: {}, Prix min: {}, Prix max: {}", 
                   name, minPrice, maxPrice);
        
        List<ProductDTO> products = productService.searchProducts(name, minPrice, maxPrice);
        
        logger.info("Nombre de produits trouvés: {}", products.size());
        
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @Operation(
        summary = "Create a new product",
        description = "Creates a new product with the provided details including name, description, price and quantity. " +
                     "Validates all input fields before creation."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Product created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "id": 1,
                        "name": "Sample Product",
                        "description": "A detailed description",
                        "price": 29.99,
                        "quantity": 100
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid input - missing or invalid required fields",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "Validation failed",
                        "details": ["Price must be greater than 0", "Name is required"]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ProductDTO> createProduct(
        @Parameter(
            description = "Product to create",
            required = true,
            schema = @Schema(implementation = ProductDTO.class),
            examples = @ExampleObject(
                value = """
                {
                    "name": "Sample Product",
                    "description": "A detailed description",
                    "price": 29.99,
                    "quantity": 100
                }
                """
            )
        )
        @Valid @RequestBody ProductDTO productDTO
    ) {
        logger.info("Création d'un nouveau produit : {}", productDTO.getName());
        ProductDTO createdProduct = productService.createProduct(productDTO);
        logger.info("Produit créé avec succès - ID: {}, Nom: {}", 
                   createdProduct.getId(), createdProduct.getName());
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get a product by its id",
        description = "Retrieves a product's complete details by its unique identifier. " +
                     "Returns 404 if the product is not found."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "Not Found",
                        "message": "Product with id 1 not found"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ProductDTO> getProduct(
        @Parameter(description = "ID of the product to retrieve", required = true, example = "1")
        @PathVariable Long id
    ) {
        logger.info("Recherche du produit avec l'ID: {}", id);
        ProductDTO product = productService.getProduct(id);
        logger.info("Produit trouvé - ID: {}, Nom: {}", product.getId(), product.getName());
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update a product",
        description = "Updates an existing product's information. All fields can be modified. " +
                     "The product must exist, and the input must be valid."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product updated successfully",
            content = @Content(schema = @Schema(implementation = ProductDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input"
        )
    })
    public ResponseEntity<ProductDTO> updateProduct(
        @Parameter(description = "ID of the product to update", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(
            description = "Updated product information",
            required = true,
            schema = @Schema(implementation = ProductDTO.class)
        )
        @Valid @RequestBody ProductDTO productDTO
    ) {
        logger.info("Mise à jour du produit - ID: {}", id);
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        logger.info("Produit mis à jour avec succès - ID: {}, Nouveau nom: {}", 
                   id, updatedProduct.getName());
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a product",
        description = "Permanently removes a product from the system by its ID. " +
                     "Returns 404 if the product is not found."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Product deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public ResponseEntity<Void> deleteProduct(
        @Parameter(description = "ID of the product to delete", required = true, example = "1")
        @PathVariable Long id
    ) {
        logger.info("Suppression du produit - ID: {}", id);
        productService.deleteProduct(id);
        logger.info("Produit supprimé avec succès - ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
