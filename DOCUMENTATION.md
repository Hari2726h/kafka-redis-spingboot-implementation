# Complete Project Tutorial & Documentation

Welcome to your project! Since you generated this with AI and want to understand how it all works, this document is written as a step-by-step tutorial. We will go through the entire project, explaining what every file does, what the code means, and how the application runs.

---

## Part 1: How the Application Runs

This project is a **Full-Stack Application** split into two main parts:
1. **The Backend (Spring Boot / Java):** This runs on port `8080`. It handles the database, business logic, caching, and event messaging.
2. **The Frontend (React / JavaScript):** This runs on port `3000`. It is the user interface that you interact with in your browser.

### The Flow of Data
When you create a new product on the website:
1. The **React Frontend** sends a JSON HTTP request to the **Spring Boot Backend**.
2. The Backend validates the data and saves it to a **MySQL Database**.
3. The Backend updates three different **Caches** (Redis, LRU, Caffeine) so that future reads are lightning fast.
4. The Backend sends a message to **Apache Kafka** (an event broker) saying "A product was created".
5. A background worker (Kafka Consumer) reads that message and saves a record in the `history` database table.

---

## Part 2: The Backend (Java / Spring Boot)

Let's look at the backend files located in `src/main/java/com/example/demo/`.

### 1. The Starting Point
* **`DemoApplication.java`**: This is the main entry point of the Java app. It has a `main()` method.
  * **What it does:** Before starting Spring Boot, it calls `LocalInfraBootstrap.start()`. This is a very cool AI feature: it automatically starts a hidden, embedded version of Redis and Kafka on your computer so you don't have to install them manually!

### 2. Configuration Files (`/config`)
These files use the `@Configuration` annotation, which tells Spring Boot to set up these settings when the app starts.
* **`LocalInfraBootstrap.java`**: The code that checks if Redis and Kafka are running, and if not, starts mini "embedded" versions of them for local testing.
* **`CacheConfig.java`**: Sets up "Caffeine" (a fast local memory cache). It's configured to hold a maximum of 100 search results for 10 minutes.
* **`RedisConfig.java`**: Sets up the connection to Redis. It tells Redis to save our Java objects as "JSON" strings so they are readable.
* **`KafkaConfig.java`**: Sets up Apache Kafka. It defines how to "Produce" (send) and "Consume" (receive) messages. It also automatically creates a message queue called `history-events`.
* **`SwaggerConfig.java`**: Sets up OpenAPI/Swagger. This automatically reads your code and builds a beautiful documentation website (at `http://localhost:8080/swagger-ui/index.html`) where you can test your APIs.

### 3. The Database Tables (`/entity` & `/enums`)
Entities are Java classes that represent tables in your MySQL database. We use the `@Entity` annotation.
* **`Product.java`**: Represents the `products` table. It uses "Lombok" (`@Getter`, `@Setter`) so we don't have to write boring getter/setter code. It has fields for `id`, `name`, `price`, and `status`.
* **`History.java`**: Represents the `history` table. It records audit logs. It has fields for `objectType`, `objectId`, `action`, and `createdAt`.
* **`ProductStatus.java` & `ActionType.java` (Enums)**: Enums are strict lists of allowed values. For example, a product can only be `ACTIVE`, `INACTIVE`, or `OUT_OF_STOCK`. An action can only be `CREATE`, `UPDATE`, or `DELETE`.

### 4. Database Access (`/repository`)
* **`ProductRepository.java` & `HistoryRepository.java`**: These are interfaces that extend `JpaRepository`. Spring Boot magically writes the SQL queries for you! For example, just by writing the method name `findByNameContainingIgnoreCase(String name)`, Spring Boot knows how to write the SQL to search products by name.

