import java.util.UUID;

public class Customer {
    private CourierService courierService;
    private String customerID;
    private String name;
    private String address;
    private String houseNumber;
    private String streetName;
    private String city;
    private String state;
    private String pinCode;
    private String country;
    private Discount discount;

    public Customer(CourierService courierService, String name, String houseNumber,
     String streetName, String city, String state, String pinCode, String country, Discount discount) {
        this.courierService = courierService;
        this.customerID = UUID.randomUUID().toString();
        this.name = name;
        this.houseNumber = houseNumber;
        this.streetName = streetName;
        this.city = city;
        this.state = state;
        this.pinCode = pinCode;
        this.country = country;
        this.address = houseNumber + " " + streetName + ", " + city + ", " + state + ", " + pinCode + ", " + country;
        this.discount = discount;
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public void createPackage(String receiver, String destination){
        courierService.createPackage(this, receiver, destination);
    }

    public void viewPackageStatus(String packageID){
        package = courierService.trackPackage(packageID);
        package.viewPackageStatus();
    }

    
    
}