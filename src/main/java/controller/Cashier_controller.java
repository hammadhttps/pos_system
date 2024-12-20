package controller;

import model.Cashier;
import services.CashierDAO;
import services.SaleDAO;
import model.Sale;


import java.util.Date;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import model.Product;
import services.ProductDAO;

import java.util.*;

public class Cashier_controller {

    private final SaleDAO saleDAO = new SaleDAO();
    private Cashier cashier=null;
    private final CashierDAO cashierDAO=new CashierDAO();


    public Pane bill;
    public Button btnCancel;
    public Button btnCreateBill;
    public Button btnCancel1;
    public ListView selectedItemsList;
    public ScrollPane scrollPane;
    @FXML
    private Button btnApplyDiscount;

    @FXML
    private Button btnGrocery, btnElectronics, btnfashion;

    @FXML
    private VBox vboxProductContainer;

    @FXML
    private TableView<Product> selectedItemsListtable;

    @FXML
    private TableColumn<Product, String> colProductName, colProductCategory;

    @FXML
    private TableColumn<Product, Double> colProductPrice;
    @FXML
    private TableColumn<Product, Integer> colProductquantity;


    private final ObservableList<Product> selectedProducts = FXCollections.observableArrayList();


    private Map<String, List<Product>> productsByCategory = new HashMap<>();
    private final Map<Product, Integer> productSelectionCount = new HashMap<>();

    private  int quantity;


