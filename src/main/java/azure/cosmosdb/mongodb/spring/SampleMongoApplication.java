/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package azure.cosmosdb.mongodb.spring;

import azure.cosmosdb.mongodb.spring.aggregation.CustomerWithEnvironmentIds;
import azure.cosmosdb.mongodb.spring.aggregation.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;

@SpringBootApplication
public class SampleMongoApplication implements CommandLineRunner {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	public void run(String... args) {
		if (args.length == 0) {
			doMongoBasicOperations();
		} else if (args.length == 1) {
			doMongoAggregationOperations();
		} else  {
			throw new RuntimeException("usage: no args for basic mongo, 'aggregate' for mongo aggregations");
		}
	}

	private void doMongoBasicOperations() {
		// Use WriteRepository to do some write operations
		customerRepository.deleteAll();

		// save a couple of customers
		customerRepository.save(new Customer("Alice", "Smith"));
		customerRepository.save(new Customer("Bob", "Smith"));

		// Use ReadRepository to do some read operations
		// fetch all customers
		System.out.println("Customers found with findAll():");
		System.out.println("-------------------------------");
		for (Customer customer : customerRepository.findAll()) {
			System.out.println(customer);
		}
		System.out.println();

		// fetch an individual customer
		System.out.println("Customer found with findByFirstName('Alice'):");
		System.out.println("--------------------------------");
		System.out.println(customerRepository.findByFirstName("Alice"));

		System.out.println("Customers found with findByLastName('Smith'):");
		System.out.println("--------------------------------");
		for (Customer customer : customerRepository.findByLastName("Smith")) {
			System.out.println(customer);
		}

		List<Customer> allCustomers = customerRepository.findAll();

		for (Customer customer : allCustomers) {
			System.out.println(customer);
		}
	}

	private void doMongoAggregationOperations() {
		/*
		Note:
		this times out on a non-sharded collection at 10k envIds and 100k customers

		must be run on a sharded collection to get the `reply message length` exception:

		Caused by: com.mongodb.MongoInternalException: The reply message length 9490041 is less than the maximum message length 4194304
		 */
		List<Customer> customers = new ArrayList<Customer>();

		List<String> environmentIds = new ArrayList<String>();
		for (int i = 0; i < 10000; i++) {
			environmentIds.add(UUID.randomUUID().toString());
		}
		Random random = new Random();
		List<Status> statuses = Arrays.asList(Status.values());
		for (int i = 0; i < 100000; i++) {
			customers.add(new Customer(UUID.randomUUID().toString(),
									   "lastName",
									   environmentIds.get(random.nextInt(environmentIds.size())),
									   statuses.get(random.nextInt(statuses.size()))));
		}
		customerRepository.saveAll(customers);

		Criteria matchLastNameCriteria = Criteria.where("lastName").is("lastName");
		MatchOperation matchOperation = Aggregation.match(matchLastNameCriteria);
		GroupOperation groupOperation = group("status", "lastName")
				.count().as("total")
				.push("$environmentId").as("environmentIds");
		try {
			List<CustomerWithEnvironmentIds> results = mongoTemplate.aggregate(Aggregation.newAggregation(
					matchOperation,
					groupOperation
			), Customer.class, CustomerWithEnvironmentIds.class).getMappedResults();

			System.out.println("Aggregation successful! number of results:" + results.size());
			for (CustomerWithEnvironmentIds groupedCustomers : results) {
				System.out.println(groupedCustomers);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleMongoApplication.class, args);
	}

}
