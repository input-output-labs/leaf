# Leaf

## Installation & run

1) `mvn install`
2) `cd demo`
3) `mvn spring-boot:run`


While developping, you can go directly to the demo folder and run each time:
`cd .. && mvn install && cd ./demo && mvn spring-boot:run`


## Bump version script

To bump the leaf packages to the same version, you can use the script bump_version.sh with the version you want to bump:

```
sh ./bump_version.sh 1.30
```

## Stripe

### Checkout

Useful links: 
- API checkout: https://stripe.com/docs/api/checkout/sessions
- Webhook test with stripe command line (for local testing): https://stripe.com/docs/webhooks/test

Command line:
After installing and loggin in with stripe cli:
-- Listen to the webhook: `stripe listen --forward-to http://127.0.0.1:8080/api/checkout-sessions/webhook`
-- Dispatch an event manually for testing (cf trigger event doc below): `stripe trigger checkout.session.completed`


- Event API (returned by Stripe's webhook): https://stripe.com/docs/api/events/object && https://stripe.com/docs/cli/trigger#trigger-event
- Postman collection: https://www.postman.com/stripedev/workspace/stripe-developers/overview


