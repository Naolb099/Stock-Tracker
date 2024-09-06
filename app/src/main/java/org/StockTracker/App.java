package org.StockTracker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class App extends Application {
    private static final String API_KEY = "E770TIKGBHY8OIQL";
    private static final String SYMBOL = "MSFT";
    private static final int WAIT_TIME_MS = 5000;

    private LineChart<String, Number> lineChart;
    private XYChart.Series<String, Number> series;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Google (GOOG) Stock Price");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Stock Price (USD)");

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Real-Time Google (GOOG) Stock Price");

        series = new XYChart.Series<>();
        series.setName("GOOG Stock Prices");
        lineChart.getData().add(series);

        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();

        startFetchingData();
    }

    private void startFetchingData() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    fetchStockData(SYMBOL, API_KEY);
                } catch (Exception e) {
                    System.err.println("Error fetching stock data: " + e.getMessage());
                }
            }
        }, 0, WAIT_TIME_MS);
    }

    private void fetchStockData(String symbol, String apiKey) throws Exception {
        String urlString = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey="
                + apiKey;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JsonObject jsonResponse = JsonParser.parseString(content.toString()).getAsJsonObject();
            JsonObject globalQuote = jsonResponse.getAsJsonObject("Global Quote");

            if (globalQuote != null) {
                String stockPriceStr = globalQuote.get("05. price").getAsString();
                BigDecimal stockPrice = new BigDecimal(stockPriceStr);
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

                Platform.runLater(() -> updateChart(time, stockPrice.doubleValue()));
            } else {
                System.out.println("Global Quote data is not available in the response.");
            }
        } else {
            System.out.println("Failed to fetch data. HTTP response code: " + connection.getResponseCode());
        }
    }

    private void updateChart(String time, double stockPrice) {
        series.getData().add(new XYChart.Data<>(time, stockPrice));
    }
}
