sed -i 's/"email": "missingfields@finance.com"//' FDP/src/test/java/com/finance/backend/controller/AuthControllerTest.java
sed -i 's/"password": "Password@123"//' FDP/src/test/java/com/finance/backend/controller/AuthControllerTest.java
sed -i 's/"name": "Missing Name"/"name": ""/' FDP/src/test/java/com/finance/backend/controller/AuthControllerTest.java
