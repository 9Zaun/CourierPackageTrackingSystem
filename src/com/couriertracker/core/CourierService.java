import java.util.ArrayList;

public class CourierService {
    private String serviceName;
    private Inventory inventory;
    private ArrayList<Customer> customers;

    public CourierService(String serviceName, Inventory inventory, Customer[] customers) {
        this.serviceName = serviceName;
        this.inventory = inventory;
        this.customers = customers;
    }

    public void registerCustomer(CourierService courierService, String customerID, String name, String houseNumber,
     String streetName, String city, String state, String pinCode, String country, Discount discount){

        Customer customer = new Customer(courierService, customerID, name, houseNumber, streetName, city, state, pinCode, country, discount);
        customers.add(customer);
    }

    public Package createPackage(Customer customer, )
}