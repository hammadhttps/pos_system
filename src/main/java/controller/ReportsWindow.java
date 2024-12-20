package controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import model.Sale;
import model.Product;
import services.ProductDAO;
import services.SaleDAO;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReportsWindow {

    private String branchcode;
    private List<Sale> sales = new ArrayList<>();
    private final SaleDAO saleDAO = new SaleDAO();
    private final ProductDAO productDAO=new ProductDAO();
    private List<Product> product;

    @FXML
    private Pane displayPane;

    @FXML
    private TableView<Product> productTable;  // Table to display products
    @FXML
    private TableColumn<Product, String> productIdColumn;
    @FXML
    private TableColumn<Product, String> productNameColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, Integer> quantityColumn;
    @FXML
    private TableColumn<Product, Double> salePriceColumn;
    @FXML
    private TableColumn<Product, Double> priceByCartonColumn;
    @FXML
    private TableColumn<Product, Double> priceByUnitColumn;




    public void setBranchcode(String bc) {
        this.branchcode = bc;
        sales = saleDAO.getSalesByBranchCode(bc);
    }


    public void sales_graph(ActionEvent event) {
        productTable.setVisible(false);
        LineChart<String, Number> lineChart = createSalesLineChart();
        displayPane.getChildren().clear();
        displayPane.getChildren().add(lineChart);
    }


    public void sales_chart(ActionEvent event)
    {
        productTable.setVisible(false);
        BarChart<String, Number> barChart = createSalesBarChart();
        displayPane.getChildren().clear();
        displayPane.getChildren().add(barChart);
    }

    public void inventory(ActionEvent event) {
        productTable.setVisible(true);
        // Fetch the list of products from the ProductDAO
        product = productDAO.getAllProducts();


        productIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductId()));


        productNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));


        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));


        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());


        salePriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getSalePrice()).asObject());


        priceByCartonColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPriceByCarton()).asObject());


        priceByUnitColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPriceByUnit()).asObject());


        productTable.setItems(javafx.collections.FXCollections.observableArrayList(product));
    }



    private LineChart<String, Number> createSalesLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Total Sales Amount");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Sales Graph");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Sales");

        // Populate the series with sales data
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        for (Sale sale : sales) {
            series.getData().add(new XYChart.Data<>(dateFormat.format(sale.getDate()), sale.getTotalAmount()));
        }

        lineChart.getData().add(series);
        return lineChart;
    }

    // Create a BarChart to show sales per product
    private BarChart<String, Number> createSalesBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Product");
        yAxis.setLabel("Quantity Sold");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Sales Chart");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Products Sold");


        Map<String, Integer> productSalesMap = new HashMap<>();
        for (Sale sale : sales) {
            for (Product product : sale.getProducts()) {
                String productId = product.getProductId();
                int quantitySold = product.getQuantity();


                productSalesMap.put(productId, productSalesMap.getOrDefault(productId, 0) + quantitySold);
            }
        }


        for (Map.Entry<String, Integer> entry : productSalesMap.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);
        return barChart;
    }
}