### 5. The Business Logic (`/service` & `/cache`)
* **`ProductCacheService.java`**: This handles three levels of caching to make the app incredibly fast:
  1. **Redis Cache**: Used to store individual products (e.g., getting product #5).
  2. **Custom LRU Cache (`ProductLruCache.java`)**: A custom-built cache that stores the complete list of all products. If it gets too full, it deletes the oldest accessed data (Least Recently Used).
  3. **Caffeine Cache**: Used to store the results of search queries.
* **`ProductService.java`**: The core brain. When you ask to create a product, this service:
  1. Saves the product to MySQL using the Repository.
  2. Updates all the caches in `ProductCacheService`.
  3. Calls the Kafka Producer to log the action.
* **`HistoryService.java`**: Simply retrieves the history logs from the database.

### 6. The Kafka Event System (`/kafka`)
Kafka is a messaging system. It allows different parts of an app to communicate asynchronously.
* **`HistoryEvent.java`**: A simple object (DTO) holding the data to be sent (e.g., "Product 5 was created").
* **`HistoryProducer.java`**: Uses `KafkaTemplate` to send the `HistoryEvent` into the `history-events` Kafka topic.
* **`HistoryConsumer.java`**: Uses `@KafkaListener` to constantly listen to the `history-events` topic. When it hears a new message, it saves a new `History` record into the MySQL database. 

### 7. The Web APIs (`/controller` & `/dto`)
Controllers are the gateways. They receive HTTP requests from the React frontend and pass them to the Services.
* **`ProductRequest.java` (DTO)**: Data Transfer Object. It defines what data the user must send to create a product. It uses `@NotBlank` and `@Positive` to ensure the user doesn't send empty names or negative prices.
* **`ProductController.java`**: Maps URLs to functions using annotations like `@GetMapping` and `@PostMapping`. For example, `POST /api/products` triggers the creation of a product.
* **`HistoryController.java`**: Maps `GET /api/history` to return the audit logs.

### 8. Backend Properties (`src/main/resources/`)
* **`application.properties`**: Contains the passwords and URLs to connect to MySQL (`jdbc:mysql://localhost:3306/demo`), Kafka, and Redis. It also tells Hibernate (the database tool) to automatically create tables (`ddl-auto=update`).
* **`logback-spring.xml`**: Configures the terminal output (logs). It saves logs to a file in the `logs/` folder.

---

## Part 3: The Frontend (React / JavaScript)

The frontend is built with React and Vite. It lives in the `/frontend` folder.

### 1. Configuration
* **`package.json`**: Lists all the Javascript libraries the project needs (like React and Vite).
* **`vite.config.js`**: Configures the development server. Very importantly, it contains a `proxy` setting. When React tries to fetch `/api/...`, Vite secretly forwards the request to your Java backend at `http://localhost:8080`.

### 2. The Core Setup
* **`index.html`**: The main webpage. It has an empty `<div id="root"></div>`.
* **`src/main.jsx`**: This file finds the `root` div and injects the entire React application (`<App />`) into it.

### 3. The API Helper
* **`src/api.js`**: Contains simple JavaScript functions like `getProducts()` and `createProduct()`. These use the browser's native `fetch()` command to talk to the Spring Boot REST APIs.

### 4. The Components (`/src/components` & `App.jsx`)
React builds UIs using reusable blocks of code called "Components".
* **`App.jsx`**: The main page layout. 
  * It uses React "Hooks" (`useState`) to store the list of products and the search term. 
  * It uses `useEffect` to automatically load the products from the backend as soon as the page opens.
* **`ProductForm.jsx`**: The form where you type the name and price. When you click submit, it takes the input values and sends them to the backend via `api.js`.
* **`ProductList.jsx`**: Takes the list of products and displays them in a grid. It has buttons to Edit or Delete products.
* **`HistoryList.jsx`**: Fetches the audit logs from the backend and displays them so you can see the Kafka consumer working in real-time.

### 5. Styling
* **`src/styles.css`**: All the CSS that makes the app look beautiful. It uses modern features like CSS Variables (`:root`), Grid layouts (`display: grid`), and blurred glass effects (`backdrop-filter: blur(16px)`).

---

## Conclusion

This project is a highly advanced, modern stack. It isn't just a simple database app; by including Kafka, Redis, Caffeine, and custom LRU caches, the AI built you an application that demonstrates enterprise-level architecture used by companies like Netflix and Uber. 

You can read this document directly in your code editor while looking at the files to understand how everything connects!
