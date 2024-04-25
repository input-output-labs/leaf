# LEAF SMSENDER
---

# SMS Sender Service

This service provides functionality for sending SMS messages using Twilio API.

## Prerequisites
Twilio account SID, auth token, and a phone number to use for sending SMS messages

## Setup
Open the `application.properties` file and provide your Twilio account SID, auth token, and phone number in the appropriate fields:

   ```properties
   leaf.smsender.twilio.api.accountSid=your_account_sid
   leaf.smsender.twilio.api.authToken=your_auth_token
   leaf.smsender.twilio.api.phoneNumber=your_twilio_phone_number
   ```

## API Endpoints
### Send SMS

- **URL:** `/api/smsender/send-sms`
- **Method:** POST
- **Request Body:**
  ```json
  {
    "to": "recipient_phone_number",
    "message": "message_content"
  }
  ```
- **Response:**
	- Success: HTTP 200 OK with message "SMS sent successfully"
	- Failure: 
      - HTTP 400 Bad Request with error message if request is invalid
      - HTTP 500 Internal Server Error with error message if sending SMS fails.

## Usage
##### FYI: to test these endpoints without spring security restrictions feel free to add  `.excludePathPatterns("/api/smsender/send-sms")` in LeafConfig.java;
To send an SMS message, make a POST request to the `/api/smsender/send-sms` endpoint with the recipient's phone number and the message content in the request body.

Example using cURL:

```bash
curl -X POST http://localhost:8080/api/smsender/send-sms \
  -H "Content-Type: application/json" \
  -d '{"to":"recipient_phone_number", "message":"Hello from SMS Sender Service"}'
```
---