    @FXML
    public void initialize() {

        //  table columns
        colProductName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colProductCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        colProductPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSalePrice()).asObject());
        colProductquantity.setCellValueFactory(data ->
                new SimpleIntegerProperty(productSelectionCount.getOrDefault(data.getValue(), 0)).asObject()
        );



        selectedItemsListtable.setItems(selectedProducts);


        loadProducts();

        // Button actions
        btnGrocery.setOnAction(event -> showProductsByCategory("Grocery"));
        btnElectronics.setOnAction(event -> showProductsByCategory("Electronic Accessories"));
        btnfashion.setOnAction(event -> showProductsByCategory("Fashion"));
        btnCancel1.setOnAction(event -> deleteSelectedItem());

        // Set action for Cancel button
        btnCancel.setOnAction(event -> resetTable());
        btnCreateBill.setOnAction(event -> createBill());
        btnApplyDiscount.setOnAction(event -> applyDiscount());
    }

    public void get_cashier_detail(String username1)
    {
        cashier=cashierDAO.getCashier(username1);
    }




    private double discountPercentage = 0.0;

    private void applyDiscount() {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Apply Discount");
        dialog.setHeaderText("Enter Discount Percentage");
        dialog.setContentText("Discount (%):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(discountInput -> {
            try {
                discountPercentage = Double.parseDouble(discountInput);
                if (discountPercentage < 0 || discountPercentage > 100) {
                    showAlert("Invalid Input", "Please enter a value between 0 and 100.");
                } else {
                    createBill(); // Recreate the bill with the updated discount
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    private void createBill() {
        if (selectedProducts.isEmpty()) {
            showAlert("No Items", "No items selected to create a bill.");
            return;
        }

        bill.getChildren().clear();
        bill.setVisible(true);

        VBox billLayout = new VBox(10);
        billLayout.setStyle("-fx-padding: 20; -fx-alignment: top-center; -fx-background-color: white;");

        Label title = new Label("Bill Summary");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label cashierLabel = new Label("Cashier: " + cashier.getName());
        Label branchCodeLabel = new Label("Branch Code: " + cashier.getBranchCode());
        cashierLabel.setStyle("-fx-font-size: 14px;");
        branchCodeLabel.setStyle("-fx-font-size: 14px;");

        TableView<Product> billTable = new TableView<>();
        billTable.setPrefWidth(750);
        billTable.setPrefHeight(400);

        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(250);

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        categoryCol.setPrefWidth(150);

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(data -> new SimpleIntegerProperty(productSelectionCount.getOrDefault(data.getValue(), 0)).asObject());
        quantityCol.setPrefWidth(100);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSalePrice()).asObject());
        priceCol.setPrefWidth(150);

        billTable.getColumns().addAll(nameCol, categoryCol, quantityCol, priceCol);
        billTable.setItems(FXCollections.observableArrayList(selectedProducts));

        double totalPrice = selectedProducts.stream()
                .mapToDouble(product -> product.getSalePrice() * productSelectionCount.get(product))
                .sum();
        double discountAmount = totalPrice * (discountPercentage / 100);
        double finalPrice = totalPrice - discountAmount;

        Label totalLabel = new Label("Total: $" + String.format("%.2f", totalPrice));
        Label discountLabel = new Label("Discount (" + discountPercentage + "%): -$" + String.format("%.2f", discountAmount));
        Label finalTotalLabel = new Label("Final Total: $" + String.format("%.2f", finalPrice));

        totalLabel.setStyle("-fx-font-size: 16px;");
        discountLabel.setStyle("-fx-font-size: 16px;");
        finalTotalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        closeButton.setOnAction(event -> bill.setVisible(false));

        billLayout.getChildren().addAll(
                title,
                cashierLabel,
                branchCodeLabel,
                billTable,
                totalLabel,
                discountLabel,
                finalTotalLabel,
                closeButton
        );
        bill.getChildren().add(billLayout);

        // Save the sale in the database
        Sale sale = new Sale(
                generate4DigitNumber(),
                new ArrayList<>(selectedProducts),
                finalPrice,
                new Date(),
                cashier.getBranchCode()
        );

        boolean isSaved = saleDAO.createSale(sale);

        if (isSaved) {
            // Update product quantities in the database
            ProductDAO productDAO = new ProductDAO();
            boolean isQuantityUpdated = productDAO.updateProductQuantities(productSelectionCount);

            if (isQuantityUpdated) {
                showAlert("Success", "Sale completed and inventory updated.");
            } else {
                showAlert("Error", "Sale saved, but inventory update failed.");
            }
        } else {
            showAlert("Error", "Failed to save the sale.");
        }

        resetTable(); // Clear the table after saving the bill
    }



    private void loadProducts() {
        ProductDAO productDAO = new ProductDAO();

        // Fetch products from the database for all categories
        List<Product> allProducts = productDAO.getAllProducts();

        // Cache products by category
        for (Product product : allProducts) {
            productsByCategory.computeIfAbsent(product.getCategory(), k -> new ArrayList<>()).add(product);
        }
    }

    private void showProductsByCategory(String category) {
        // Get the cached products for the selected category
        List<Product> products = productsByCategory.get(category);

        if (products != null) {
            vboxProductContainer.getChildren().clear();
            List<HBox> productRows = new ArrayList<>();

            // Create rows of 3 products
            for (int i = 0; i < products.size(); i += 3) {
                List<VBox> productBoxes = new ArrayList<>();
                for (int j = i; j < i + 3 && j < products.size(); j++) {
                    productBoxes.add(createProductBox(products.get(j)));
                }
                productRows.add(createRow(productBoxes.toArray(new VBox[0])));
            }

            vboxProductContainer.getChildren().addAll(productRows);
        } else {
            showAlert("No Products", "No products available in this category.");
        }
    }

    private void deleteSelectedItem() {
        Product selectedProduct = selectedItemsListtable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            int currentCount = productSelectionCount.get(selectedProduct);
            if (currentCount > 1) {
                productSelectionCount.put(selectedProduct, currentCount - 1); // Decrement count
            } else {
                productSelectionCount.remove(selectedProduct); // Remove from map
                selectedProducts.remove(selectedProduct); // Remove from TableView
            }

            // Decrement the sold quantity of the product
            if (selectedProduct.get_sold_quantity() > 0) {
                selectedProduct.setQuantity(selectedProduct.getQuantity()+1);
                selectedProduct.set_sold_quantity(selectedProduct.get_sold_quantity() - 1);
            }

            selectedItemsListtable.refresh(); // Refresh the table to show updated counts
        } else {
            showAlert("No Selection", "Please select an item to delete.");
        }
    }



    private void resetTable() {
        selectedProducts.clear();
    }

    private HBox createRow(VBox... products) {
        HBox row = new HBox(15);
        row.getChildren().addAll(products);
        return row;
    }

    private VBox createProductBox(Product product) {
        VBox productBox = new VBox(5);
        productBox.setStyle("-fx-alignment: center;");

        // Check if product is out of stock
        boolean isOutOfStock = product.getQuantity() == 0;

        // Set the image
        ImageView imageView = createImageView("C:\\Users\\city\\Desktop\\Project_pos\\src\\main\\resources\\com\\example\\project_pos\\products_icons\\" + product.getName() + ".png");

        // Disable the image if the product is out of stock
        if (isOutOfStock) {
            imageView.setOpacity(0.5); // Make image appear faded
            imageView.setOnMouseClicked(event -> showAlert("Out of Stock", "This product is currently out of stock."));
        } else {
            imageView.setOnMouseClicked(event -> addProductToSelectedList(product));
        }

        // Labels for the product name and price
        Label nameLabel = new Label(product.getName());
        Label priceLabel = new Label("$" + product.getSalePrice());

        // Create "Out of Stock" label if applicable
        if (isOutOfStock) {
            Label outOfStockLabel = new Label("Out of Stock");
            outOfStockLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            productBox.getChildren().addAll(imageView, nameLabel, priceLabel, outOfStockLabel);
        } else {
            productBox.getChildren().addAll(imageView, nameLabel, priceLabel);
        }

        return productBox;
    }

    private ImageView createImageView(String imagePath) {
        Image image;
        try {
            image = new Image(imagePath);
            if (image.isError()) {
                throw new IllegalArgumentException("Image not found.");
            }
        } catch (Exception e) {
            // Fallback to default image if the specified image is not found
            image = new Image("C:\\Users\\city\\Desktop\\Project_pos\\src\\main\\resources\\com\\example\\project_pos\\products_icons\\image.png");
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void addProductToSelectedList(Product product) {
        if (productSelectionCount.containsKey(product)) {
            int currentCount = productSelectionCount.get(product);
            productSelectionCount.put(product, currentCount + 1); // Increment count
        } else {
            productSelectionCount.put(product, 1); // Add with initial count of 1
            selectedProducts.add(product);
        }

        // Increment the sold quantity of the product
        product.set_sold_quantity(product.get_sold_quantity() + 1);
        product.setQuantity(product.getQuantity()-1);

        selectedItemsListtable.refresh();
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public static String generate4DigitNumber() {

        long currentTimeMillis = System.currentTimeMillis();


        int last4Digits = (int)(currentTimeMillis % 10000); // Get the last 4 digits


        return String.format("%04d", last4Digits);
    }
}
