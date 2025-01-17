package org.example.utility.api;

import org.example.utility.courses.CourseAssembly;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * APIClient class handles the interaction with external APIs to fetch course recommendations based on user interests.
 * It supports concurrent API requests with timeout management and error handling.
 * The class implements the Deployment interface to manage the deployment process.
 */
public class APIClient implements Deployment {

    /**
     * Timeout duration (in milliseconds) for establishing a connection.
     */
    private static final int CONNECTION_TIMEOUT = 20000;

    /**
     * Timeout duration (in milliseconds) for reading data from the connection.
     */
    private static final int READ_TIMEOUT = 20000;

    /**
     * Fetches course data based on user interests from external APIs.
     * The method makes concurrent requests to two URLs (primary and backup) until it successfully receives data.
     *
     * @param interests The interests of the user to filter course recommendations.
     * @return A list of course codes recommended based on the provided interests.
     */
    public static ArrayList<String> getAPIDataClasses(String interests) {

        ArrayList<String> courses = new ArrayList<>();

        // First URL (primary API)
        String firstUrl = "http://127.0.0.1:8000/recommend-courses/";
        // Second URL (backup API)
        String secondUrl = "https://coursesapi-84sd.onrender.com/recommend-courses/";

        // Create executor service for concurrent tasks
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Callable for fetching data from first URL
        Callable<String> fetchFirstUrlTask = () -> fetchDataFromUrlClasses(firstUrl, interests);

        // Callable for fetching data from second URL
        Callable<String> fetchSecondUrlTask = () -> fetchDataFromUrlClasses(secondUrl, interests);

        boolean fetched = false;
        while (!fetched) {
            try {
                // Submit tasks and get futures
                Future<String> firstUrlFuture = executorService.submit(fetchFirstUrlTask);
                Future<String> secondUrlFuture = executorService.submit(fetchSecondUrlTask);

                // Get results, with timeout
                String firstUrlResponse = null;
                String secondUrlResponse = null;

                try {
                    firstUrlResponse = firstUrlFuture.get(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    System.out.println("Timeout occurred while fetching data from first URL.");
                }

                if (firstUrlResponse != null) {
                    // Parse and handle firstUrlResponse
                    parseJsonResponse(firstUrlResponse, courses);
                    fetched = true; // Set fetched to true on success
                    secondUrlFuture.cancel(true); // Cancel the second task if the first one succeeded
                } else {
                    // Attempt to fetch from the second URL if first URL failed or timed out
                    try {
                        secondUrlResponse = secondUrlFuture.get(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException e) {
                        System.out.println("Timeout occurred while fetching data from second URL.");
                    }

                    if (secondUrlResponse != null) {
                        // Parse and handle secondUrlResponse
                        parseJsonResponse(secondUrlResponse, courses);
                        fetched = true; // Set fetched to true on success
                    } else {
                        System.out.println("Failed to fetch data from both URLs.");
                    }
                }

            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Exception occurred: " + e.getMessage());
                fetched = true; // Set fetched to true to break out of the loop on exception
            }
        }

        // Shutdown executor service
        executorService.shutdown();

        return courses;
    }

    /**
     * Parses the JSON response from the API and extracts course codes into the provided list.
     *
     * @param responseData The raw JSON response as a string from the API.
     * @param courses The list to populate with course codes.
     */
    private static void parseJsonResponse(String responseData, ArrayList<String> courses) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String courseCode = jsonObject.getString("Course Code");
                courses.add(courseCode);
            }
        } catch (Exception e) {
            // If no data from the API (can't parse any data to JSON)
            System.out.println("No valid response");
        }
    }

    /**
     * Fetches data from a given URL using the GET method, with error handling and timeout management.
     *
     * @param apiUrl The URL to fetch data from.
     * @param encodedInterests The encoded interests parameter to be included in the request.
     * @return The raw response data as a string, or null if the request fails.
     */
    private static String fetchDataFromUrlClasses(String apiUrl, String encodedInterests) {
        try {
            URL url = new URL(String.format("%s?interests=%s", apiUrl, URLEncoder.encode(encodedInterests, "UTF-8")));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Set timeouts
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            conn.connect();

            // Check response code
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Failed to fetch data from URL: " + apiUrl + ", HTTP error code: " + responseCode);
                return null;
            }

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("Connection timed out while fetching data from URL: " + apiUrl);
            return null;
        } catch (IOException e) {
            System.out.println("IO Exception occurred while fetching data from URL: " + apiUrl + ", Error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Exception occurred while fetching data from URL: " + apiUrl + ", Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * A method to perform a POST request to an API endpoint.
     * The URL is fetched from the CourseAssembly class credentials file.
     */
    public static void runAPI() {
        String urlString = CourseAssembly.readCredentialsFromFile()[1];
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Connect and send request
            connection.connect();

            // Close the connection after sending the request
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}