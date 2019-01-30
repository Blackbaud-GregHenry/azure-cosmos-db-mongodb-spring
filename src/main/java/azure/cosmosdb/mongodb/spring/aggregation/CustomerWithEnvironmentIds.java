package azure.cosmosdb.mongodb.spring.aggregation;

import java.util.List;

public class CustomerWithEnvironmentIds {

    private Status status;
    private String lastName;
    private Long total;
    private List<String> environmentIds;

    public CustomerWithEnvironmentIds(Status status, String lastName, Long total, List<String> environmentIds) {
        this.status = status;
        this.lastName = lastName;
        this.total = total;
        this.environmentIds = environmentIds;
    }
}
