# Step 3 Report: API Integration
## 1. API Selection and Rationale
### Chosen API: DummyJSON Quotes API
**URL:** `https://dummyjson.com/`
### Why This API?
The DummyJSON Quotes API was selected for the IoT Bazaar application because:
1. **No Authentication Was Required:** The API doesn't require API keys or account setup.
2. **Free:** DummyJSON is a free REST API.
3. **Simple JSON Structure:** The API returns clean, predictable JSON responses that are easy to parse and integrate with Kotlin data classes using Gson.
## 2. API Endpoint Used
### Endpoint: Random Quote
GET https://dummyjson.com/quotes/random

### Response Format
```
{
  "id": 42,
  "quote": "Success is not final, failure is not fatal: it is the courage to continue that counts.",
  "author": "Winston Churchill"
}
```

## 3. Error Handling Strategy

### 3.1 Network Layer Error Handling

Retrofit client has a built-in error handling mechanism for HTTP errors. The response is checked for success and if an error occurs the text is set to display an error.
If a network error occurs (e.g., no internet connection), the failure callback is triggered (onFailure), which sets the text on the app to display a network error text. Additionally, a Toast message is shown to provide more context to the user.

### 3.2 User Feedback
1. **Loading State:** 
   - Shows "Loading quote..." immediately to indicate processing
2. **Success State:** 
   - Displays formatted quote with author attribution
3. **Error States:**
   - **HTTP Error (4xx/5xx):** "Failed to load quote. Please try again later."
   - **Network Error:** "No internet connection. Please check your network."
   - **Toast Notification:** Additional error details shown in Toast for debugging
