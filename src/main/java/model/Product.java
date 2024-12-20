package model;

public class Product {
    private String productId;
    private String name;
    private String category;
    private double originalPrice;
    private double salePrice;
    private double priceByUnit;
    private double priceByCarton;
    private int quantity;
    private int sell_quantity=0;

    public Product(String number, String bread, String bakery, double v, double v1, double v2, double v3,int i1)
    {
        this.productId=number;
        this.name=bread;
        this.category=bakery;
        this.originalPrice=v;
        this.salePrice=v1;
        this.priceByUnit=v2;
        this.priceByCarton=v3;
        this.quantity=i1;

    }
    public Product(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Product(String productId, String productName, int quantity)
    {
        this.productId=productId;
        this.name=productName;
        this.quantity=quantity;
    }

    public Product() {

    }

    public void set_sold_quantity(int sellquantity1)
    {
        this.sell_quantity=sellquantity1;
    }
    public int get_sold_quantity()
    {
        return this.sell_quantity;
    }



    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public double getPriceByUnit() {
        return priceByUnit;
    }

    public void setPriceByUnit(double priceByUnit) {
        this.priceByUnit = priceByUnit;
    }

    public double getPriceByCarton() {
        return priceByCarton;
    }

    public void setPriceByCarton(double priceByCarton) {
        this.priceByCarton = priceByCarton;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    // Static method to parse a Product from a string
    public static Product parse(String line) {
        try {

            String[] parts = line.split(",");

            if (parts.length != 8) {
                throw new IllegalArgumentException("Invalid product data format.");
            }

            // Parse and construct the Product
            String productId = parts[0].trim();
            String name = parts[1].trim();
            String category = parts[2].trim();
            double originalPrice = Double.parseDouble(parts[3].trim());
            double salePrice = Double.parseDouble(parts[4].trim());
            double priceByUnit = Double.parseDouble(parts[5].trim());
            double priceByCarton = Double.parseDouble(parts[6].trim());
            int quantity = Integer.parseInt(parts[7].trim());

            return new Product(productId, name, category, originalPrice, salePrice, priceByUnit, priceByCarton, quantity);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse product: " + line, e);
        }
    }

    @Override
    public String toString() {
        return String.join(",",
                productId,
                name,
                category,
                String.valueOf(originalPrice),
                String.valueOf(salePrice),
                String.valueOf(priceByUnit),
                String.valueOf(priceByCarton),
                String.valueOf(quantity));
    }
}