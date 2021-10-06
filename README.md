# koltin_example_app
This is a simple guideline on the project creation and scaffolding project based on the recommended practice proposed by Springboot ＋ Kotlin. It should be use the microservice architecture to do so. The following tools will be used on this example:

- [Spring Boot](https://spring.io/projects/spring-boot) - Application Framework for Java, Kotlin & Groovy
- [OpenAPI 3](https://www.openapis.org/) - Specification and standardized documentation for REST API
- [REST API](https://restfulapi.net/) - Acronym for Representational State Transfer. It is architectural style for distributed hypermedia systems
- [Spring Data](https://spring.io/projects/spring-data) - A familiar and consistent, Spring-based programming model for data access while still retaining the special traits of the underlying data store
- [PostgreSQL](https://www.postgresql.org/) - A powerful, open source object-relational database system
- [Redis](https://redis.io/) - An open source (BSD licensed), in-memory data structure store, used as a database, cache, and message broker
- [gRPC](https://grpc.io/) - A modern open source high performance Remote Procedure Call (RPC) framework that can run in any environment

## Project Started

Visit [Spring Initializer](https://start.spring.io/), to init the spring boot application with Kotlin.

## Adding Rest Controller

```kotlin
@RestController
@RequestMapping("/api")
class ArticleRestController(private val articleService: ArticleService) {
    @GetMapping("/users")
    fun getAllUsers(): List<UserEntity> =
            userService.findAll()
}
```

## Adding SpringDoc OpenAPI3 Support

Add the package to `build.gradle.kts` 

```yaml
implementation("org.springdoc:springdoc-openapi-ui:1.5.2")
```

Add the Spring Doc config to `application.yml`

```yaml
## Spring Doc
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui-custom.html
    operationsSorter: method
```

Define a configuation class `AppConfig.kt` to customize the bean of OpenApi Spec

```kotlin
@Configuration
class AppConfig {
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .components(Components())
            .info(Info().title("User Service"))
}
```

Define the Rest Controller Tag

```kotlin
@Tag(name = "UserRestController")
class UserRestController(private val userService: UserService) {
	...
}
```
Visit the OpenAPI ui by

```
http://localhost:8280/swagger-ui-custom.html
```

Visit the api doc by

```
http://localhost:8280/api-docs
```


## Adding Spring Data Support

Add the package to `build.gradle.kts` 

```yaml
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
```

Add the spring data and jpa config to `application.yml`

```yaml
## Spring DATASOURCE (DataSourceAutoConfiguration & DataSourceProperties)
spring:
  datasource:
    hikari:
      connectionTimeout: 20000
      maximumPoolSize: 5
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: password
  ## Hibernate Properties
  jpa:
    generate-ddl: true
    ddl-auto: true
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQL81Dialect
```
## Add the docker-compose file to start the postgreSQL service
```yml
postgres:
  container_name: kotlin_example_app_postgres
  environment:
    - POSTGRES_PASSWORD=password
  image: postgres
  ports:
    - 5432:5432
```

Create a JpaRepository

```kotlin
@Repository
interface UserRepository : JpaRepository<UserEntity, Long>
```

Inject the JpaRepository to the way to use

```kotlin
@Service
class UserService(private val userRepository: UserRepository) {
    fun findAll(): List<UserEntity> =
        userRepository.findAll()
}
```

JpaRepository has the below default methods

```kotlin
userRepository.findAll()
userRepository.save(object)
userRepository.findById(id)
userRepository.delete(object)
userRepository.deleteById(id)
...
```
## Adding Redis Support as cache

Add the package to `build.gradle.kts` 

```yaml
    implementation("org.springframework.boot:spring-boot-starter-data-redis:2.5.5")
    implementation("redis.clients:jedis:3.7.0")
```
Add the Spring Doc config to `application.yml`

```yaml
spring:
  ## Redis
  redis:
    host: 127.0.0.1
    port: 6379
    password: password
    ttl: 3600
    tls: false
    database: 0
    pool:
      max-active: 8 
      max-wait: -1 
      max-idle: 8 
      min-idle: 0 
      timeout: 0 
```

Add the Redis config to docker-compose.yml

```yml
redis:
  container_name: kotlin_example_app_redis
  image: redis
  command: redis-server --requirepass password
  ports:
    - 6379:6379
```

Add the Redis config in AppConfig.kt

```kotlin
@Configuration
class AppConfig {
    val redisHost: String = "127.0.0.1"
    
    val redisPort: Int = 6379

    val redisPassword: String = "password"

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        val config: RedisStandaloneConfiguration = RedisStandaloneConfiguration(redisHost, redisPort)
        config.password = RedisPassword.of(redisPassword)
        return JedisConnectionFactory(config)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any>? {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(jedisConnectionFactory())
        return template
    }
}
```

Create a RedisUtil.kt file 

```kotlin
@Component
class RedisUtil {

    @Resource
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    // =============================common============================
    /**
     * 指定緩存失效時間
     * @param key 鍵
     * @param time 時間(秒)
     * @return
     */
    fun expire(key: String, time: Long): Boolean {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 根據key 獲取過期時間
     * @param key 鍵 不能為null
     * @return 時間(秒) 返回0代表為永久有效
     */
    fun getExpire(key: String): Long {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS)
    }

    /**
     * 判斷key是否存在
     * @param key 鍵
     * @return true 存在 false不存在
     */
    fun hasKey(key: String): Boolean {
        try {
            return redisTemplate.hasKey(key)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 刪除緩存
     * @param key 可以傳一個值 或多個
     */
    fun del(vararg key: String) {
        if (key.isNotEmpty()) {
            if (key.size == 1) {
                redisTemplate.delete(key[0])
            } else {
                redisTemplate.delete(key.toList())
            }
        }
    }

    // ============================String=============================
    /**
     * 普通緩存獲取
     * @param key 鍵
     * @return 值
     */
    operator fun get(key: String?): Any? {
        return if (key == null) null else redisTemplate.opsForValue().get(key)
    }

    /**
     * 普通緩存放入
     * @param key 鍵
     * @param value 值
     * @return true成功 false失敗
     */
    operator fun set(key: String, value: Any): Boolean {
        try {
            redisTemplate.opsForValue().set(key, value)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 普通緩存放入並設置時間
     * @param key 鍵
     * @param value 值
     * @param time 時間(秒) time要大於0 如果time小於等於0 將設置無限期
     * @return true成功 false 失敗
     */
    operator fun set(key: String, value: Any, time: Long): Boolean {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS)
            } else {
                set(key, value)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 遞增
     * @param key 鍵
     * @param delta 要增加幾(大於0)
     * @return
     */
    fun incr(key: String, delta: Long): Long {
        if (delta < 0) {
            throw RuntimeException("遞增因子必須大於0")
        }
        return redisTemplate.opsForValue().increment(key, delta)!!
    }

    /**
     * 遞減
     * @param key 鍵
     * @param delta 要減少幾(小於0)
     * @return
     */
    fun decr(key: String, delta: Long): Long {
        if (delta < 0) {
            throw RuntimeException("遞減因子必須大於0")
        }
        return redisTemplate.opsForValue().increment(key, -delta)!!
    }

    // ================================Map=================================
    /**
     * HashGet
     * @param key 鍵 不能為null
     * @param item 項 不能為null
     * @return 值
     */
    fun hget(key: String, item: String): Any? {
        return redisTemplate.opsForHash<Any, Any>().get(key, item)
    }

    /**
     * 獲取hashKey對應的所有鍵值
     * @param key 鍵
     * @return 對應的多個鍵值
     */
    fun hmget(key: String): Map<Any, Any> {
        return redisTemplate.opsForHash<Any, Any>().entries(key)
    }

    /**
     * HashSet
     * @param key 鍵
     * @param map 對應多個鍵值
     * @return true 成功 false 失敗
     */
    fun hmset(key: String, map: Map<String, Any>): Boolean {
        try {
            redisTemplate.opsForHash<Any, Any>().putAll(key, map)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * HashSet 並設置時間
     * @param key 鍵
     * @param map 對應多個鍵值
     * @param time 時間(秒)
     * @return true成功 false失敗
     */
    fun hmset(key: String, map: Map<String, Any>, time: Long): Boolean {
        try {
            redisTemplate.opsForHash<Any, Any>().putAll(key, map)
            if (time > 0) {
                expire(key, time)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 向一張hash表中放入數據,如果不存在將創建
     * @param key 鍵
     * @param item 項
     * @param value 值
     * @return true 成功 false失敗
     */
    fun hset(key: String, item: String, value: Any): Boolean {
        try {
            redisTemplate.opsForHash<Any, Any>().put(key, item, value)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 向一張hash表中放入數據,如果不存在將創建
     * @param key 鍵
     * @param item 項
     * @param value 值
     * @param time 時間(秒) 注意:如果已存在的hash表有時間,這裡將會替換原有的時間
     * @return true 成功 false失敗
     */
    fun hset(key: String, item: String, value: Any, time: Long): Boolean {
        try {
            redisTemplate.opsForHash<Any, Any>().put(key, item, value)
            if (time > 0) {
                expire(key, time)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 刪除hash表中的值
     * @param key 鍵 不能為null
     * @param item 項 可以使多個 不能為null
     */
    fun hdel(key: String, vararg item: Any) {
        redisTemplate.opsForHash<Any, Any>().delete(key, *item)
    }

    /**
     * 判斷hash表中是否有該項的值
     * @param key 鍵 不能為null
     * @param item 項 不能為null
     * @return true 存在 false不存在
     */
    fun hHasKey(key: String, item: String): Boolean {
        return redisTemplate.opsForHash<Any, Any>().hasKey(key, item)
    }

    /**
     * hash遞增 如果不存在,就會創建一個 並把新增後的值返回
     * @param key 鍵
     * @param item 項
     * @param by 要增加幾(大於0)
     * @return
     */
    fun hincr(key: String, item: String, by: Double): Double {
        return redisTemplate.opsForHash<Any, Any>().increment(key, item, by)
    }

    /**
     * hash遞減
     * @param key 鍵
     * @param item 項
     * @param by 要減少記(小於0)
     * @return
     */
    fun hdecr(key: String, item: String, by: Double): Double {
        return redisTemplate.opsForHash<Any, Any>().increment(key, item, -by)
    }

    // ============================set=============================
    /**
     * 根據key獲取Set中的所有值
     * @param key 鍵
     * @return
     */
    fun sGet(key: String): Set<Any>? {
        try {
            return redisTemplate.opsForSet().members(key)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    /**
     * 根據value從一個set中查詢,是否存在
     * @param key 鍵
     * @param value 值
     * @return true 存在 false不存在
     */
    fun sHasKey(key: String, value: Any): Boolean {
        try {
            return redisTemplate.opsForSet().isMember(key, value)!!
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 將數據放入set緩存
     * @param key 鍵
     * @param values 值 可以是多個
     * @return 成功個數
     */
    fun sSet(key: String, vararg values: Any): Long {
        try {
            return redisTemplate.opsForSet().add(key, *values)!!
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }

    }

    /**
     * 將set數據放入緩存
     * @param key 鍵
     * @param time 時間(秒)
     * @param values 值 可以是多個
     * @return 成功個數
     */
    fun sSetAndTime(key: String, time: Long, vararg values: Any): Long {
        try {
            val count = redisTemplate.opsForSet().add(key, *values)
            if (time > 0)
                expire(key, time)
            return count!!
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }

    }

    /**
     * 獲取set緩存的長度
     * @param key 鍵
     * @return
     */
    fun sGetSetSize(key: String): Long {
        try {
            return redisTemplate.opsForSet().size(key)!!
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }

    }

    /**
     * 移除值為value的
     * @param key 鍵
     * @param values 值 可以是多個
     * @return 移除的個數
     */
    fun setRemove(key: String, vararg values: Any): Long {
        try {
            val count = redisTemplate.opsForSet().remove(key, *values)
            return count!!
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }

    }
    // ===============================list=================================

    /**
     * 獲取list緩存的內容
     * @param key 鍵
     * @param start 開始
     * @param end 結束 0 到 -1代表所有值
     * @return
     */
    fun lGet(key: String, start: Long, end: Long): List<Any>? {
        try {
            return redisTemplate.opsForList().range(key, start, end)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    /**
     * 獲取list緩存的長度
     * @param key 鍵
     * @return
     */
    fun lGetListSize(key: String): Long {
        try {
            return redisTemplate.opsForList().size(key)!!
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }

    }

    /**
     * 通過索引 獲取list中的值
     * @param key 鍵
     * @param index 索引 index>=0時， 0 表頭，1 第二個元素，依次類推；index<0時，-1，表尾，-2倒數第二個元素，依次類推
     * @return
     */
    fun lGetIndex(key: String, index: Long): Any? {
        try {
            return redisTemplate.opsForList().index(key, index)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    /**
     * 將list放入緩存
     * @param key 鍵
     * @param value 值
     * @param time 時間(秒)
     * @return
     */
    fun lSet(key: String, value: Any): Boolean {
        try {
            redisTemplate.opsForList().rightPush(key, value)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 將list放入緩存
     * @param key 鍵
     * @param value 值
     * @param time 時間(秒)
     * @return
     */
    fun lSet(key: String, value: Any, time: Long): Boolean {
        try {
            redisTemplate.opsForList().rightPush(key, value)
            if (time > 0)
                expire(key, time)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 將list放入緩存
     * @param key 鍵
     * @param value 值
     * @param time 時間(秒)
     * @return
     */
    fun lSet(key: String, value: List<Any>): Boolean {
        try {
            redisTemplate.opsForList().rightPushAll(key, *value.toTypedArray())
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 將list放入緩存
     *
     * @param key 鍵
     * @param value 值
     * @param time 時間(秒)
     * @return
     */
    fun lSet(key: String, value: List<Any>, time: Long): Boolean {
        try {
            redisTemplate.opsForList().rightPushAll(key, *value.toTypedArray())
            if (time > 0)
                expire(key, time)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 根據索引修改list中的某條數據
     * @param key 鍵
     * @param index 索引
     * @param value 值
     * @return
     */
    fun lUpdateIndex(key: String, index: Long, value: Any): Boolean {
        try {
            redisTemplate.opsForList().set(key, index, value)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 移除N個值為value
     * @param key 鍵
     * @param count 移除多少個
     * @param value 值
     * @return 移除的個數
     */
    fun lRemove(key: String, count: Long, value: Any): Long {
        try {
            val remove = redisTemplate.opsForList().remove(key, count, value)
            return remove!!
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }

    }
}
```

Use the RedisUtil for caching

```koltin
@Service
class UserService(private  val userRepository: UserRepository) {
    val key = "user"
    val mapper = jacksonObjectMapper()

    //@Value("\${spring.redis.ttl}")
    val testkey: Long = 3600;

    @Resource
    private lateinit var redisUtil: RedisUtil

    fun findAll(): List<UserEntity> =
        userRepository.findAll()

    fun save(@Valid createUserDto: CreateUserDto): UserEntity {
        val userEntity: UserEntity = mapper.convertValue<UserEntity>(createUserDto)
        val result = userRepository.save(userEntity)
        Thread {
            redisUtil.hset(key, userEntity.id.toString(), mapper.writeValueAsString(result), testkey)
        }.start()
        return result
    }

    fun findById(userId: Long): ResponseEntity<UserEntity> {
        val redisResult = redisUtil.hget(key, userId.toString())
        if (redisResult != null) {
            val result = mapper.readValue(redisResult.toString(), UserEntity::class.java)
            return ResponseEntity.ok(result)
        } else
            return userRepository.findById(userId).map { result ->
                redisUtil.hset(key, userId.toString(), mapper.writeValueAsString(result), testkey)
                ResponseEntity.ok(result)
            }.orElse(ResponseEntity.notFound().build())
    }

    fun update(
        userId: Long,
        @Valid updateUserDto: UpdateUserDto
    ): ResponseEntity<UserEntity> {
        redisUtil.hdel(key, userId.toString())
        return userRepository.findById(userId).map { existingUser ->
            val updatedUser: UserEntity = existingUser
                .copy(
                    userName = if (updateUserDto?.userName != null) updateUserDto.userName else existingUser.userName,
                    password = if (updateUserDto?.password != null) updateUserDto.password else existingUser.password
                )

            ResponseEntity.ok().body(userRepository.save(updatedUser))
        }.orElse(ResponseEntity.notFound().build())
    }

    fun delete(articleId: Long): ResponseEntity<Void> {
        redisUtil.hdel(key, articleId.toString())
        return userRepository.findById(articleId).map { article ->
            userRepository.delete(article)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }
}
```

## How to run the docker compose file?

**Start Service**
```
docker-compose up
```

**Shutdown Services**
```
docker-compose down
```
