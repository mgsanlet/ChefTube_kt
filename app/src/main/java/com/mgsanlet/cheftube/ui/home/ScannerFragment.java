package com.mgsanlet.cheftube.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mgsanlet.cheftube.R;

import java.util.Locale;

/**
 * A fragment that provides functionality for scanning product barcodes and displaying
 * nutritional information using the Open Food Facts API. Users can scan product barcodes
 * to retrieve and display product name, Nutri-Score, and Eco-Score information.
 * @author MarioG
 */
public class ScannerFragment extends Fragment {
    // -Declaring constants-
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v3/product/";
    // -Declaring UI elements-
    private ImageButton scanButton;
    private TextView productNameTView;
    private TextView nutriscoreTView;
    private TextView ecoscoreTView;
    private Button infoBtn;


    // -Declaring string resources-
    private String scanPromptStr;
    private String productNameStr;
    private String nutriscoreStr;
    private String ecoscoreStr;
    private String productNotFoundStr;

    // -Declaring variables-
    private String currentBarcode;
    // -Declaring shared preferences data-
    private static final String PREFS_NAME = "AppPrefs";
    private static final String LANGUAGE_KEY = "language";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_healthy, container, false);
        // -Initializing UI elements-
        scanButton = view.findViewById(R.id.scanButton);
        productNameTView = view.findViewById(R.id.productNameTView);
        nutriscoreTView = view.findViewById(R.id.nutriscoreTView);
        ecoscoreTView = view.findViewById(R.id.ecoscoreTView);
        infoBtn = view.findViewById(R.id.infoBtn);

        // -Initializing string resources-
        scanPromptStr = getString(R.string.scan_prompt);
        productNameStr = getString(R.string.product_name);
        nutriscoreStr = getString(R.string.nutriscore);
        ecoscoreStr = getString(R.string.ecoscore);
        productNotFoundStr = getString(R.string.product_not_found);

        scanButton.setOnClickListener(v -> startBarcodeScan());
        infoBtn.setOnClickListener(v -> openProductPage());
        return view;
    }

    /**
     * Initiates the barcode scanning process using the device's camera.
     * Configures the scanner with default settings:
     * - Uses all supported barcode formats
     * - Uses rear camera
     * - Plays beep sound on successful scan
     * - Disables barcode image saving
     */
    private void startBarcodeScan() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(scanPromptStr);
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    /**
     * Handles the result from the barcode scanning activity
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult()
     * @param resultCode The integer result code returned by the child activity
     * @param data An Intent containing the result data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        //String barcode = "3017620422003";  example
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                currentBarcode = result.getContents();
                fetchProductData();
            }
        }
    }


    /**
     * Fetches product data from the Open Food Facts API using the scanned barcode
     */
    private void fetchProductData() {

        // Forming the api request url based on barcode
        String url = BASE_URL + currentBarcode;

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        // -Creating JSON request-
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    // -Using GSon to process the answer-
                    Log.d("API_RESPONSE", response.toString());
                    processResponse(response.toString(), currentBarcode);
                },
                error -> {
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        Toast.makeText(getContext(), "API: Error " + statusCode, Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Processes the JSON response from the API and updates the UI
     *
     * @param jsonResponse The JSON string response from the API
     */
    private void processResponse(String jsonResponse, String currentBarcode) {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String locale = prefs.getString(LANGUAGE_KEY, Locale.getDefault().getLanguage());
        // -Using GSon to map the classes-
        Gson gson = new Gson();
        ProductResponse productResponse = gson.fromJson(jsonResponse, ProductResponse.class);
        String productName;

        if (productResponse.getProduct() != null) {
            // -Assigning product name based on locale-
            switch(locale){
                case "es":{
                    productName = productResponse.getProduct().getProductNameEs();
                    break;
                }
                case "it":{
                    productName = productResponse.getProduct().getProductNameIt();
                    break;
                }
                default:{
                    productName = productResponse.getProduct().getProductNameEn();
                    break;
                }
            }

            String nutriScore = productResponse.getProduct().getNutriscoreGrade();
            String ecoScore = productResponse.getProduct().getEcoscoreGrade();
            productNameTView.setText(productNameStr + "\n" + productName);
            nutriscoreTView.setText(nutriscoreStr + " " + nutriScore.toUpperCase());
            ecoscoreTView.setText(ecoscoreStr + " " + ecoScore.toUpperCase());
            if (currentBarcode != null && !currentBarcode.isEmpty()) {
                infoBtn.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(getContext(), productNotFoundStr, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Opens the product page on Open Food Facts website in a browser
     */
    private void openProductPage() {

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String locale = prefs.getString(LANGUAGE_KEY, Locale.getDefault().getLanguage());
        // -Forming web url based on locale and barcode-
        String productUrl = "https://"+locale+".openfoodfacts.org/product/" + currentBarcode;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(productUrl));
        startActivity(browserIntent);
    }

    /**
     * Data class for mapping the API response structure
     */
    public static class ProductResponse {
        private Product product;

        public Product getProduct() {
            return product;
        }
    }

    /**
     * Data class representing product information from the API
     */
    public static class Product {
        private String product_name_en;
        private String product_name_es;
        private String product_name_it;
        private String nutriscore_grade;
        private String ecoscore_grade;

        public String getProductNameEn() {
            return product_name_en;
        }

        public String getProductNameEs() {
            return product_name_es;
        }

        public String getProductNameIt() {
            return product_name_it;
        }


        public String getNutriscoreGrade() {
            return nutriscore_grade;
        }

        public String getEcoscoreGrade() {
            return ecoscore_grade;
        }
    }
}