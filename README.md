You can build the project and run tests with: 

    mvn clean install

You can run the app with:
    
    mvn exec:java -pl core

Server will start listening to port 4567 on localhost.

There are four endpoints:
* Create account
```
    curl --request PUT \
      --url http://localhost:4567/accounting/account \
      --header 'content-type: application/json' \
      --data '{ "balance": 123.128 }'
```
* Get account
```
    curl --request GET \
      --url http://localhost:4567/accounting/account/1
```
* Validate transfer
```
    curl --request POST \
      --url http://localhost:4567/accounting/validateTransfer \
      --header 'content-type: application/json' \
      --data '{
                "sendingAccountId": 1,
                "receivingAccountId": 2,
                "amount": 100
              }'
```
* Process transfer
```
    curl --request POST \
      --url http://localhost:4567/accounting/processTransfer \
      --header 'content-type: application/json' \
      --data '{
                "sendingAccountId": 1,
                "receivingAccountId": 2,
                "amount": 10
              }'
```
