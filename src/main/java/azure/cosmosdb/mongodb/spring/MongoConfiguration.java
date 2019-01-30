package azure.cosmosdb.mongodb.spring;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "azure.cosmosdb.mongodb.spring")
public class MongoConfiguration {
	@Value("${spring.data.mongodb.uri:mongodb://localhost:27017}")
	private String mongoUri;

	/**
	 * Creates a base instance that can be configured for each
	 * implementation.
	 *
	 * @return A generic instance pointed at the hosts.
	 * @throws Exception
	 */
	private MongoClient createMongo() throws Exception {
		return new MongoClient(new MongoClientURI(mongoUri));
	}

	@Bean
	public MongoClient readFromPrimaryMongo() throws Exception {
		final MongoClient mongo = createMongo();
		return mongo;
	}

	@Bean
	public MongoDbFactory mongoDbFactory(MongoClient mongo) {
		return new SimpleMongoDbFactory(mongo, "springgeo");
	}

	@Bean
	public MongoTemplate mongoTemplate(@Qualifier("readFromPrimaryMongo") MongoClient mongo,
									   MongoDbFactory mongoDbFactory) {
		return new MongoTemplate(mongoDbFactory);
	}
}