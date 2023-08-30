# Ktor Query Params Library

The **ktor-query-params** library consists of two modules that provide defined handling of query parameters and automatic generation of Swagger definitions for Ktor endpoints.

## Modules

### ktor-query-params

The **ktor-query-params** module introduces a convenient way to define and manage query parameters and responses in Ktor applications. It enables you to pre-declare query parameters with explicit type checks and conversions, ensuring that your code adheres to the specified contract.

#### Features

- Declare query parameters and responses in advance.
- Ensure type correctness and automatic conversions.
- Extensions to access parameters in an easier way within a call (`param.get()`, `response.send()`)

#### Usage Example

```kotlin
routing {
    route("/test") {
        val stringParam = stringParam("string") // String?
        val intParam = intParam("int") // Int?
        val boolParam = boolParam("bool") // Bool?
        val requiredParam = stringParam("required")
            .required() // String
        val manyParam = stringParam("manyParam").many() // Set<String>
        val enumParam = enumParam<TestEnum>("enum") // TestEnum?
        val localDateParam = localDateParam("localDate") // LocalDate?

        val okResult = responds<Result>()
        
        // Custom extensions to simplify handling
        doGet {
            okResult.send(
                Result(
                    stringParam.get(),
                    intParam.get(),
                    boolParam.get(),
                    requiredParam.get(),
                    manyParam.get(),
                    enumParam.get(),
                    localDateParam.get()
                )
            )
        }
        
        // Ktor native alternative
        post {
            call.send(okResult, Result(
                call.get(stringParam),
                // ...
            ))
        }
    }
}
```

### ktor-query-params-openapi-generator

The **ktor-query-params-openapi-generator** module wraps the `io.swagger:swagger-core` library and facilitates the automatic generation of Swagger definitions for Ktor endpoints that utilize query parameters defined with the prior module.

#### Features

- Generate Swagger definitions for Ktor endpoints with query parameters.
- Keep API documentation aligned with query parameter declarations.

#### Usage Example

```kotlin
install(KtorOpenapiPlugin) {
    swagger.info = Info().apply {
        version = "1.0.0"
        title = "Test API"
    }
}

routing {
    route("/test") {
        val stringParam = stringParam("string")
            .description("A string param")

        val okResult = responds<Result>()

        openApi {
            get.tags = listOf("tag1", "tag2")
        }
        doGet {
            okResult.send(
                Result(
                    stringParam.get()
                )
            )
        }
    }
    route("/exlude") {
        excludeFromApi()
        get { /* ... */ }
    }
    // the definition will be accessible via a GET request to swagger.json
    swaggerJsonRoute()
}
```

## Contributions

Contributions to the **ktor-query-params** library are welcome! If you find any issues or have suggestions for improvements, please open an issue or submit a pull request on the [GitHub repository](https://github.com/Vladiatro/ktor-query-params).

## License

This project is licensed under the [Apache License 2.0](LICENSE).